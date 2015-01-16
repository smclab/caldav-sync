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

package it.smc.calendar.sync.calendar.util;

import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.CalendarBookingLocalServiceUtil;
import com.liferay.calendar.service.CalendarLocalServiceUtil;
import com.liferay.calendar.service.CalendarServiceUtil;
import com.liferay.calendar.service.permission.CalendarPermission;
import com.liferay.calendar.util.comparator.CalendarNameComparator;
import com.liferay.compat.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.OrderFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.ClassLoaderPool;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.util.SessionClicks;
import com.liferay.portlet.PortletPreferencesFactoryUtil;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
public class CalendarUtil {

	public static final String ACTION_VIEW_BOOKING_DETAILS =
		"VIEW_BOOKING_DETAILS";

	public static List<Calendar> getAllCalendars(
			PermissionChecker permissionChecker)
		throws PortalException, SystemException {

		List<Calendar> calendars = new ArrayList<Calendar>();

		List<Calendar> allCalendars =
			CalendarLocalServiceUtil.getCalendars(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		calendars = new ArrayList<Calendar>();

		long[] calendarIds = GetterUtil.getLongValues(
			StringUtil.split(
				PortletPreferencesFactoryUtil.getPortalPreferences(
					permissionChecker.getUserId(), true).getValue(
						SessionClicks.class.getName(), "otherCalendars")));

		for (Calendar calendar : allCalendars) {
			if (CalendarPermission.contains(
					permissionChecker, calendar, ActionKeys.VIEW)) {

				if (PortletPropsValues.
						PROPFIND_PROVIDE_SESSIONCLICKS_CALENDARS) {

					if (!ArrayUtil.contains(
							calendarIds, calendar.getCalendarId()) &&
						!isCurrentUserCalendar(
							permissionChecker.getUserId(), calendar)) {

						continue;
					}
				}

				calendars.add(calendar);
			}
		}

		return calendars;
	}

	public static List<CalendarBooking> getCalendarBookings(
			PermissionChecker permissionChecker, Calendar calendar,
			Date startDate, Date endDate)
		throws PortalException, SystemException {

		ClassLoader classLoader = ClassLoaderPool.getClassLoader(
			"calendar-portlet");

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CalendarBooking.class, classLoader);

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("calendarId", calendar.getCalendarId()));

		List<Integer> calendarStatus = new ArrayList<Integer>();
		calendarStatus.add(WorkflowConstants.STATUS_APPROVED);
		calendarStatus.add(WorkflowConstants.STATUS_PENDING);
		calendarStatus.add(WorkflowConstants.STATUS_DRAFT_FROM_APPROVED);

		dynamicQuery.add(RestrictionsFactoryUtil.in("status", calendarStatus));

		if (startDate != null) {
			dynamicQuery.add(
				RestrictionsFactoryUtil.ge(
					"startTime", new Long(startDate.getTime())));
		}

		if (endDate != null) {
			dynamicQuery.add(
				RestrictionsFactoryUtil.le(
					"endTime", new Long(endDate.getTime())));
		}

		List<CalendarBooking> allCalendarBookings =
			CalendarBookingLocalServiceUtil.dynamicQuery(dynamicQuery);

		return filterCalendarBookings(
			allCalendarBookings, permissionChecker, ActionKeys.VIEW);
	}

	public static List<Calendar> getCalendarResourceCalendars(
			CalendarResource calendarResource)
		throws PortalException, SystemException {

		return CalendarServiceUtil.search(
			calendarResource.getCompanyId(),
			new long[] {calendarResource.getGroupId()},
			new long[] {calendarResource.getCalendarResourceId()}, null,
			false, QueryUtil.ALL_POS, QueryUtil.ALL_POS,
			new CalendarNameComparator(true));
	}

	public static Date getLastCalendarModifiedDate(long calendarId)
		throws PortalException, SystemException {

		ClassLoader classLoader = ClassLoaderPool.getClassLoader(
			"calendar-portlet");

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CalendarBooking.class, classLoader);

		dynamicQuery.add(RestrictionsFactoryUtil.eq("calendarId", calendarId));

		dynamicQuery.setProjection(
			ProjectionFactoryUtil.property("modifiedDate"));

		dynamicQuery.setLimit(0, 1);

		dynamicQuery.addOrder(OrderFactoryUtil.desc("modifiedDate"));

		List<Object> lastModifiedDate =
			CalendarBookingLocalServiceUtil.dynamicQuery(dynamicQuery);

		if ((lastModifiedDate != null) && !lastModifiedDate.isEmpty()) {
			Timestamp ts = (Timestamp)lastModifiedDate.get(0);
			return new Date(ts.getTime());
		}

		return new Date();
	}

	protected static CalendarBooking filterCalendarBooking(
			CalendarBooking calendarBooking,
			PermissionChecker permissionChecker)
		throws PortalException, SystemException {

		if (!CalendarPermission.contains(
				permissionChecker, calendarBooking.getCalendarId(),
				ACTION_VIEW_BOOKING_DETAILS)) {

			calendarBooking.setTitle(StringPool.BLANK);
			calendarBooking.setDescription(StringPool.BLANK);
			calendarBooking.setLocation(StringPool.BLANK);
		}

		return calendarBooking;
	}

	protected static List<CalendarBooking> filterCalendarBookings(
			List<CalendarBooking> calendarBookings,
			PermissionChecker permissionChecker, String actionId)
		throws PortalException, SystemException {

		calendarBookings = ListUtil.copy(calendarBookings);

		Iterator<CalendarBooking> itr = calendarBookings.iterator();

		while (itr.hasNext()) {
			CalendarBooking calendarBooking = itr.next();

			if (!CalendarPermission.contains(
					permissionChecker, calendarBooking.getCalendarId(),
					ACTION_VIEW_BOOKING_DETAILS)) {

				if (!CalendarPermission.contains(
						permissionChecker, calendarBooking.getCalendarId(),
						actionId)) {

					itr.remove();
				}
				else {
					filterCalendarBooking(calendarBooking, permissionChecker);
				}
			}
		}

		return calendarBookings;
	}

	protected static boolean isCurrentUserCalendar(
			long userId, Calendar calendar)
		throws PortalException, SystemException {

		CalendarResource calendarResource = calendar.getCalendarResource();

		if (calendarResource.getClassName().equals(User.class.getName()) &&
			(calendarResource.getClassPK() == userId) ) {

			return true;
		}
		else {
			return false;
		}
	}

}