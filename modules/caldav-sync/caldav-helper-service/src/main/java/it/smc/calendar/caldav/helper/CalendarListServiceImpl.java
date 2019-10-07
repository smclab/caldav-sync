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

package it.smc.calendar.caldav.helper;

import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.CalendarLocalServiceUtil;
import com.liferay.calendar.service.CalendarResourceServiceUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.SessionClicks;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import it.smc.calendar.caldav.helper.api.CalendarListService;
import it.smc.calendar.caldav.helper.util.PropsValues;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author rge
 */
@Component(immediate = true, service = CalendarListService.class)
public class CalendarListServiceImpl implements CalendarListService {

	public static List<Calendar> getSelectedCalendars(
			PermissionChecker permissionChecker)
		throws PortalException {

		ArrayList<Calendar> calendars = new ArrayList<>();

		long userId = permissionChecker.getUserId();

		String otherCalendarPreferences =
			"com.liferay.calendar.web_otherCalendars";

		PortalPreferences preferences =
			PortletPreferencesFactoryUtil.getPortalPreferences(userId, true);

		String calendarIdsPref = preferences.getValue(
			SessionClicks.class.getName(), otherCalendarPreferences);

		long[] calendarIds = GetterUtil.getLongValues(
			StringUtil.split(calendarIdsPref));

		for (long calendarId : calendarIds) {
			Calendar calendar = CalendarLocalServiceUtil.fetchCalendar(
				calendarId);

			if (calendar == null) {
				continue;
			}

			if (_calendarModelResourcePermission.contains(
					permissionChecker, calendarId, ActionKeys.VIEW)) {

				calendars.add(calendar);
			}
		}

		return calendars;
	}

	public static List<Calendar> getUserGroupCalendars(
			PermissionChecker permissionChecker)
		throws PortalException {

		List<Calendar> calendars = new ArrayList<>();

		long classNameId = PortalUtil.getClassNameId(Group.class.getName());

		List<CalendarResource> calendarResources =
			CalendarResourceServiceUtil.search(
				permissionChecker.getCompanyId(), new long[] {}, new long[] {
					classNameId
				}, null, true, true, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

		for (CalendarResource calendarResource : calendarResources) {
			for (Calendar calendar : calendarResource.getCalendars()) {
				if (_calendarModelResourcePermission.contains(
					permissionChecker, calendar, ActionKeys.VIEW)) {

					calendars.add(calendar);
				}
			}
		}

		return calendars;
	}

	@Override
	public List<Calendar> getAllCalendars(PermissionChecker permissionChecker)
		throws PortalException {

		boolean useSessionClicksCalendars =
			PropsValues.PROPFIND_PROVIDE_SESSIONCLICKS_CALENDARS;
		boolean hidePersonalCalendar = PropsValues.HIDE_PERSONAL_CALENDAR;

		List<Calendar> calendars = new ArrayList<>();

		if (useSessionClicksCalendars) {
			if (!hidePersonalCalendar) {
				if (_log.isDebugEnabled()) {
					_log.debug("Get user personal calendars");
				}

				List<Calendar> userCalendars =
					getUserCalendars(permissionChecker.getUserId());
				if (Validator.isNotNull(userCalendars)) {
					if (_log.isDebugEnabled()) {
						for (Calendar calendar : userCalendars) {
							_log.debug(" - " + calendar.getName());
						}
					}

					calendars.addAll(userCalendars);
				}
			}

			if (PropsValues.PROPFIND_PROVIDE_USER_GROUPS) {
				if (_log.isDebugEnabled()) {
					_log.debug(
						"Get also group calendars the the user belong to");
				}

				List<Calendar> userGroupCalendars =
					getUserGroupCalendars(permissionChecker);
				if (Validator.isNotNull(userGroupCalendars)) {
					if (_log.isDebugEnabled()) {
						for (Calendar calendar : userGroupCalendars) {
							_log.debug(" - " + calendar.getName());
						}
					}

					calendars.addAll(userGroupCalendars);
				}
			}
			else if (_log.isDebugEnabled()) {
				_log.debug("Don't get group calendars");
			}

			if (_log.isDebugEnabled()) {
				_log.debug("Get session click selected calendars");
			}

			List<Calendar> selectedCalendars =
				getSelectedCalendars(permissionChecker);

			if(Validator.isNotNull(selectedCalendars)){
				if (_log.isDebugEnabled()) {
					for (Calendar calendar : selectedCalendars) {
						_log.debug(" - " + calendar.getName());
					}
				}

				calendars.addAll(selectedCalendars);
			}
		}
		else {
			if (_log.isDebugEnabled()) {
				_log.debug("Get all user's visible calendars");
			}

			List<Calendar> allCalendars = CalendarLocalServiceUtil.getCalendars(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

			for (Calendar calendar : allCalendars) {
				if (_calendarModelResourcePermission.contains(
						permissionChecker, calendar, ActionKeys.VIEW)) {

					if (_log.isDebugEnabled()) {
						_log.debug(" - " + calendar.getName());
					}

					calendars.add(calendar);
				}
			}
		}

		return calendars;
	}

	public List<Calendar> getUserCalendars(long userId) {
		long classNameId = PortalUtil.getClassNameId(User.class.getName());

		CalendarResource calendarResource = null;
		try {
			calendarResource =
				CalendarResourceServiceUtil.fetchCalendarResource(
					classNameId, userId);
			if (Validator.isNull(calendarResource)) {
				return null;
			}
		}
		catch (PortalException pe) {
			_log.error(pe, pe);
		}
		return calendarResource.getCalendars();
	}

	private static Log _log = LogFactoryUtil.getLog(
		CalendarListServiceImpl.class);

	@Reference(
		target = "(model.class.name=com.liferay.calendar.model.Calendar)",
		unbind = "-"
	)
	protected void setModelPermissionChecker(
		ModelResourcePermission<Calendar> modelResourcePermission) {

		_calendarModelResourcePermission = modelResourcePermission;
	}

	private static ModelResourcePermission<Calendar>
		_calendarModelResourcePermission;
}