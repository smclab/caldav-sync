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

package it.smc.calendar.sync.internal;

import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.CalendarBookingLocalServiceUtil;
import com.liferay.calendar.service.permission.CalendarPermission;
import com.liferay.portal.kernel.io.unsync.UnsyncStringReader;
import com.liferay.portal.kernel.io.unsync.UnsyncStringWriter;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.sanitizer.SanitizerException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;

import java.net.URI;

import java.util.ArrayList;
import java.util.List;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;

public class ICSSanitizer {

	public ICSSanitizer(Calendar calendar) {
		_calendar = calendar;
	}

	@SuppressWarnings("unchecked")	
	public String sanitize(String ics) throws SanitizerException {
		try {
			CalendarBuilder calendarBuilder = new CalendarBuilder();

			UnsyncStringReader unsyncStringReader = new UnsyncStringReader(ics);

			net.fortuna.ical4j.model.Calendar iCalCalendar = calendarBuilder.build(unsyncStringReader);

			List<VEvent> vEvents = iCalCalendar.getComponents(Component.VEVENT);

			for (VEvent vEvent : vEvents) {
				if (vEvent.getAlarms().size() > 0) {
					updateAlarmAttendeers(vEvent);
				}
			}

			return toString(iCalCalendar);
		} catch (Exception e) {
			throw new SanitizerException(e);
		}
	}

	protected String toString(net.fortuna.ical4j.model.Calendar iCalCalendar) throws Exception {

		CalendarOutputter calendarOutputter = new CalendarOutputter(false);

		ComponentList componentList = iCalCalendar.getComponents();

		if (componentList.isEmpty()) {
			calendarOutputter.setValidating(false);
		}

		UnsyncStringWriter unsyncStringWriter = new UnsyncStringWriter();

		calendarOutputter.output(iCalCalendar, unsyncStringWriter);

		unsyncStringWriter.flush();

		return unsyncStringWriter.toString();
	}

	protected void updateAlarmAttendeers(VEvent vEvent) throws Exception {

		ComponentList vAlarms = (ComponentList) vEvent.getAlarms();
		// String calendarBookingUuid = vEvent.getProperty(Uid.UID).getValue();
		/*
		 * List<CalendarBooking> calendarBooking =
		 * CalendarBookingLocalServiceUtil.
		 * getCalendarBookingsByUuidAndCompanyId( calendarBookingUuid,
		 * _calendar.getCompanyId());
		 */

		VAlarm vAlarm;
		PropertyList propertyList;

		for (int i = 0; i < vAlarms.size(); i++) {
			vAlarm = (VAlarm) vAlarms.get(i);
			propertyList = vAlarm.getProperties();

			if (!vAlarm.getAction().equals(Action.EMAIL)) {
				propertyList.remove(propertyList.getProperty(Action.ACTION));
				propertyList.add(Action.EMAIL);

				if ((vAlarm.getSummary() == null) && (vAlarm.getDescription() != null)) {

					String description = vAlarm.getDescription().getValue();
					propertyList.add(new Summary(description));
				}

				List<String> recipients = _getNotificationRecipients();

				URI uri;
				Attendee attendee;

				for (String recipient : recipients) {
					uri = URI.create("mailto:".concat(recipient));
					attendee = new Attendee(uri);
					propertyList.add(attendee);
				}
			}
		}
	}

	private List<String> _getNotificationRecipients() throws Exception {
		CalendarResource calendarResource = _calendar.getCalendarResource();

		List<Role> roles = RoleLocalServiceUtil.getResourceBlockRoles(_calendar.getResourceBlockId(),
				Calendar.class.getName(), "MANAGE_BOOKINGS");

		List<String> notificationRecipients = new ArrayList<String>();

		for (Role role : roles) {
			String name = role.getName();

			if (name.equals(RoleConstants.OWNER)) {
				User calendarResourceUser = UserLocalServiceUtil.getUser(calendarResource.getUserId());

				notificationRecipients.add(calendarResourceUser.getEmailAddress());

				User calendarUser = UserLocalServiceUtil.getUser(_calendar.getUserId());

				if (calendarResourceUser.getUserId() != calendarUser.getUserId()) {

					notificationRecipients.add(calendarUser.getEmailAddress());
				}
			} else {
				List<User> roleUsers = UserLocalServiceUtil.getRoleUsers(role.getRoleId());

				for (User roleUser : roleUsers) {
					PermissionChecker permissionChecker = PermissionCheckerFactoryUtil.create(roleUser);

					if (!CalendarPermission.contains(permissionChecker, _calendar, "MANAGE_BOOKINGS")) {

						continue;
					}

					notificationRecipients.add(roleUser.getEmailAddress());
				}
			}
		}

		return notificationRecipients;
	}

	private Calendar _calendar;

}