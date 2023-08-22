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

package it.smc.calendar.caldav.sync.methods;

import com.liferay.calendar.exporter.CalendarDataFormat;
import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.service.CalendarBookingLocalServiceUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.webdav.WebDAVStorage;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.Node;
import com.liferay.portal.kernel.xml.QName;
import it.smc.calendar.caldav.sync.listener.ICSContentImportExportFactoryUtil;
import it.smc.calendar.caldav.sync.listener.ICSImportExportListener;
import it.smc.calendar.caldav.sync.util.CalDAVProps;
import it.smc.calendar.caldav.sync.util.CalDAVRequestThreadLocal;
import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import it.smc.calendar.caldav.sync.util.DocUtil;
import it.smc.calendar.caldav.util.CalendarUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * @author Fabio Pezzutto
 */
public class ReportMethodImpl extends PropfindMethodImpl {

	protected void addCalendarBookingData(
			WebDAVRequest webDAVRequest, Resource resource,
			CalendarBooking calendarBooking, Element multistatusElement)
		throws Exception {

		String data = CalendarBookingLocalServiceUtil.exportCalendarBooking(
			calendarBooking.getCalendarBookingId(),
			CalendarDataFormat.ICAL.getValue());

		ICSImportExportListener icsContentListener =
			ICSContentImportExportFactoryUtil.newInstance();

		data = icsContentListener.beforeContentExported(data, calendarBooking);

		Element responseElement = multistatusElement.addElement(
			CalDAVProps.createQName("response"));

		responseElement.addElement(CalDAVProps.createQName("href"));
		responseElement.addText(
			CalDAVUtil.getCalendarBookingURL(calendarBooking));

		Element propStatElement = responseElement.addElement(
			CalDAVProps.createQName("propstat"));

		Element propElement = propStatElement.addElement(
			CalDAVProps.createQName("prop"));

		String getetag = CalDAVUtil.buildETag(
			String.valueOf(calendarBooking.getPrimaryKey()),
			calendarBooking.getModifiedDate());

		propElement.addElement(CalDAVProps.createQName("getetag"));
		propElement.addText(getetag);

		Element calendarDataEl = propElement.addElement(
			CalDAVProps.createCalendarQName("calendar-data"));

		calendarDataEl.addCDATA(data);

		icsContentListener.afterContentExported(data, calendarBooking);

		propStatElement.addElement(CalDAVProps.createQName("status"));
		propStatElement.addText("HTTP/1.1 200 OK");
	}

	protected void addCalendarObjResourceNotFound(
		String href, Element multistatusElement) {

		Element responseElement = multistatusElement.addElement(
			CalDAVProps.createQName("response"));

		responseElement.addElement(CalDAVProps.createQName("href"));
		responseElement.addText(href);

		Element propStatElement = responseElement.addElement(
			CalDAVProps.createQName("propstat"));

		Element propElement = propStatElement.addElement(
			CalDAVProps.createQName("prop"));

		propElement.addElement(CalDAVProps.createQName("getetag"));

		propElement.addElement(
			CalDAVProps.createCalendarQName("calendar-data"));

		propStatElement.addElement(CalDAVProps.createQName("status"));
		propStatElement.addText("HTTP/1.1 404 Not Found");
	}

	@Override
	protected void addResponse(
			WebDAVStorage storage, WebDAVRequest webDAVRequest,
			Resource resource, Set<QName> props, Element multistatusElement,
			long depth)
		throws Exception {

		List<CalendarBooking> calendarBookings = null;

		List<Node> hrefNodes = CalDAVRequestThreadLocal.getRequestDocument(
		).selectNodes(
			"//*[local-name()='href']"
		);

		if (hrefNodes.size() > 0) {

			// CALDAV:calendar-multiget REPORT

			calendarBookings = new ArrayList<>();

			Calendar calendar = null;

			CalendarBooking calendarBooking;

			if (CalDAVUtil.isCalendarRequest(webDAVRequest)) {
				calendar = (Calendar)resource.getModel();
			}

			for (Node hrefNode : hrefNodes) {
				String URL = hrefNode.getText();

				if (calendar != null) {
					calendarBooking =
						CalDAVUtil.getCalendarBookingFromCalendarAndURL(
							calendar, URL);
				}
				else {
					calendarBooking = CalDAVUtil.getCalendarBookingFromURL(URL);
				}

				if (calendarBooking != null) {
					calendarBookings.add(calendarBooking);
				}
				else {
					addCalendarObjResourceNotFound(URL, multistatusElement);
				}
			}
		}
		else {

			// CALDAV:calendar-query REPORT

			Calendar calendar = (Calendar)resource.getModel();

			Date startDate = null;
			Date endDate = null;

			Element timeRangeElement = CalDAVUtil.getReportDateFilter();

			if ((timeRangeElement != null) &&
				(timeRangeElement.attribute("start") != null)) {

				String startDateStr = timeRangeElement.attribute(
					"start"
				).getValue();

				startDate = isoDateTimeUTCFormat.parse(startDateStr);
			}

			if ((timeRangeElement != null) &&
				(timeRangeElement.attribute("end") != null)) {

				String endDateStr = timeRangeElement.attribute(
					"end"
				).getValue();

				endDate = isoDateTimeUTCFormat.parse(endDateStr);
			}

			calendarBookings = CalendarUtil.getCalendarBookings(
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

	private static final DateFormat isoDateTimeUTCFormat = new SimpleDateFormat(
		"yyyyMMdd'T'HHmmss'Z'");

}