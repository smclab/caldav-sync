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
import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.permission.CalendarPermission;
import com.liferay.portal.kernel.io.unsync.UnsyncStringWriter;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ReleaseInfo;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.util.xml.DocUtil;

import it.smc.calendar.sync.caldav.util.CalDAVProps;
import it.smc.calendar.sync.caldav.util.CalDAVUtil;
import it.smc.calendar.sync.caldav.util.WebKeys;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

/**
 * @author Fabio Pezzutto
 */
public class CalendarPropsProcessor extends BasePropsProcessor {

	public CalendarPropsProcessor(
		WebDAVRequest webDAVRequest, Resource resource, Element rootElement) {

		super(webDAVRequest, resource, rootElement);

		_calendar = (Calendar)resource.getModel();
	}

	@Override
	protected void processCalDAVCalendarColor() {
		DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_COLOR,
			CalDAVUtil.getCalendarColor(_calendar));
	}

	@Override
	protected void processCalDAVCalendarDescription() {
		DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_DESCRIPTION,
			_calendar.getDescription(locale));
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

		Element calendarHomeSetElement = DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_HOME_SET);

		DocUtil.add(
			calendarHomeSetElement, CalDAVProps.createQName("href"),
			CalDAVUtil.getCalendarResourceURL(calendarResource));
	}

	@Override
	protected void processCalDAVCalendarTimeZone() {
		TimeZoneRegistry registry =
			TimeZoneRegistryFactory.getInstance().createRegistry();

		TimeZone timeZone = registry.getTimeZone("GMT");

		VTimeZone vTimeZone = timeZone.getVTimeZone();

		net.fortuna.ical4j.model.Calendar iCalCalendar =
			new net.fortuna.ical4j.model.Calendar();

		PropertyList propertiesList = iCalCalendar.getProperties();

		ProdId prodId = new ProdId(
			"-//Liferay Inc//Liferay Portal " + ReleaseInfo.getVersion() +
			"//EN");

		propertiesList.add(prodId);
		propertiesList.add(Version.VERSION_2_0);

		iCalCalendar.getComponents().add(vTimeZone);

		Element calendarTimeZoneElement = DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_TIMEZONE);

		try {
			calendarTimeZoneElement.addCDATA(vTimeZoneToString(iCalCalendar));
		}
		catch (Exception e) {
			_log.error(e, e);
		}
	}

	@Override
	protected void processCalDAVGetCTag() {
		DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_GETCTAG,
			CalDAVUtil.getResourceETag(resource));
	}

	@Override
	protected void processCalDAVSupportedCalendarComponentSet() {
		Element supportedCalendarComponentSet = DocUtil.add(
			successPropElement,
			CalDAVProps.CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET);

		if (!resource.isCollection()) {
			DocUtil.add(
				supportedCalendarComponentSet, CalDAVProps.DAV_COMP,
				WebKeys.VCALENDAR);
			DocUtil.add(
				supportedCalendarComponentSet, CalDAVProps.DAV_COMP,
				WebKeys.VEVENT);
		}
	}

	@Override
	protected void processCalDAVSupportedCalendarData() {
		Element supportedCalendarDataElement = DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_SUPPORTED_CALENDAR_DATA);

		Element calendarDataElement = DocUtil.add(
			supportedCalendarDataElement,
			CalDAVProps.createCalendarQName("calendar-data"));

		calendarDataElement.addAttribute(
			"content-type", resource.getContentType());

		calendarDataElement.addAttribute("version", "2.0");
	}

	@Override
	protected void processDAVCurrentUserPrivilegeSet() {
		Element currentUserPrivilegeSetElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_CURRENT_USER_PRIVILEGE_SET);

		Element readPrivilegeElement = DocUtil.add(
			currentUserPrivilegeSetElement,
			CalDAVProps.createQName("privilege"));

		DocUtil.add(readPrivilegeElement, CalDAVProps.createQName("read"));

		if (CalendarPermission.contains(
				webDAVRequest.getPermissionChecker(), _calendar,
				ActionKeys.UPDATE)) {

			DocUtil.add(readPrivilegeElement, CalDAVProps.createQName("write"));

			DocUtil.add(
				readPrivilegeElement, CalDAVProps.createQName("write-content"));
		}
	}

	@Override
	protected void processDAVGetContentLength() {
		DocUtil.add(
			successPropElement, CalDAVProps.DAV_GETCONTENTLENGTH,
			resource.getSize());
	}

	@Override
	protected void processDAVResourceType() {
		Element resourceTypeElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_RESOURCETYPE);

		if (!CalDAVUtil.isThunderbird(webDAVRequest)) {
			DocUtil.add(
				resourceTypeElement, CalDAVProps.createQName("collection"));
		}

		DocUtil.add(
			resourceTypeElement, CalDAVProps.createCalendarQName("calendar"));
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