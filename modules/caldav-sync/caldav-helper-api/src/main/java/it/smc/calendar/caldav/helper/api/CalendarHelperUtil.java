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

package it.smc.calendar.caldav.helper.api;

import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.portal.kernel.model.User;

import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Fabio Pezzutto
 */
public class CalendarHelperUtil {

	public static Optional<User> getCalendarResourceUser(
		CalendarResource calendarResource) {

		return getService().getCalendarResourceUser(calendarResource);
	}

	public static CalendarHelper getService() {
		if (_serviceTracker == null) {
			Bundle bundle = FrameworkUtil.getBundle(CalendarHelper.class);

			_serviceTracker = new ServiceTracker<>(
				bundle.getBundleContext(), CalendarHelper.class, null);

			_serviceTracker.open();
		}

		return _serviceTracker.getService();
	}

	public static boolean isCalendarResourceUserCalendar(
		CalendarResource calendarResource) {

		return getService().isCalendarResourceUserCalendar(calendarResource);
	}

	public static boolean isCalendarUserCalendar(Calendar calendar) {
		return getService().isCalendarUserCalendar(calendar);
	}

	private static ServiceTracker<CalendarHelper, CalendarHelper>
		_serviceTracker;

}