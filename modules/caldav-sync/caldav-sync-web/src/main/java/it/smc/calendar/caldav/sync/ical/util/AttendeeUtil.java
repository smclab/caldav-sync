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
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.property.Attendee;

/**
 * @author Fabio Pezzutto
 */
public class AttendeeUtil {

	public static int getStatus(Attendee attendee, int defaultStatus) {
		PartStat partstat = (PartStat)attendee.getParameters().getParameter(
			PartStat.PARTSTAT);

		if (partstat == null) {
			return defaultStatus;
		}

		if (PartStat.ACCEPTED.equals(partstat)) {
			return CalendarBookingWorkflowConstants.STATUS_APPROVED;
		}
		else if (PartStat.DECLINED.equals(partstat)) {
			return CalendarBookingWorkflowConstants.STATUS_DENIED;
		}
		else if (PartStat.TENTATIVE.equals(partstat)) {
			return CalendarBookingWorkflowConstants.STATUS_MAYBE;
		}

		return defaultStatus;
	}
}
