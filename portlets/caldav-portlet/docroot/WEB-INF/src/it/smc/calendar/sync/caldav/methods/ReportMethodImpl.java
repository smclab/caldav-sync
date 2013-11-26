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

package it.smc.calendar.sync.caldav.methods;

import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.service.CalendarBookingLocalServiceUtil;
import com.liferay.calendar.util.CalendarDataFormat;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.webdav.WebDAVStorage;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.QName;
import com.liferay.util.xml.DocUtil;

import it.smc.calendar.sync.caldav.CalDAVRequestThreadLocal;
import it.smc.calendar.sync.caldav.util.CalDAVProps;
import it.smc.calendar.sync.caldav.util.CalDAVUtil;
import it.smc.calendar.sync.calendar.util.CalendarUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
public class ReportMethodImpl extends PropfindMethodImpl {

	protected void addCalendarBookingData(
			WebDAVRequest webDAVRequest, Resource resource,
			CalendarBooking calendarBooking, Element multistatusElement)
		throws Exception {

		String data = CalendarBookingLocalServiceUtil.exportCalendarBooking(
			calendarBooking.getCalendarBookingId(),
			CalendarDataFormat.ICAL.getValue());

		Element responseElement = DocUtil.add(
			multistatusElement, CalDAVProps.createQName("response"));

		DocUtil.add(
			responseElement, CalDAVProps.createQName("href"),
			CalDAVUtil.getCalendarBookingURL(calendarBooking));

		Element propStatElement = DocUtil.add(
			responseElement, CalDAVProps.createQName("propstat"));
		Element propElement = DocUtil.add(
			propStatElement, CalDAVProps.createQName("prop"));

		DocUtil.add(
			propElement, CalDAVProps.createQName("getetag"),
			CalDAVUtil.getResourceETag(resource));

		Element calendarDataEl = DocUtil.add(
			propElement, CalDAVProps.createCalendarQName("calendar-data"));

		calendarDataEl.addCDATA(data);

		DocUtil.add(
			propStatElement, CalDAVProps.createQName("status"),
			"HTTP/1.1 200 OK");
	}

	@Override
	protected void addResponse(
			WebDAVStorage storage, WebDAVRequest webDAVRequest,
			Resource resource, Set<QName> props, Element multistatusElement,
			long depth)
		throws Exception {

		List<CalendarBooking> calendarBookings = null;

		List<Node> hrefNodes =
			CalDAVRequestThreadLocal.getRequestDocument().selectNodes(
				"//*[local-name()='href']");

		if ((hrefNodes.size() > 0) &&
			CalDAVUtil.isCalendarBookingRequest(webDAVRequest)) {

			calendarBookings = new ArrayList<CalendarBooking>();

			CalendarBooking calendarBooking;

			for (Node hrefNode : hrefNodes) {
				calendarBooking = CalDAVUtil.getCalendarBookingFromURL(
					hrefNode.getText());

				if (calendarBooking != null) {
					calendarBookings.add(calendarBooking);
				}
			}
		}
		else {
			Calendar calendar = (Calendar)resource.getModel();

			Date startDate = null;
			Date endDate = null;

			Element timeRangeElement = CalDAVUtil.getReportDateFilter();

			if ((timeRangeElement != null) &&
				(timeRangeElement.attribute("start") != null)) {

				String startDateStr = timeRangeElement.attribute(
					"start").getValue();

				startDate = new net.fortuna.ical4j.model.Date(startDateStr);
			}

			if ((timeRangeElement != null) &&
				(timeRangeElement.attribute("end") != null)) {

				String endDateStr = timeRangeElement.attribute(
					"end").getValue();

				endDate = new net.fortuna.ical4j.model.Date(endDateStr);
			}

			calendarBookings =
				CalendarUtil.getCalendarBookings(
					webDAVRequest.getPermissionChecker(), calendar, startDate,
					endDate);
		}

		for (CalendarBooking calendarBooking : calendarBookings) {
			try {
				addCalendarBookingData(
					webDAVRequest, resource, calendarBooking,
					multistatusElement);
			}
			catch (Exception e) {
				if (_log.isWarnEnabled()) {
					_log.warn(e);
				}
			}
		}
	}

	private static Log _log = LogFactoryUtil.getLog(ReportMethodImpl.class);

}