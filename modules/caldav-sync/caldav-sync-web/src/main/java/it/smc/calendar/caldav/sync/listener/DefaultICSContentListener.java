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

package it.smc.calendar.caldav.sync.listener;

import com.liferay.calendar.constants.CalendarActionKeys;
import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.CalendarBookingLocalServiceUtil;
import com.liferay.calendar.service.permission.CalendarPermission;
import com.liferay.calendar.workflow.CalendarBookingWorkflowConstants;
import com.liferay.expando.kernel.model.ExpandoBridge;
import com.liferay.expando.kernel.model.ExpandoColumnConstants;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.io.unsync.UnsyncStringReader;
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
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.service.ServiceContextThreadLocal;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.util.ArrayUtil;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.UnicodeProperties;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import it.smc.calendar.caldav.helper.api.CalendarHelperUtil;
import it.smc.calendar.caldav.helper.util.PropsValues;
import it.smc.calendar.caldav.sync.ical.util.AttendeeUtil;
import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Transp;
import org.osgi.service.component.annotations.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * @author Fabio Pezzutto
 */
@Component(
	immediate = true,
	service = ICSImportExportListener.class
)
public class DefaultICSContentListener implements ICSImportExportListener {

	public void afterContentExported(
			String ics, CalendarBooking calendarBooking)
		throws SanitizerException {
	}

	public void afterContentImported(String ics, Calendar calendar)
		throws SanitizerException {

		try {
			net.fortuna.ical4j.model.Calendar iCalCalendar = getICalendar(ics);

			ComponentList components = iCalCalendar.getComponents(
				net.fortuna.ical4j.model.Component.VEVENT);

			for (Object component : components) {
				if (component instanceof VEvent) {
					VEvent vEvent = (VEvent)component;

					String bookingUuid = vEvent.getUid().getValue();

					CalendarBooking calendarBooking =
						CalendarBookingLocalServiceUtil.fetchCalendarBooking(
							calendar.getCalendarId(), bookingUuid);

					if (calendarBooking != null) {
						updateBookingAttendees(calendarBooking, vEvent);
					}
				}
			}
		}
		catch (Exception e) {
			throw new SanitizerException(e);
		}
	}

	public String beforeContentExported(
			String ics, CalendarBooking calendarBooking)
		throws SanitizerException {

		try {
			long userId = PrincipalThreadLocal.getUserId();

			if (userId == 0) {
				userId =
					PermissionThreadLocal.getPermissionChecker().getUserId();
			}

			User currentUser = UserLocalServiceUtil.getUser(userId);

			//TODO: find a better way to do it
			ics = ics.replaceAll("TRIGGER:PT", "TRIGGER:PT-");

			net.fortuna.ical4j.model.Calendar iCalCalendar = getICalendar(ics);

			ComponentList components = iCalCalendar.getComponents(
				net.fortuna.ical4j.model.Component.VEVENT);

			for (Object component : components) {
				if (component instanceof VEvent) {
					VEvent vEvent = (VEvent)component;

					if (vEvent.getAlarms().size() > 0) {
						updateAlarmActions(vEvent, userId);
					}

					updateICSExternalAttendees(vEvent, calendarBooking);

					updateDowloadedInvitations(
						currentUser, iCalCalendar, vEvent, calendarBooking);

					DateTime modifiedDate = new DateTime(
						calendarBooking.getModifiedDate().getTime());
					LastModified lastModified = new LastModified(modifiedDate);
					lastModified.setUtc(true);
					vEvent.getProperties().add(lastModified);

					DateTime createdDate = new DateTime(
						calendarBooking.getCreateDate().getTime());
					Created created = new Created(createdDate);
					created.setUtc(true);
					vEvent.getProperties().add(created);
				}
			}

			if (_log.isDebugEnabled()) {
				_log.debug("Calendar exported: " + iCalCalendar.toString());
			}

			return iCalCalendar.toString();
		}
		catch (Exception e) {
			throw new SanitizerException(e);
		}
	}

	public String beforeContentImported(String ics, Calendar calendar)
		throws SanitizerException {

		try {
			net.fortuna.ical4j.model.Calendar iCalCalendar = getICalendar(ics);

			ComponentList components = iCalCalendar.getComponents(
				net.fortuna.ical4j.model.Component.VEVENT);

			for (Object component : components) {
				if (component instanceof VEvent) {
					VEvent vEvent = (VEvent)component;

					if (vEvent.getAlarms().size() > 0) {
						updateAlarmAttendeers(vEvent, calendar);
					}
				}
			}

			if (_log.isDebugEnabled()) {
				_log.debug("Calendar imported: " + iCalCalendar.toString());
			}

			return iCalCalendar.toString();
		}
		catch (Exception e) {
			throw new SanitizerException(e);
		}
	}

	protected User getCalendarBookingUser(CalendarBooking calendarBooking)
		throws PortalException {

		CalendarResource calendarResource =
			calendarBooking.getCalendarResource();

		Optional<User> user = CalendarHelperUtil.getCalendarResourceUser(
			calendarResource);

		if (!user.isPresent()) {
			return null;
		}

		return user.get();
	}

	protected net.fortuna.ical4j.model.Calendar getICalendar(String ics)
		throws IOException, ParserException {

		CalendarBuilder calendarBuilder = new CalendarBuilder();

		UnsyncStringReader unsyncStringReader = new UnsyncStringReader(ics);

		net.fortuna.ical4j.model.Calendar iCalCalendar = calendarBuilder.build(
			unsyncStringReader);

		return iCalCalendar;
	}

	protected void updateAlarmActions(VEvent vEvent, long userId)
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

	protected void updateAlarmAttendeers(
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

	protected void updateBookingAttendees(
		CalendarBooking calendarBooking, VEvent vEvent)
		throws PortalException {

		PropertyList attendeeList = vEvent.getProperties(
			Property.ATTENDEE);

		Iterator iterator = attendeeList.iterator();
		String[] attendees = new String[0];
		String[] attendeesEmailAddresses = new String[0];

		while (iterator.hasNext()) {
			Attendee attendee = (Attendee)iterator.next();

			if (Validator.isNotNull(attendee.getValue())) {
				String attendeeEmail = StringUtil.replace(
					StringUtil.toLowerCase(attendee.getValue()), "mailto:",
					StringPool.BLANK);

				if (!Validator.isEmailAddress(attendeeEmail)) {
					continue;
				}

				User user = UserLocalServiceUtil.fetchUserByEmailAddress(
					calendarBooking.getCompanyId(), attendeeEmail);

				Optional<User> bookingUser =
					CalendarHelperUtil.getCalendarResourceUser(
						calendarBooking.getCalendarResource());

				if (user == null) {
					attendees = ArrayUtil.append(
						attendees, attendee.toString());

					attendeesEmailAddresses = ArrayUtil.append(
						attendeesEmailAddresses, attendeeEmail);
				}
				else if (bookingUser.isPresent() &&
					user.equals(bookingUser.get())) {

					int status = AttendeeUtil.getStatus(
						attendee, calendarBooking.getStatus());

					if (status != calendarBooking.getStatus()) {
						ServiceContext serviceContext =
							ServiceContextThreadLocal.getServiceContext();

						CalendarBookingLocalServiceUtil.updateStatus(
							user.getUserId(), calendarBooking, status,
							serviceContext);

						// TODO: update parent modified date, it shouldn't be
						// necessary

						LastModified iCalLastModified =
							(LastModified)vEvent.getProperty(
								LastModified.LAST_MODIFIED);

						Date modifiedDate = new Date();

						if (iCalLastModified != null) {
							modifiedDate = iCalLastModified.getDate();
						}

						_updateAllBookingModifiedDate(
							calendarBooking, modifiedDate);
					}
				}
			}
		}

		ExpandoBridge calendarBookingExpando =
			calendarBooking.getExpandoBridge();

		String invitedUsersCustomFieldName =
			PropsValues.INVITED_USERS_CUSTOM_FIELD_NAME;

		String invitedUsersLabelCustomFieldName =
			PropsValues.INVITED_USERS_LABEL_CUSTOM_FIELD_NAME;

		if (Validator.isNull(invitedUsersCustomFieldName)) {
			return;
		}

		if (!calendarBookingExpando.hasAttribute(invitedUsersCustomFieldName)) {
			calendarBookingExpando.addAttribute(
				invitedUsersCustomFieldName,
				ExpandoColumnConstants.STRING_ARRAY, new String[0],
				Boolean.FALSE);

			UnicodeProperties hiddenProperties = new UnicodeProperties();

			hiddenProperties.setProperty(
				ExpandoColumnConstants.PROPERTY_HIDDEN,
				String.valueOf(Boolean.TRUE));

			calendarBookingExpando.setAttributeProperties(
				invitedUsersCustomFieldName, hiddenProperties, Boolean.FALSE);
		}

		if (Validator.isNotNull(invitedUsersLabelCustomFieldName) &&
			!calendarBookingExpando.hasAttribute(
				invitedUsersLabelCustomFieldName)) {

			calendarBookingExpando.addAttribute(
				invitedUsersLabelCustomFieldName,
				ExpandoColumnConstants.STRING_ARRAY, new String[0],
				Boolean.FALSE);
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

	protected void updateDowloadedInvitations(
			User user, net.fortuna.ical4j.model.Calendar iCalCalendar,
			VEvent vEvent, CalendarBooking calendarBooking)
		throws PortalException {

		CalendarBooking parentBooking =
			calendarBooking.getParentCalendarBooking();

		// set organizer

		User userOrganizer = getCalendarBookingUser(parentBooking);

		if (userOrganizer != null) {
			URI uri = URI.create(
				"mailto:".concat(userOrganizer.getEmailAddress()));

			Organizer organizer = new Organizer(uri);
			organizer.getParameters().add(
				new Cn(userOrganizer.getFullName()));

			vEvent.getProperties().add(organizer);

			Attendee organizerAttendee = AttendeeUtil.create(
				userOrganizer.getEmailAddress(), userOrganizer.getFullName(),
				false,
				CalendarBookingWorkflowConstants.STATUS_APPROVED);

			vEvent.getProperties().add(organizerAttendee);
		}

		vEvent.getProperties().add(Transp.OPAQUE);

		boolean hasUpdatePermissions = CalendarPermission.contains(
			PermissionThreadLocal.getPermissionChecker(),
			calendarBooking.getCalendarId(),
			CalendarActionKeys.MANAGE_BOOKINGS);

		User bookingUser = getCalendarBookingUser(calendarBooking);

		// set event status

		boolean bookingPending = calendarBooking.getStatus() ==
			WorkflowConstants.STATUS_PENDING;

		if (_log.isDebugEnabled()) {
			if (bookingUser != null) {
				_log.debug(
					"Booking user for booking " +
					calendarBooking.getCalendarBookingId() + " is " +
					bookingUser.getScreenName());
			}
			else {
				_log.debug(
					"No booking user for booking " +
					calendarBooking.getCalendarBookingId());
			}

			if (userOrganizer != null) {
				_log.debug(
					"User organizer is " +  userOrganizer.getScreenName());
			}
		}

		Property eventStatus = vEvent.getProperty(Status.STATUS);

		if (eventStatus != null) {
			vEvent.getProperties().remove(eventStatus);
		}

		if (bookingPending) {
			vEvent.getProperties().add(Status.VEVENT_TENTATIVE);
		}
		else {
			vEvent.getProperties().add(Status.VEVENT_CONFIRMED);
		}

		// update attendees status

		List<CalendarBooking> childCalendarBookings =
			calendarBooking.getParentCalendarBooking()
				.getChildCalendarBookings();

		List<Attendee> attendees = new ArrayList<>();
		List<String> attendeesEmails = new ArrayList<>();
		Iterator<Attendee> attendeesIterator = vEvent.getProperties(
			Attendee.ATTENDEE).iterator();

		while (attendeesIterator.hasNext()) {
			attendees.add(attendeesIterator.next());
		}

		for (CalendarBooking childCalendarBooking : childCalendarBookings) {
			boolean attendeeFound = false;

			Optional<User> childBookingUser =
				CalendarHelperUtil.getCalendarResourceUser(
					childCalendarBooking.getCalendarResource());

			if (_log.isDebugEnabled()) {
				if (childBookingUser.isPresent()) {
					_log.debug(
						"User for calendar booking " +
						childCalendarBooking.getCalendarBookingId() + " is " +
						childBookingUser.get().getScreenName());
				}
				else {
					_log.debug(
						"No User found for calendar booking " +
						childCalendarBooking.getCalendarBookingId());
				}
			}

			if (!childBookingUser.isPresent()) {
				continue;
			}

			for (Attendee attendee : attendees) {

				String emailAddress = StringUtil.replace(
					attendee.getValue(), "mailto:", StringPool.BLANK);

				if (!Validator.isEmailAddress(emailAddress)) {
					continue;
				}

				if (attendeesEmails.contains(emailAddress)) {
					continue;
				}

				if (emailAddress.equals(
						childBookingUser.get().getEmailAddress())) {

					attendeeFound = true;
					attendeesEmails.add(emailAddress);

					Parameter partStatParameter = attendee.getParameter(
						PartStat.PARTSTAT);

					if (partStatParameter == null) {
						continue;
					}

					attendee.getParameters().remove(partStatParameter);

					switch (childCalendarBooking.getStatus()) {
						case WorkflowConstants.STATUS_DENIED:
							attendee.getParameters().add(PartStat.DECLINED);
							break;
						case WorkflowConstants.STATUS_APPROVED:
							attendee.getParameters().add(PartStat.ACCEPTED);
							attendee.getParameters().removeAll(Rsvp.RSVP);
							break;
						case CalendarBookingWorkflowConstants.STATUS_MAYBE:
							attendee.getParameters().add(PartStat.TENTATIVE);
							break;
						default:
							attendee.getParameters().add(PartStat.NEEDS_ACTION);
							break;
					}

					break;
				}
			}

			String emailAddress = childBookingUser.get().getEmailAddress();

			if (_log.isDebugEnabled()) {
				_log.debug(
					"Child booking user email address is " + emailAddress);
			}

			if ((userOrganizer != null) && childBookingUser.get().equals(
					userOrganizer)) {
				continue;
			}

			if (!attendeeFound && !attendeesEmails.contains(emailAddress) &&
				!attendeesEmails.contains(
					childBookingUser.get().getEmailAddress())) {

				Attendee attendee = AttendeeUtil.create(
					childBookingUser.get().getEmailAddress(),
					childBookingUser.get().getFullName(), true,
					childCalendarBooking.getStatus());

				if (_log.isDebugEnabled()) {
					_log.debug(
						"Add attendee " + attendee.toString() +
						" to booking " +
						calendarBooking.getCalendarBookingId());
				}

				vEvent.getProperties().add(attendee);
				attendeesEmails.add(emailAddress);
			}
		}

		// add current user as attendee

		if (bookingPending && hasUpdatePermissions && (bookingUser != null) &&
			bookingUser.equals(user) && !bookingUser.equals(userOrganizer) &&
			!attendeesEmails.contains(user.getEmailAddress())) {

			Property methodProperty = iCalCalendar.getProperty(Method.METHOD);

			iCalCalendar.getProperties().remove(methodProperty);
			iCalCalendar.getProperties().add(Method.REQUEST);

			Attendee attendee = AttendeeUtil.create(
				user.getEmailAddress(), user.getFullName(), true,
				CalendarBookingWorkflowConstants.STATUS_PENDING);

			if (_log.isDebugEnabled()) {
				_log.debug(
					"Adding current user (" + user.getEmailAddress() +
					") as attendee of booking" +
					calendarBooking.getCalendarBookingId());
			}

			vEvent.getProperties().add(attendee);
		}
	}

	protected void updateICSExternalAttendees(
			VEvent vEvent, CalendarBooking calendarBooking)
		throws PortalException {

		ExpandoBridge calendarBookingExpando =
			calendarBooking.getExpandoBridge();

		String invitedUsersCustomFieldName =
			PropsValues.INVITED_USERS_CUSTOM_FIELD_NAME;

		String invitedUsersLabelCustomFieldName =
			PropsValues.INVITED_USERS_LABEL_CUSTOM_FIELD_NAME;

		if (Validator.isNull(invitedUsersCustomFieldName) ||
			Validator.isNull(invitedUsersLabelCustomFieldName)) {

			return;
		}

		String[] attendees = GetterUtil.getStringValues(
			calendarBookingExpando.getAttribute(
				invitedUsersCustomFieldName, Boolean.FALSE));

		String[] visibleAttendeeEmails = GetterUtil.getStringValues(
			calendarBookingExpando.getAttribute(
				invitedUsersLabelCustomFieldName, Boolean.FALSE));

		PropertyList propertyList = vEvent.getProperties();

		// remove attendee not into user visible custom field

		List<Attendee> allAttendees = new ArrayList<>();

		for (String attendeeString : attendees) {
			Attendee attendee = _toICalAttendee(attendeeString);

			String attendeeEmail = StringUtil.replace(
				attendee.getValue().toLowerCase(), "mailto:",
				StringPool.BLANK);

			if (ArrayUtil.contains(visibleAttendeeEmails, attendeeEmail)) {
				visibleAttendeeEmails = ArrayUtil.remove(
					visibleAttendeeEmails, attendeeEmail);
				allAttendees.add(attendee);
			}
		}

		// add new emails ad attendee

		for (String attendeeEmail : visibleAttendeeEmails) {
			URI uri = URI.create("mailto:" + attendeeEmail);
			Attendee attendee = new Attendee(uri);
			attendee.getParameters().add(new Cn(attendeeEmail));

			allAttendees.add(attendee);
		}

		propertyList.addAll(allAttendees);
	}

	private List<String> _getNotificationRecipients(Calendar calendar)
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

	private Attendee _toICalAttendee(String attendeeString)
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

			net.fortuna.ical4j.model.Component vEvent =
				iCalCalendar.getComponent(VEvent.VEVENT);

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

		return attendee;
	}

	private void _updateAllBookingModifiedDate(
			CalendarBooking calendarBooking, Date date)
		throws PortalException {

		CalendarBooking parentCalendarBooking =
			calendarBooking.getParentCalendarBooking();

		if (calendarBooking.getModifiedDate().getTime() < date.getTime()) {

			parentCalendarBooking.setModifiedDate(date);

			CalendarBookingLocalServiceUtil.updateCalendarBooking(
				parentCalendarBooking);
		}

		List<CalendarBooking> childCalendarBookings =
			parentCalendarBooking.getChildCalendarBookings();

		for (CalendarBooking childCalendarBooking : childCalendarBookings) {
			if (childCalendarBooking.getModifiedDate().getTime() >=
					date.getTime()) {
				continue;
			}

			childCalendarBooking.setModifiedDate(date);

			CalendarBookingLocalServiceUtil.updateCalendarBooking(
				childCalendarBooking);
		}
	}

	private static Log _log = LogFactoryUtil.getLog(
		DefaultICSContentListener.class);

}
