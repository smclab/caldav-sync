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

package it.smc.calendar.caldav.sync.ical.util;

import com.liferay.calendar.workflow.CalendarBookingWorkflowConstants;
import com.liferay.portal.kernel.workflow.WorkflowConstants;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.parameter.XParameter;
import net.fortuna.ical4j.model.property.Attendee;

import java.net.URI;

/**
 * @author Fabio Pezzutto
 */
public class AttendeeUtil {

	public static Attendee create(
		String emailAddress, String fullName, boolean isRoleParticipant,
		int status) {

		URI uri = URI.create("mailto:".concat(emailAddress));
		Attendee attendee = new Attendee(uri);
		attendee.getParameters().add(new Cn(fullName));
		attendee.getParameters().add(CuType.INDIVIDUAL);

		switch (status) {
			case WorkflowConstants.STATUS_DENIED:
				attendee.getParameters().add(PartStat.DECLINED);
				break;
			case WorkflowConstants.STATUS_APPROVED:
				attendee.getParameters().add(PartStat.ACCEPTED);
				break;
			case CalendarBookingWorkflowConstants.STATUS_MAYBE:
				attendee.getParameters().add(PartStat.TENTATIVE);
				break;
			default:
				attendee.getParameters().add(PartStat.NEEDS_ACTION);
				attendee.getParameters().add(Rsvp.TRUE);
				break;
		}

		if (isRoleParticipant) {
			attendee.getParameters().add(
				net.fortuna.ical4j.model.parameter.Role.REQ_PARTICIPANT);
		}
		else {
			attendee.getParameters().add(
				Role.CHAIR);
		}

		attendee.getParameters().add(new XParameter("X-NUM-GUESTS", "0"));

		return attendee;
	}

	public static int getStatus(Attendee attendee, int defaultStatus) {
		PartStat partstat = (PartStat)attendee.getParameters().getParameter(
			PartStat.PARTSTAT);

		if (partstat == null) {
			return defaultStatus;
		}

		if (PartStat.ACCEPTED.equals(partstat)) {
			return WorkflowConstants.STATUS_APPROVED;
		}
		else if (PartStat.DECLINED.equals(partstat)) {
			return WorkflowConstants.STATUS_DENIED;
		}
		else if (PartStat.TENTATIVE.equals(partstat)) {
			return CalendarBookingWorkflowConstants.STATUS_MAYBE;
		}

		return defaultStatus;
	}
}
