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
import com.liferay.calendar.model.CalendarResource;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.io.unsync.UnsyncStringWriter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.upload.LiferayInputStream;
import it.smc.calendar.caldav.helper.api.CalendarHelperUtil;
import it.smc.calendar.caldav.sync.util.CalDAVProps;
import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import it.smc.calendar.caldav.sync.util.CalendarModelPermission;
import it.smc.calendar.caldav.sync.util.DocUtil;
import it.smc.calendar.caldav.sync.util.ICalUtil;
import it.smc.calendar.caldav.sync.util.WebKeys;
import it.smc.calendar.caldav.util.PropsValues;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.ComponentList;

import java.util.Optional;

/**
 * @author Fabio Pezzutto
 */
public class CalendarPropsProcessor extends BasePropsProcessor {

	public CalendarPropsProcessor() {
	}

	public CalendarPropsProcessor(
		WebDAVRequest webDAVRequest, Resource resource, Element rootElement) {

		super(webDAVRequest, resource, rootElement);

		_calendar = (Calendar)resource.getModel();
	}

	@Override
	protected void processCalDAVCalendarColor() {
		String color = CalDAVUtil.getCalendarColor(_calendar);

		if (CalDAVUtil.isMacOSX(webDAVRequest)) {
			color = color.concat("FF");
		}

		successPropElement.addElement(CalDAVProps.CALDAV_CALENDAR_COLOR);
		successPropElement.addText(color);
	}

	@Override
	protected void processCalDAVCalendarDescription() {
		successPropElement.addElement(CalDAVProps.CALDAV_CALENDAR_DESCRIPTION);
		successPropElement.addText(_calendar.getDescription(locale));
	}

	@Override
	protected void processCalDAVCalendarHomeSet() {
		CalendarResource calendarResource;

		try {
			calendarResource = _calendar.getCalendarResource();
		}
		catch (Exception e) {
			_log.error(e);

			return;
		}

		Element calendarHomeSetElement = successPropElement.addElement(
			CalDAVProps.CALDAV_CALENDAR_HOME_SET);
		calendarHomeSetElement.addElement(CalDAVProps.createQName("href"));
		calendarHomeSetElement.addText(
			CalDAVUtil.getCalendarResourceURL(calendarResource));
	}

	@Override
	protected void processCalDAVCalendarTimeZone() {
		net.fortuna.ical4j.model.Calendar iCalCalendar =
			ICalUtil.getVTimeZoneCalendar();

		Element calendarTimeZoneElement = successPropElement.addElement(
			CalDAVProps.CALDAV_CALENDAR_TIMEZONE);

		try {
			calendarTimeZoneElement.addCDATA(vTimeZoneToString(iCalCalendar));
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	@Override
	protected void processCalDAVCalendarUserAddressSet() {
		CalendarResource calendarResource;

		try {
			calendarResource = _calendar.getCalendarResource();
		}
		catch (Exception e) {
			_log.error(e);

			return;
		}

		String address = CalDAVUtil.getCalendarResourceURL(calendarResource);

		Optional<User> user = CalendarHelperUtil.getCalendarResourceUser(
			calendarResource);

		if (user.isPresent()) {
			address =
				"mailto:" +
					user.get(
					).getEmailAddress();
		}

		Element calendarUserAddressSetElement = successPropElement.addElement(
			CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET);

		calendarUserAddressSetElement.addElement(
			CalDAVProps.createQName("href"));
		calendarUserAddressSetElement.addText(address);
	}

	@Override
	protected void processCalDAVGetCTag() {
		successPropElement.addElement( CalDAVProps.CALDAV_GETCTAG);
		successPropElement.addText(CalDAVUtil.getResourceETag(resource));
	}

	@Override
	protected void processCalDAVMaxResourceSize() {
		long maxResourceSize = LiferayInputStream.THRESHOLD_SIZE / 8;
		successPropElement.addElement(CalDAVProps.CALDAV_MAX_RESOURCE_SIZE);
		successPropElement.addText(String.valueOf(maxResourceSize));
	}

	@Override
	protected void processCalDAVSupportedCalendarComponentSet() {
		Element supportedCalendarComponentSet = successPropElement.addElement(
			CalDAVProps.CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET);

		Element el = supportedCalendarComponentSet.addElement(
			CalDAVProps.DAV_COMP);

		el.addAttribute("name", WebKeys.VCALENDAR);

		el = supportedCalendarComponentSet.addElement(CalDAVProps.DAV_COMP);

		el.addAttribute("name", WebKeys.VEVENT);
	}

	@Override
	protected void processCalDAVSupportedCalendarData() {
		Element supportedCalendarDataElement = successPropElement.addElement(
			CalDAVProps.CALDAV_SUPPORTED_CALENDAR_DATA);

		Element calendarDataElement = supportedCalendarDataElement.addElement(
			CalDAVProps.createCalendarQName("calendar-data"));

		calendarDataElement.addAttribute(
			"content-type", resource.getContentType());

		calendarDataElement.addAttribute("version", "2.0");
	}

	@Override
	protected void processCalDAVValidCalendarData() {
		Element validCalendarDataElement = successPropElement.addElement(
			CalDAVProps.CALDAV_VALID_CALENDAR_DATA);

		net.fortuna.ical4j.model.Calendar iCalCalendar =
			ICalUtil.getVTimeZoneCalendar();

		try {
			validCalendarDataElement.addCDATA(vTimeZoneToString(iCalCalendar));
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	@Override
	protected void processDAVCurrentUserPrivilegeSet() {
		Element currentUserPrivilegeSetElement = successPropElement.addElement(
			CalDAVProps.DAV_CURRENT_USER_PRIVILEGE_SET);

		Element readPrivilegeElement =
			currentUserPrivilegeSetElement.addElement(
				CalDAVProps.createQName("privilege"));

		readPrivilegeElement.addElement(CalDAVProps.createQName("read"));

		if (CalendarModelPermission.contains(
				webDAVRequest.getPermissionChecker(), _calendar,
				ActionKeys.UPDATE)) {

			readPrivilegeElement.addElement(CalDAVProps.createQName("write"));

			readPrivilegeElement.addElement(
				CalDAVProps.createQName("write-content"));
		}
	}

	@Override
	protected void processDAVDisplayName() {
		if (PropsValues.EXTENDED_DISPLAY_NAME) {
			StringBuilder sb = new StringBuilder();

			try {
				CalendarResource calendarResource =
					_calendar.getCalendarResource();

				if (calendarResource != null) {
					sb.append(calendarResource.getName(locale));
					sb.append(" - ");
				}
			}
			catch (PortalException pe) {
				if (_log.isWarnEnabled()) {
					_log.warn(pe.getMessage());
				}
			}

			sb.append(_calendar.getName(locale));

			successPropElement.addElement(CalDAVProps.DAV_DISPLAYNAME);
			successPropElement.addText(sb.toString());
		}
		else {
			successPropElement.addElement(CalDAVProps.DAV_DISPLAYNAME);
			successPropElement.addText(
				_calendar.getName(locale).replaceAll(
					StringPool.SPACE, StringPool.BLANK));
		}
	}

	@Override
	protected void processDAVGetContentLength() {
		successPropElement.addElement(CalDAVProps.DAV_GETCONTENTLENGTH);
		successPropElement.addText(String.valueOf(resource.getSize()));
	}

	@Override
	protected void processDAVOwner() {
		successPropElement.addElement(CalDAVProps.DAV_OWNER);
		successPropElement.addText(
			CalDAVUtil.getPrincipalURL(_calendar.getUserId()));
	}

	@Override
	protected void processDAVResourceId() {
		Element resourceIdElement = successPropElement.addElement(
			CalDAVProps.DAV_RESOURCE_ID);

		resourceIdElement.addElement(CalDAVProps.createQName("href"));
		resourceIdElement.addText("urn:uuid:".concat(_calendar.getUuid()));
	}

	@Override
	protected void processDAVResourceType() {
		Element resourceTypeElement = successPropElement.addElement(
			CalDAVProps.DAV_RESOURCETYPE);

		if (!CalDAVUtil.isThunderbird(webDAVRequest)) {
			resourceTypeElement.addElement(
				CalDAVProps.createQName("collection"));
		}

		resourceTypeElement.addElement(
			CalDAVProps.createCalendarQName("calendar"));
	}

	protected String vTimeZoneToString(
			net.fortuna.ical4j.model.Calendar iCalCalendar)
		throws Exception {

		CalendarOutputter calendarOutputter = new CalendarOutputter();

		ComponentList componentList = iCalCalendar.getComponents();

		if (componentList.isEmpty()) {
			calendarOutputter.setValidating(false);
		}

		UnsyncStringWriter unsyncStringWriter = new UnsyncStringWriter();

		calendarOutputter.output(iCalCalendar, unsyncStringWriter);

		unsyncStringWriter.flush();

		return unsyncStringWriter.toString();
	}

	private static Log _log = LogFactoryUtil.getLog(
		CalendarPropsProcessor.class);

	private Calendar _calendar;

}