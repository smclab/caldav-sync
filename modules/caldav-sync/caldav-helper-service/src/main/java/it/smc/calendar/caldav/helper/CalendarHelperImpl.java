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
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;

import it.smc.calendar.caldav.helper.api.CalendarHelper;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

/**
 * @author Fabio Pezzutto
 */
@Component(immediate = true, service = CalendarHelper.class)
public class CalendarHelperImpl implements CalendarHelper {

	public Optional<User> getCalendarResourceUser(
		CalendarResource calendarResource) {

		if (!isCalendarResourceUserCalendar(calendarResource)) {
			return Optional.empty();
		}

		User user = UserLocalServiceUtil.fetchUser(
			calendarResource.getClassPK());

		if (user != null) {
			return Optional.of(user);
		}

		return Optional.empty();
	}

	public boolean isCalendarResourceUserCalendar(
		CalendarResource calendarResource) {

		return calendarResource.getClassName(
		).equals(
			User.class.getName()
		);
	}

	public boolean isCalendarUserCalendar(Calendar calendar) {
		try {
			return isCalendarResourceUserCalendar(
				calendar.getCalendarResource());
		}
		catch (PortalException pe) {
			_log.error(pe, pe);
		}

		return Boolean.FALSE;
	}

	private static Log _log = LogFactoryUtil.getLog(CalendarHelperImpl.class);

}