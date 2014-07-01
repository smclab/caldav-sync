/**
 * Copyright (c) 2013 SMC Treviso Srl. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package it.smc.calendar.sync.caldav;

import com.liferay.calendar.NoSuchCalendarException;
import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.CalendarBookingServiceUtil;
import com.liferay.calendar.service.CalendarLocalServiceUtil;
import com.liferay.calendar.service.CalendarResourceLocalServiceUtil;
import com.liferay.calendar.service.CalendarServiceUtil;
import com.liferay.calendar.service.permission.CalendarResourcePermission;
import com.liferay.calendar.util.CalendarDataFormat;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.webdav.BaseWebDAVStorageImpl;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVException;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.webdav.methods.MethodFactory;
import com.liferay.portal.kernel.webdav.methods.MethodFactoryRegistryUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.PrincipalException;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.UserServiceUtil;
import com.liferay.portal.util.PortalUtil;

import it.smc.calendar.sync.caldav.util.CalDAVUtil;
import it.smc.calendar.sync.calendar.util.CalendarUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Fabio Pezzutto
 */
public class LiferayCalDAVStorageImpl extends BaseWebDAVStorageImpl {

	@Override
	public int deleteResource(WebDAVRequest webDAVRequest)
		throws WebDAVException {

		try {
			CalendarBooking calendarBooking = (CalendarBooking)getResource(
				webDAVRequest).getModel();

			CalendarBookingServiceUtil.deleteCalendarBooking(
				calendarBooking.getCalendarBookingId());

			return HttpServletResponse.SC_NO_CONTENT;
		}
		catch (PrincipalException pe) {
			return HttpServletResponse.SC_FORBIDDEN;
		}
		catch (NoSuchCalendarException nsfe) {
			return HttpServletResponse.SC_CONFLICT;
		}
		catch (PortalException pe) {
			if (_log.isWarnEnabled()) {
				_log.warn(pe, pe);
			}

			return HttpServletResponse.SC_CONFLICT;
		}
		catch (Exception e) {
			throw new WebDAVException(e);
		}
	}

	@Override
	public MethodFactory getMethodFactory() {

		return MethodFactoryRegistryUtil.getMethodFactory(
			CalDAVMethodFactory.class.getName());
	}

	@Override
	public Resource getResource(WebDAVRequest webDAVRequest)
		throws WebDAVException {

		try {
			String[] pathArray = webDAVRequest.getPathArray();
			String method = webDAVRequest.getHttpServletRequest().getMethod();

			if (CalDAVUtil.isPrincipalRequest(webDAVRequest)) {
				long userid = GetterUtil.getLong(pathArray[2]);
				User user = null;

				try {
					user = UserServiceUtil.getUserById(userid);
				}
				catch (Exception e) {
					if (_log.isWarnEnabled()) {
						_log.warn(e);
					}
				}

				if (user == null) {
					throw new WebDAVException(
						"No user were found with id " + pathArray[2]);
				}

				return toResource(webDAVRequest, user);
			}

			// calendar resource collection request

			String calendarResourceGUID = pathArray[0];
			CalendarResource calendarResource =
				CalendarResourceLocalServiceUtil.
					fetchCalendarResourceByUuidAndCompanyId(
						calendarResourceGUID, webDAVRequest.getCompanyId());

			if (calendarResource == null) {
				throw new ResourceNotFoundException (
					"No calendar resource were found for GUID " +
					calendarResourceGUID);
			}

			//CalendarResourcePermission.check(
			//	webDAVRequest.getPermissionChecker(), calendarResource,
			//	ActionKeys.VIEW);

			if (CalDAVUtil.isCalendarBookingRequest(webDAVRequest) &&
				!method.equals(CalDAVHttpMethods.PUT)) {

				// calendar booking resource

				String resourceName = pathArray[pathArray.length - 1];
				String resourceExtension = StringPool.PERIOD.concat(
					FileUtil.getExtension(resourceName));

				if (resourceName.endsWith(resourceExtension)) {
					String resourceShortName = StringUtil.replace(
						resourceName, resourceExtension, StringPool.BLANK);
					long calendarBookingId = GetterUtil.getLong(
						resourceShortName);

					CalendarBooking calendarBooking =
						CalendarBookingServiceUtil.fetchCalendarBooking(
							calendarBookingId);

					if (calendarBooking == null) {
						throw new ResourceNotFoundException(
							"Calendar booking not found with id: " +
							calendarBookingId);
					}

					return toResource(webDAVRequest, calendarBooking);
				}
			}

			if (CalDAVUtil.isCalendarRequest(webDAVRequest)) {

				// calendar request

				String calendarGUID = pathArray[2];

				Calendar calendar =
					CalendarLocalServiceUtil.fetchCalendarByUuidAndCompanyId(
						calendarGUID, webDAVRequest.getCompanyId());

				if (calendar == null) {
					throw new ResourceNotFoundException (
						"No calendar were found for GUID " + calendarGUID);
				}

				//CalendarPermission.check(
				//	webDAVRequest.getPermissionChecker(), calendar,
				//	ActionKeys.VIEW);

				return toResource(webDAVRequest, calendar);
			}
			else {
				return toResource(webDAVRequest, calendarResource);
			}
		}
		catch (Exception e) {
			throw new WebDAVException(e);
		}
	}

	@Override
	public List<Resource> getResources(WebDAVRequest webDAVRequest)
		throws WebDAVException {

		try {
			String[] pathArray = webDAVRequest.getPathArray();

			// calendar resource collection request

			CalendarResource calendarResource;

			if (CalDAVUtil.isPrincipalRequest(webDAVRequest)) {
				long userid = GetterUtil.getLong(pathArray[2]);
				User user = null;

				try {
					user = UserServiceUtil.getUserById(userid);
				}
				catch (Exception e) {
					if (_log.isWarnEnabled()) {
						_log.warn(e);
					}
				}

				if (user == null) {
					throw new WebDAVException(
						"No user were found with id " + pathArray[2]);
				}

				calendarResource =
					CalendarResourceLocalServiceUtil.fetchCalendarResource(
						PortalUtil.getClassNameId(User.class),
						user.getPrimaryKey());
			}
			else {
				String calendarResourceGUID = pathArray[0];

				calendarResource =
					CalendarResourceLocalServiceUtil.
						fetchCalendarResourceByUuidAndCompanyId(
							calendarResourceGUID, webDAVRequest.getCompanyId());
			}

			if (calendarResource == null) {
				throw new WebDAVException("No calendar resource were found");
			}

			CalendarResourcePermission.check(
				webDAVRequest.getPermissionChecker(), calendarResource,
				ActionKeys.VIEW);

			if (CalDAVUtil.isCalendarRequest(webDAVRequest)) {
				return toCalendarBookingResources(webDAVRequest);
			}
			else {
				return toCalendarResources(webDAVRequest, calendarResource);
			}
		}
		catch (Exception e) {
			throw new WebDAVException(e);
		}
	}

	@Override
	public boolean isSupportsClassTwo() {
		return false;
	}

	@Override
	public int putResource(WebDAVRequest webDAVRequest) throws WebDAVException {
		try {
			String data = CalDAVRequestThreadLocal.getRequestContent();

			Calendar calendar = (Calendar)getResource(webDAVRequest).getModel();

			ICSSanitizer sanitizer = new ICSSanitizer(calendar);
			data = sanitizer.sanitize(data);

			CalendarServiceUtil.importCalendar(
				calendar.getCalendarId(), data,
				CalendarDataFormat.ICAL.getValue());

			return HttpServletResponse.SC_CREATED;
		}
		catch (PrincipalException pe) {
			return HttpServletResponse.SC_FORBIDDEN;
		}
		catch (NoSuchCalendarException nsfe) {
			return HttpServletResponse.SC_CONFLICT;
		}
		catch (PortalException pe) {
			if (_log.isWarnEnabled()) {
				_log.warn(pe, pe);
			}

			return HttpServletResponse.SC_CONFLICT;
		}
		catch (Exception e) {
			throw new WebDAVException(e);
		}
	}

	protected List<Resource> toCalendarBookingResources(
			WebDAVRequest webDAVRequest)
		throws PortalException, SystemException {

		Calendar calendar = (Calendar)getResource(webDAVRequest).getModel();

		List<CalendarBooking> calendarBookings =
			CalendarUtil.getCalendarBookings(
				webDAVRequest.getPermissionChecker(), calendar, null, null);

		List<Resource> resources = new ArrayList<Resource>();

		for (CalendarBooking calendarBooking : calendarBookings) {
			resources.add(toResource(webDAVRequest, calendarBooking));
		}

		return resources;
	}

	protected List<Resource> toCalendarResources(
			WebDAVRequest webDAVRequest, CalendarResource calendarResource)
		throws PortalException, SystemException {

		List<Calendar> calendars;

		if (CalDAVUtil.isIOS(webDAVRequest)) {

			calendars = CalendarUtil.getAllCalendars(
				webDAVRequest.getPermissionChecker());
		}
		else {
			calendars = CalendarUtil.getCalendarResourceCalendars(
				calendarResource);
		}

		List<Resource> resources = new ArrayList<Resource>();

		for (Calendar calendar : calendars) {
			resources.add(toResource(webDAVRequest, calendar));
		}

		return resources;
	}

	protected Resource toResource(
			WebDAVRequest webDAVRequest, Calendar calendar) {

		String parentPath = getRootPath() + webDAVRequest.getPath();

		Locale locale = PortalUtil.getLocale(
			webDAVRequest.getHttpServletRequest());

		return new CalendarResourceImpl(calendar, parentPath, locale);
	}

	protected Resource toResource(
		WebDAVRequest webDAVRequest, CalendarBooking calendarBooking) {

		String parentPath = getRootPath() + webDAVRequest.getPath();

		Locale locale = PortalUtil.getLocale(
			webDAVRequest.getHttpServletRequest());

		return new CalendarBookingResourceImpl(
			calendarBooking, parentPath, locale);
	}

	protected Resource toResource(
			WebDAVRequest webDAVRequest, CalendarResource calendarResource) {

		String parentPath = getRootPath() + webDAVRequest.getPath();

		Locale locale = PortalUtil.getLocale(
			webDAVRequest.getHttpServletRequest());

		return new CalendarResourceResourceImpl(
			calendarResource, parentPath, locale);
	}

	protected Resource toResource(WebDAVRequest webDAVRequest, User user) {
		String parentPath = getRootPath() + webDAVRequest.getPath();

		Locale locale = PortalUtil.getLocale(
			webDAVRequest.getHttpServletRequest());

		return new UserResourceImpl(user, parentPath, locale);
	}

	private static Log _log = LogFactoryUtil.getLog(
		LiferayCalDAVStorageImpl.class);

}