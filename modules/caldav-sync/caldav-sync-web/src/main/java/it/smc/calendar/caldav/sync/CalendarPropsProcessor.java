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

package it.smc.calendar.caldav.sync;

import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.petra.string.StringPool;
import com.liferay.petra.xml.DocUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.io.unsync.UnsyncStringWriter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.util.PrefsPropsUtil;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.Element;

import it.smc.calendar.caldav.helper.api.CalendarHelperUtil;
import it.smc.calendar.caldav.sync.util.CalDAVProps;
import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import it.smc.calendar.caldav.sync.util.CalendarModelPermission;
import it.smc.calendar.caldav.sync.util.ICalUtil;
import it.smc.calendar.caldav.sync.util.WebKeys;
import it.smc.calendar.caldav.util.PropsValues;

import java.util.Optional;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.ComponentList;

/**
 * @author Fabio Pezzutto
 */
public class CalendarPropsProcessor extends BasePropsProcessor {

	public CalendarPropsProcessor() {
	}

	public CalendarPropsProcessor(
		WebDAVRequest webDAVRequest, Resource resource, Element rootElement) {

		super(webDAVRequest, resource, rootElement);

		_calendar = (Calendar)resource.getModel();
	}

	@Override
	protected void processCalDAVCalendarColor() {
		String color = CalDAVUtil.getCalendarColor(_calendar);

		if (CalDAVUtil.isMacOSX(webDAVRequest)) {
			color = color.concat("FF");
		}

		DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_COLOR, color);
	}

	@Override
	protected void processCalDAVCalendarDescription() {
		DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_DESCRIPTION,
			_calendar.getDescription(locale));
	}

	@Override
	protected void processCalDAVCalendarHomeSet() {
		CalendarResource calendarResource;

		try {
			calendarResource = _calendar.getCalendarResource();
		}
		catch (Exception e) {
			_log.error(e);

			return;
		}

		Element calendarHomeSetElement = DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_HOME_SET);

		DocUtil.add(
			calendarHomeSetElement, CalDAVProps.createQName("href"),
			CalDAVUtil.getCalendarResourceURL(calendarResource));
	}

	@Override
	protected void processCalDAVCalendarTimeZone() {
		net.fortuna.ical4j.model.Calendar iCalCalendar =
			ICalUtil.getVTimeZoneCalendar();

		Element calendarTimeZoneElement = DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_TIMEZONE);

		try {
			calendarTimeZoneElement.addCDATA(vTimeZoneToString(iCalCalendar));
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	@Override
	protected void processCalDAVCalendarUserAddressSet() {
		CalendarResource calendarResource;

		try {
			calendarResource = _calendar.getCalendarResource();
		}
		catch (Exception e) {
			_log.error(e);

			return;
		}

		String address = CalDAVUtil.getCalendarResourceURL(calendarResource);

		Optional<User> user = CalendarHelperUtil.getCalendarResourceUser(
			calendarResource);

		if (user.isPresent()) {
			address =
				"mailto:" +
					user.get(
					).getEmailAddress();
		}

		Element calendarUserAddressSetElement = DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET);

		DocUtil.add(
			calendarUserAddressSetElement, CalDAVProps.createQName("href"),
			address);
	}

	@Override
	protected void processCalDAVGetCTag() {
		DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_GETCTAG,
			CalDAVUtil.getResourceETag(resource));
	}

	@Override
	protected void processCalDAVMaxResourceSize() {
		long maxResourceSize =
			PrefsPropsUtil.getLong(
				PropsKeys.UPLOAD_SERVLET_REQUEST_IMPL_MAX_SIZE) / 8;

		DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_MAX_RESOURCE_SIZE,
			maxResourceSize);
	}

	@Override
	protected void processCalDAVSupportedCalendarComponentSet() {
		Element supportedCalendarComponentSet = DocUtil.add(
			successPropElement,
			CalDAVProps.CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET);

		Element el = DocUtil.add(
			supportedCalendarComponentSet, CalDAVProps.DAV_COMP);

		el.addAttribute("name", WebKeys.VCALENDAR);

		el = DocUtil.add(supportedCalendarComponentSet, CalDAVProps.DAV_COMP);

		el.addAttribute("name", WebKeys.VEVENT);
	}

	@Override
	protected void processCalDAVSupportedCalendarData() {
		Element supportedCalendarDataElement = DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_SUPPORTED_CALENDAR_DATA);

		Element calendarDataElement = DocUtil.add(
			supportedCalendarDataElement,
			CalDAVProps.createCalendarQName("calendar-data"));

		calendarDataElement.addAttribute(
			"content-type", resource.getContentType());

		calendarDataElement.addAttribute("version", "2.0");
	}

	@Override
	protected void processCalDAVValidCalendarData() {
		Element validCalendarDataElement = DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_VALID_CALENDAR_DATA);

		net.fortuna.ical4j.model.Calendar iCalCalendar =
			ICalUtil.getVTimeZoneCalendar();

		try {
			validCalendarDataElement.addCDATA(vTimeZoneToString(iCalCalendar));
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	@Override
	protected void processDAVCurrentUserPrivilegeSet() {
		Element currentUserPrivilegeSetElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_CURRENT_USER_PRIVILEGE_SET);

		Element readPrivilegeElement = DocUtil.add(
			currentUserPrivilegeSetElement,
			CalDAVProps.createQName("privilege"));

		DocUtil.add(readPrivilegeElement, CalDAVProps.createQName("read"));

		if (CalendarModelPermission.contains(
				webDAVRequest.getPermissionChecker(), _calendar,
				ActionKeys.UPDATE)) {

			DocUtil.add(readPrivilegeElement, CalDAVProps.createQName("write"));

			DocUtil.add(
				readPrivilegeElement, CalDAVProps.createQName("write-content"));
		}
	}

	@Override
	protected void processDAVDisplayName() {
		if (PropsValues.EXTENDED_DISPLAY_NAME) {
			StringBuilder sb = new StringBuilder();

			try {
				CalendarResource calendarResource =
					_calendar.getCalendarResource();

				if (calendarResource != null) {
					sb.append(calendarResource.getName(locale));
					sb.append(" - ");
				}
			}
			catch (PortalException pe) {
				if (_log.isWarnEnabled()) {
					_log.warn(pe.getMessage());
				}
			}

			sb.append(_calendar.getName(locale));

			DocUtil.add(
				successPropElement, CalDAVProps.DAV_DISPLAYNAME, sb.toString());
		}
		else {
			DocUtil.add(
				successPropElement, CalDAVProps.DAV_DISPLAYNAME,
				_calendar.getName(
					locale
				).replaceAll(
					StringPool.SPACE, StringPool.BLANK
				));
		}
	}

	@Override
	protected void processDAVGetContentLength() {
		DocUtil.add(
			successPropElement, CalDAVProps.DAV_GETCONTENTLENGTH,
			resource.getSize());
	}

	@Override
	protected void processDAVOwner() {
		DocUtil.add(
			successPropElement, CalDAVProps.DAV_OWNER,
			CalDAVUtil.getPrincipalURL(_calendar.getUserId()));
	}

	@Override
	protected void processDAVResourceId() {
		Element resourceIdElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_RESOURCE_ID);

		DocUtil.add(
			resourceIdElement, CalDAVProps.createQName("href"),
			"urn:uuid:".concat(_calendar.getUuid()));
	}

	@Override
	protected void processDAVResourceType() {
		Element resourceTypeElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_RESOURCETYPE);

		if (!CalDAVUtil.isThunderbird(webDAVRequest)) {
			DocUtil.add(
				resourceTypeElement, CalDAVProps.createQName("collection"));
		}

		DocUtil.add(
			resourceTypeElement, CalDAVProps.createCalendarQName("calendar"));
	}

	protected String vTimeZoneToString(
			net.fortuna.ical4j.model.Calendar iCalCalendar)
		throws Exception {

		CalendarOutputter calendarOutputter = new CalendarOutputter();

		ComponentList componentList = iCalCalendar.getComponents();

		if (componentList.isEmpty()) {
			calendarOutputter.setValidating(false);
		}

		UnsyncStringWriter unsyncStringWriter = new UnsyncStringWriter();

		calendarOutputter.output(iCalCalendar, unsyncStringWriter);

		unsyncStringWriter.flush();

		return unsyncStringWriter.toString();
	}

	private static Log _log = LogFactoryUtil.getLog(
		CalendarPropsProcessor.class);

	private Calendar _calendar;

}