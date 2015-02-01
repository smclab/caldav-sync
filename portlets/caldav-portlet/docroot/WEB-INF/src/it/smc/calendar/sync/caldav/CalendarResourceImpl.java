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

package it.smc.calendar.sync.caldav;

import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.service.CalendarServiceUtil;
import com.liferay.calendar.util.CalendarDataFormat;
import com.liferay.portal.kernel.io.unsync.UnsyncByteArrayInputStream;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.webdav.BaseResourceImpl;
import com.liferay.portal.kernel.webdav.WebDAVException;

import it.smc.calendar.sync.caldav.util.CalDAVUtil;

import java.io.InputStream;

import java.util.Locale;

/**
 * @author Fabio Pezzutto
 */
public class CalendarResourceImpl extends BaseResourceImpl {

	public CalendarResourceImpl(
			Calendar calendar, String parentPath, Locale locale) {

		super(
			parentPath, StringPool.BLANK, calendar.getName(locale),
			calendar.getCreateDate(), calendar.getModifiedDate());

		setModel(calendar);
		setClassName(Calendar.class.getName());
		setPrimaryKey(calendar.getPrimaryKey());

		try {
			_href = CalDAVUtil.getCalendarURL(calendar);
		}
		catch (Exception e) {
			_href = parentPath;
			_log.error(e);
		}

		_calendar = calendar;
	}

	@Override
	public InputStream getContentAsStream() throws WebDAVException {
		try {
			String data = CalendarServiceUtil.exportCalendar(
				_calendar.getCalendarId(), CalendarDataFormat.ICAL.getValue());

			return new UnsyncByteArrayInputStream(
				data.getBytes(StringPool.UTF8));
		}
		catch (Exception e) {
			throw new WebDAVException(e);
		}
	}

	@Override
	public String getContentType() {
		return ContentTypes.TEXT_CALENDAR + "; component=vevent";
	}

	@Override
	public String getHREF() {
		return _href;
	}

	@Override
	public long getSize() {

		String data = StringPool.BLANK;

		try {
			CalendarServiceUtil.exportCalendar(
				_calendar.getCalendarId(), CalendarDataFormat.ICAL.getValue());
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Unable to export calendar to ics for calendar " +
					_calendar.getCalendarId());
			}
		}

		return data.getBytes().length;
	}

	@Override
	public boolean isCollection() {
		return true;
	}

	private static Log _log = LogFactoryUtil.getLog(CalendarResourceImpl.class);

	private Calendar _calendar;
	private String _href;

}