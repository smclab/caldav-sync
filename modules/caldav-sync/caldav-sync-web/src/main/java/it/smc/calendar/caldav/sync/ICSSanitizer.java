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
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.CalendarBookingLocalServiceUtil;
import com.liferay.calendar.service.permission.CalendarPermission;
import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.io.unsync.UnsyncStringReader;
import com.liferay.portal.kernel.io.unsync.UnsyncStringWriter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Role;
import com.liferay.portal.kernel.model.RoleConstants;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.sanitizer.SanitizerException;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionThreadLocal;
import com.liferay.portal.kernel.service.RoleLocalServiceUtil;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import it.smc.calendar.caldav.helper.util.PropsValues;
import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Summary;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabio Pezzutto
 */
public class ICSSanitizer {

	public static String sanitizeDownloadICS(
			String ics, CalendarBooking calendarBooking)
		throws SanitizerException {

		try {
			long userId = PrincipalThreadLocal.getUserId();

			if (userId == 0) {
				userId =
					PermissionThreadLocal.getPermissionChecker().getUserId();
			}

			//TODO: find a better way to do it
			ics = ics.replaceAll("TRIGGER:PT", "TRIGGER:PT-");

			net.fortuna.ical4j.model.Calendar iCalCalendar = getICalendar(ics);

			ComponentList components = iCalCalendar.getComponents(
				Component.VEVENT);

			for (Object component : components) {
				if (component instanceof VEvent) {
					VEvent vEvent = (VEvent)component;

					if (vEvent.getAlarms().size() > 0) {
						updateAlarmActions(vEvent, userId);
					}


					updateICSExternalAttendees(vEvent, calendarBooking);
				}
			}

			return iCalCalendar.toString();
		}
		catch (Exception e) {
			throw new SanitizerException(e);
		}
	}

	public static String sanitizeUploadedICS(String ics, Calendar calendar)
		throws SanitizerException {

		try {
			/*
			ics = ics.replaceAll("\r\n", "_TMP_REPLACE_");
			ics = ics.replaceAll(";EMAIL=\"(.*)\"", "");
			ics = ics.replaceAll("EMAIL=\"(.*);\"", "");
			ics = ics.replaceAll("EMAIL=\"(.*):\"", ":");
			ics = ics.replaceAll("_TMP_REPLACE_", "\r\n");
			*/

			net.fortuna.ical4j.model.Calendar iCalCalendar = getICalendar(ics);

			ComponentList components = iCalCalendar.getComponents(
				Component.VEVENT);

			for (Object component : components) {
				if (component instanceof VEvent) {
					VEvent vEvent = (VEvent)component;

					if (vEvent.getAlarms().size() > 0) {
						updateAlarmAttendeers(vEvent, calendar);
					}
				}
			}

			return iCalCalendar.toString();
		}
		catch (Exception e) {
			throw new SanitizerException(e);
		}
	}

	public static void updateBookingExternalAttendees(
			String ics, Calendar calendar)
		throws SanitizerException {

		try {
			net.fortuna.ical4j.model.Calendar iCalCalendar = getICalendar(ics);

			ComponentList components = iCalCalendar.getComponents(
				Component.VEVENT);

			for (Object component : components) {
				if (component instanceof VEvent) {
					VEvent vEvent = (VEvent)component;

					PropertyList attendees = vEvent.getProperties(
						Property.ATTENDEE);

					String bookingUuid = vEvent.getUid().getValue();

					CalendarBooking calendarBooking =
						CalendarBookingLocalServiceUtil.fetchCalendarBooking(
							calendar.getCalendarId(), bookingUuid);

					if (calendarBooking != null) {
						updateBookingAttendees(calendarBooking, attendees);
					}
				}
			}
		}
		catch (Exception e) {
			throw new SanitizerException(e);
		}
	}

	protected static net.fortuna.ical4j.model.Calendar getICalendar(String ics)
		throws IOException, ParserException {

		CalendarBuilder calendarBuilder = new CalendarBuilder();

		UnsyncStringReader unsyncStringReader = new UnsyncStringReader(ics);

		net.fortuna.ical4j.model.Calendar iCalCalendar = calendarBuilder.build(
			unsyncStringReader);

		return iCalCalendar;
	}

	protected static String toString(
			net.fortuna.ical4j.model.Calendar iCalCalendar)
		throws Exception {

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

	protected static void updateAlarmActions(VEvent vEvent, long userId)
		throws Exception {

		User user = UserLocalServiceUtil.getUser(userId);

		String currentUserEmail = StringUtil.toLowerCase(
			user.getEmailAddress());

		ComponentList vAlarms = (ComponentList)vEvent.getAlarms();

		VAlarm vAlarm;
		PropertyList propertyList;

		for (int i = 0; i < vAlarms.size(); i++) {
			vAlarm = (VAlarm)vAlarms.get(i);
			propertyList = vAlarm.getProperties();

			PropertyList alarmAttendees = propertyList.getProperties(
				Property.ATTENDEE);

			boolean currentUserIdAttendee = false;

			Iterator<Attendee> alarmAttendeesIterator =
				alarmAttendees.iterator();

			while (alarmAttendeesIterator.hasNext()) {
				Attendee attendee = alarmAttendeesIterator.next();

				String attendeeData = StringUtil.toLowerCase(
					attendee.getValue());

				if (attendeeData.contains(currentUserEmail)) {
					currentUserIdAttendee = true;
					break;
				}
			}

			HttpServletRequest request =
				ServiceContextThreadLocal.getServiceContext().getRequest();

			if (!currentUserIdAttendee) {
				propertyList.remove(propertyList.getProperty(Action.ACTION));
			}
			else if (vAlarm.getAction().equals(Action.EMAIL) &&
					 !CalDAVUtil.isThunderbird(request)) {

				propertyList.remove(propertyList.getProperty(Action.ACTION));
				propertyList.add(Action.DISPLAY);
			}
		}
	}

	protected static void updateAlarmAttendeers(
			VEvent vEvent, Calendar calendar)
		throws Exception {

		ComponentList vAlarms = (ComponentList)vEvent.getAlarms();

		VAlarm vAlarm;
		PropertyList propertyList;

		for (int i = 0; i < vAlarms.size(); i++) {
			vAlarm = (VAlarm)vAlarms.get(i);
			propertyList = vAlarm.getProperties();

			if (!vAlarm.getAction().equals(Action.EMAIL)) {
				propertyList.remove(propertyList.getProperty(Action.ACTION));
				propertyList.add(Action.EMAIL);

				if ((vAlarm.getSummary() == null) &&
					(vAlarm.getDescription() != null)) {

					String description = vAlarm.getDescription().getValue();
					propertyList.add(new Summary(description));
				}

				List<String> recipients = _getNotificationRecipients(calendar);

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

	protected static void updateBookingAttendees(
			CalendarBooking calendarBooking, PropertyList attendeeList)
		throws PortalException {

		ExpandoBridge calendarBookingExpando =
			calendarBooking.getExpandoBridge();

		String invitedUsersCustomFieldName =
			PropsValues.INVITED_USERS_CUSTOM_FIELD_NAME;

		String invitedUsersLabelCustomFieldName =
			PropsValues.INVITED_USERS_LABEL_CUSTOM_FIELD_NAME;

		if (Validator.isNull(invitedUsersCustomFieldName)) {
			return;
		}

		if (!calendarBookingExpando.hasAttribute(
				invitedUsersCustomFieldName)) {

			calendarBookingExpando.addAttribute(
				invitedUsersCustomFieldName,
				ExpandoColumnConstants.STRING_ARRAY, new String[]{},
				Boolean.FALSE);

			UnicodeProperties hiddenProperties = new UnicodeProperties();

			hiddenProperties.setProperty(
				ExpandoColumnConstants.PROPERTY_HIDDEN,
				String.valueOf(Boolean.TRUE));

			calendarBookingExpando.setAttributeProperties(
				invitedUsersCustomFieldName, hiddenProperties);
		}

		if (Validator.isNotNull(invitedUsersLabelCustomFieldName) &&
			!calendarBookingExpando.hasAttribute(
				invitedUsersLabelCustomFieldName)) {

			calendarBookingExpando.addAttribute(
				invitedUsersLabelCustomFieldName,
				ExpandoColumnConstants.STRING_ARRAY, new String[]{},
				Boolean.FALSE);
		}

		Iterator iterator = attendeeList.iterator();
		String[] attendees = new String[0];
		String[] attendeesEmailAddresses = new String[0];

		while (iterator.hasNext()) {
			Attendee attendee = (Attendee)iterator.next();

			if (Validator.isNotNull(attendee.getValue())) {
				String attendeeEmail = StringUtil.replace(
					attendee.getValue().toLowerCase(), "mailto:",
					StringPool.BLANK);

				if (!Validator.isEmailAddress(attendeeEmail)) {
					continue;
				}

				User user = UserLocalServiceUtil.fetchUserByEmailAddress(
					calendarBooking.getCompanyId(), attendeeEmail);

				if (user == null) {
					attendees = ArrayUtil.append(
						attendees, attendee.toString());

					attendeesEmailAddresses = ArrayUtil.append(
						attendeesEmailAddresses, attendeeEmail);
				}
			}
		}

		if (!ArrayUtil.isEmpty(attendees)) {
			calendarBookingExpando.setAttribute(
				invitedUsersCustomFieldName, attendees, Boolean.FALSE);
		}

		if (Validator.isNotNull(invitedUsersLabelCustomFieldName) &&
			!ArrayUtil.isEmpty(attendeesEmailAddresses)) {
			calendarBookingExpando.setAttribute(
				invitedUsersLabelCustomFieldName, attendeesEmailAddresses,
				Boolean.FALSE);
		}
	}

	protected static void updateICSExternalAttendees(
			VEvent vEvent, CalendarBooking calendarBooking)
		throws SanitizerException {

		ExpandoBridge calendarBookingExpando =
			calendarBooking.getExpandoBridge();

		String invitedUsersCustomFieldName =
			PropsValues.INVITED_USERS_CUSTOM_FIELD_NAME;

		if (Validator.isNull(invitedUsersCustomFieldName)) {
			return;
		}

		String[] attendees = GetterUtil.getStringValues(
			calendarBookingExpando.getAttribute(
				invitedUsersCustomFieldName, Boolean.FALSE));

		if (ArrayUtil.isEmpty(attendees)) {
			return;
		}

		PropertyList propertyList = vEvent.getProperties();

		Attendee attendee;

		for (String attendeeString : attendees) {
			attendee = _toICalAttendee(attendeeString);
			propertyList.add(attendee);
		}
	}

	private static List<String> _getNotificationRecipients(Calendar calendar)
		throws Exception {

		CalendarResource calendarResource = calendar.getCalendarResource();

		List<Role> roles = RoleLocalServiceUtil.getResourceBlockRoles(
			calendar.getResourceBlockId(), Calendar.class.getName(),
			"MANAGE_BOOKINGS");

		List<String> notificationRecipients = new ArrayList<>();

		for (Role role : roles) {
			String name = role.getName();

			if (name.equals(RoleConstants.OWNER)) {
				User calendarResourceUser = UserLocalServiceUtil.getUser(
					calendarResource.getUserId());

				notificationRecipients.add(
					calendarResourceUser.getEmailAddress());

				User calendarUser = UserLocalServiceUtil.getUser(
					calendar.getUserId());

				if (calendarResourceUser.getUserId() !=
						calendarUser.getUserId()) {

					notificationRecipients.add(calendarUser.getEmailAddress());
				}
			}
			else {
				List<User> roleUsers = UserLocalServiceUtil.getRoleUsers(
					role.getRoleId());

				for (User roleUser : roleUsers) {
					PermissionChecker permissionChecker =
						PermissionCheckerFactoryUtil.create(roleUser);

					if (!CalendarPermission.contains(
							permissionChecker, calendar, "MANAGE_BOOKINGS")) {

						continue;
					}

					notificationRecipients.add(roleUser.getEmailAddress());
				}
			}
		}

		return notificationRecipients;
	}

	private static Attendee _toICalAttendee(String attendeeString)
		throws SanitizerException {

		StringBuilder sb = new StringBuilder(9);
		sb.append("BEGIN:VCALENDAR");
		sb.append(StringPool.NEW_LINE);
		sb.append("BEGIN:VEVENT");
		sb.append(StringPool.NEW_LINE);
		sb.append(attendeeString);
		sb.append(StringPool.NEW_LINE);
		sb.append("END:VEVENT");
		sb.append(StringPool.NEW_LINE);
		sb.append("END:VCALENDAR");

		CalendarBuilder calendarBuilder = new CalendarBuilder();

		UnsyncStringReader unsyncStringReader = new UnsyncStringReader(
			sb.toString());

		Attendee attendee = new Attendee();

		try {
			net.fortuna.ical4j.model.Calendar iCalCalendar =
				calendarBuilder.build(unsyncStringReader);

			Component vEvent = iCalCalendar.getComponent(VEvent.VEVENT);
			Property attendeeProperty = vEvent.getProperty(Attendee.ATTENDEE);

			attendee.setValue(attendeeProperty.getValue());

			Iterator<Parameter> iterator =
				attendeeProperty.getParameters().iterator();

			while (iterator.hasNext()) {
				Parameter parameter = iterator.next();

				attendee.getParameters().add(parameter);
			}
		}
		catch (Exception e) {
			_log.error("Error parsing attendee " + attendeeString, e);
			throw new SanitizerException(e);
		}

		/*

		URI uri = URI.create("mailto:".concat(emailAddress));

		attendee.setCalAddress(uri);

		Cn cn = new Cn(emailAddress);

		ParameterList parameters = attendee.getParameters();

		parameters.add(cn);
		parameters.add(CuType.INDIVIDUAL);
		parameters.add(net.fortuna.ical4j.model.parameter.Role.REQ_PARTICIPANT);
		parameters.add(Rsvp.TRUE);

		if (status == WorkflowConstants.STATUS_APPROVED) {
			parameters.add(PartStat.ACCEPTED);
		}
		else {
			parameters.add(PartStat.NEEDS_ACTION);
		}
		*/

		return attendee;
	}

	private static Log _log = LogFactoryUtil.getLog(ICSSanitizer.class);

}