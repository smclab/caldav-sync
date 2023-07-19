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
import com.liferay.calendar.service.CalendarLocalService;
import com.liferay.calendar.service.CalendarResourceService;
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

import it.smc.calendar.caldav.helper.api.CalendarListService;
import it.smc.calendar.caldav.helper.util.PropsValues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author rge
 */
@Component(immediate = true, service = CalendarListService.class)
public class CalendarListServiceImpl implements CalendarListService {

	@Override
	public List<Calendar> getAllCalendars(PermissionChecker permissionChecker)
		throws PortalException {

		boolean useSessionClicksCalendars =
			PropsValues.PROPFIND_PROVIDE_SESSIONCLICKS_CALENDARS;
		boolean hidePersonalCalendar = PropsValues.HIDE_PERSONAL_CALENDAR;

		Set<Calendar> calendars = new HashSet<>();

		if (useSessionClicksCalendars) {
			if (!hidePersonalCalendar) {
				if (_log.isDebugEnabled()) {
					_log.debug("Get user personal calendars");
				}

				if (!permissionChecker.isSignedIn()) {
					_log.debug("Guest has no personal calendar");
				}
				else {
					List<Calendar> userCalendars = getUserCalendars(
						permissionChecker.getUserId());

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

				List<Calendar> userGroupCalendars = getUserGroupCalendars(
					permissionChecker);

				if (userGroupCalendars != null) {
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

			List<Calendar> selectedCalendars = getSelectedCalendars(
				permissionChecker);

			if (selectedCalendars != null) {
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

			List<Calendar> allCalendars = _calendarLocalService.getCalendars(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

			for (Calendar calendar : allCalendars) {
				try {
					if (_calendarModelResourcePermission.contains(
						permissionChecker, calendar, ActionKeys.VIEW)) {

						if (_log.isDebugEnabled()) {
							_log.debug(" - " + calendar.getName());
						}

						calendars.add(calendar);
					}
				}
				catch (PortalException pe) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							"Can not add calendar: " + calendar.getName(), pe);
					}
				}
			}
		}

		return new ArrayList<>(calendars);
	}

	public List<Calendar> getSelectedCalendars(
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
			Calendar calendar = _calendarLocalService.fetchCalendar(calendarId);

			if (calendar == null) {
				continue;
			}

			try {
				if (_calendarModelResourcePermission.contains(
					permissionChecker, calendarId, ActionKeys.VIEW)) {

					calendars.add(calendar);
				}
			}
			catch (PortalException pe) {
				if (_log.isWarnEnabled()) {
					_log.warn(
						"Can not add calendar: " + calendar.getName(), pe);
				}
			}
		}

		return calendars;
	}

	public List<Calendar> getUserCalendars(long userId) {
		long classNameId = PortalUtil.getClassNameId(User.class.getName());

		CalendarResource calendarResource = null;

		try {
			calendarResource = _calendarResourceService.fetchCalendarResource(
				classNameId, userId);

			if (calendarResource != null) {
				return calendarResource.getCalendars();
			}
		}
		catch (PortalException pe) {
			_log.error(pe, pe);
		}

		return Collections.emptyList();
	}

	public List<Calendar> getUserGroupCalendars(
			PermissionChecker permissionChecker)
		throws PortalException {

		List<Calendar> calendars = new ArrayList<>();

		long classNameId = PortalUtil.getClassNameId(Group.class.getName());

		List<CalendarResource> calendarResources =
			_calendarResourceService.search(
				permissionChecker.getCompanyId(), new long[0],
				new long[] {classNameId}, null, true, true, QueryUtil.ALL_POS,
				QueryUtil.ALL_POS, null);

		for (CalendarResource calendarResource : calendarResources) {
			for (Calendar calendar : calendarResource.getCalendars()) {
				try {
					if (_calendarModelResourcePermission.contains(
						permissionChecker, calendar, ActionKeys.VIEW)) {

						calendars.add(calendar);
					}
				}
				catch (PortalException pe) {
					if (_log.isWarnEnabled()) {
						_log.warn(
							"Can not add calendar: " + calendar.getName(), pe);
					}
				}
			}
		}

		return calendars;
	}

	@Reference(
		target = "(model.class.name=com.liferay.calendar.model.Calendar)",
		unbind = "-"
	)
	protected void setModelPermissionChecker(
		ModelResourcePermission<Calendar> modelResourcePermission) {

		_calendarModelResourcePermission = modelResourcePermission;
	}

	private static Log _log = LogFactoryUtil.getLog(
		CalendarListServiceImpl.class);

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private CalendarLocalService _calendarLocalService;

	private ModelResourcePermission<Calendar> _calendarModelResourcePermission;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private CalendarResourceService _calendarResourceService;

}