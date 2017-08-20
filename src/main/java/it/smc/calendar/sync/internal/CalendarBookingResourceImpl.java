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

import com.liferay.calendar.exporter.CalendarDataFormat;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.service.CalendarBookingServiceUtil;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.webdav.BaseResourceImpl;
import com.liferay.portal.kernel.webdav.WebDAVException;

import it.smc.calendar.sync.util.CalDAVUtil;

import java.io.InputStream;

import java.util.Locale;

/**
 * @author Fabio Pezzutto
 */
public class CalendarBookingResourceImpl extends BaseResourceImpl {

	public CalendarBookingResourceImpl(CalendarBooking calendarBooking, String parentPath, Locale locale) {

		super(parentPath, StringPool.BLANK, calendarBooking.getTitle(locale), calendarBooking.getCreateDate(),
				calendarBooking.getModifiedDate());

		setModel(calendarBooking);
		setClassName(CalendarBooking.class.getName());
		setPrimaryKey(calendarBooking.getPrimaryKey());

		_calendarBooking = calendarBooking;
	}

	@Override
	public InputStream getContentAsStream() throws WebDAVException {
		try {
			String data = CalendarBookingServiceUtil.exportCalendarBooking(_calendarBooking.getCalendarBookingId(),
					CalendarDataFormat.ICAL.getValue());

			return new UnsyncByteArrayInputStream(data.getBytes(StringPool.UTF8));
		} catch (Exception e) {
			throw new WebDAVException(e);
		}
	}

	@Override
	public String getContentType() {
		return ContentTypes.TEXT_CALENDAR;
	}

	@Override
	public String getHREF() {
		String url = StringPool.BLANK;

		try {
			url = CalDAVUtil.getCalendarBookingURL(_calendarBooking);
		} catch (Exception e) {
			_log.error(e, e);
		}

		return url;
	}

	@Override
	public boolean isCollection() {
		return false;
	}

	private static Log _log = LogFactoryUtil.getLog(CalendarBookingResourceImpl.class);

	private CalendarBooking _calendarBooking;

}