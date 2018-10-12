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

package it.smc.calendar.caldav.util;

import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.CalendarBookingLocalServiceUtil;
import com.liferay.calendar.service.CalendarServiceUtil;
import com.liferay.calendar.service.permission.CalendarPermission;
import com.liferay.calendar.util.comparator.CalendarNameComparator;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.OrderFactoryUtil;
import com.liferay.portal.kernel.dao.orm.ProjectionFactoryUtil;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.workflow.WorkflowConstants;

import it.smc.calendar.caldav.helper.api.CalendarListService;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * @author Fabio Pezzutto
 */
@Component(immediate = true, service=CalendarUtil.class)
public class CalendarUtil {

	public static final String ACTION_VIEW_BOOKING_DETAILS =
		"VIEW_BOOKING_DETAILS";

	public static List<Calendar> getAllCalendars(
			PermissionChecker permissionChecker)
		throws PortalException {

		return _calendarListService.getAllCalendars(permissionChecker);
	}

	public static List<CalendarBooking> getCalendarBookings(
			PermissionChecker permissionChecker, Calendar calendar,
			Date startDate, Date endDate)
		throws PortalException {

		DynamicQuery dynamicQuery =
			CalendarBookingLocalServiceUtil.dynamicQuery();

		dynamicQuery.add(
			RestrictionsFactoryUtil.eq("calendarId", calendar.getCalendarId()));

		List<Integer> calendarStatus = new ArrayList<>();
		calendarStatus.add(WorkflowConstants.STATUS_APPROVED);
		calendarStatus.add(WorkflowConstants.STATUS_PENDING);
		//calendarStatus.add(WorkflowConstants.STATUS_SCHEDULED);

		dynamicQuery.add(RestrictionsFactoryUtil.in("status", calendarStatus));

		if (startDate != null) {
			dynamicQuery.add(
				RestrictionsFactoryUtil.ge(
					"startTime", Long.valueOf(startDate.getTime())));
		}

		if (endDate != null) {
			dynamicQuery.add(
				RestrictionsFactoryUtil.le(
					"endTime", Long.valueOf(endDate.getTime())));
		}

		List<CalendarBooking> allCalendarBookings =
			CalendarBookingLocalServiceUtil.dynamicQuery(dynamicQuery);

		return filterCalendarBookings(
			allCalendarBookings, permissionChecker, ActionKeys.VIEW);
	}

	public static List<Calendar> getCalendarResourceCalendars(
			CalendarResource calendarResource)
		throws PortalException {

		return CalendarServiceUtil.search(
			calendarResource.getCompanyId(),
			new long[] {calendarResource.getGroupId()},
			new long[] {calendarResource.getCalendarResourceId()}, null, false,
			QueryUtil.ALL_POS, QueryUtil.ALL_POS,
			new CalendarNameComparator(true));
	}

	public static Date getLastCalendarModifiedDate(long calendarId)
		throws PortalException {

		DynamicQuery dynamicQuery =
			CalendarBookingLocalServiceUtil.dynamicQuery();

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
		throws PortalException {

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
		throws PortalException {

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
		throws PortalException {

		CalendarResource calendarResource = calendar.getCalendarResource();

		if (calendarResource.getClassName().equals(User.class.getName()) &&
			(calendarResource.getClassPK() == userId)) {

			return true;
		}
		else {
			return false;
		}
	}

	@Reference(unbind ="-", policyOption= ReferencePolicyOption.GREEDY)
	protected void setCalendarListService(
		CalendarListService calendarListService) {

		_calendarListService = calendarListService;
	}

	private static CalendarListService _calendarListService;

}