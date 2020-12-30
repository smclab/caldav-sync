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

/**
 * @author Fabio Pezzutto
 */
public interface CalendarHelper {

	public Optional<User> getCalendarResourceUser(
		CalendarResource calendarResource);

	public boolean isCalendarResourceUserCalendar(
		CalendarResource calendarResource);

	public boolean isCalendarUserCalendar(Calendar calendar);

}