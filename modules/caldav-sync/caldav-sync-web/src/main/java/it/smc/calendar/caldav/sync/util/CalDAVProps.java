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

package it.smc.calendar.caldav.sync.util;

import com.liferay.portal.kernel.webdav.WebDAVUtil;
import com.liferay.portal.kernel.xml.QName;
import com.liferay.portal.kernel.xml.SAXReaderUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Fabio Pezzutto
 */
public class CalDAVProps {

	// CalDAV properties RFC 4918

	public static final QName CALDAV_CALENDAR_COLOR = createAppleQName(
		"calendar-color");

	// https://tools.ietf.org/html/rfc4791#section-5.2.1

	public static final QName CALDAV_CALENDAR_DESCRIPTION = createCalendarQName(
		"calendar-description");

	// https://tools.ietf.org/html/rfc4791#section-6.2.1

	public static final QName CALDAV_CALENDAR_HOME_SET = createCalendarQName(
		"calendar-home-set");

	// https://tools.ietf.org/html/rfc4791#section-5.2.2

	public static final QName CALDAV_CALENDAR_TIMEZONE = createCalendarQName(
		"calendar-timezone");

	// https://tools.ietf.org/html/rfc6638#section-2.4.1

	public static final QName CALDAV_CALENDAR_USER_ADDRESS_SET =
		createCalendarQName("calendar-user-address-set");

	// https://tools.ietf.org/html/rfc6638#section-2.4.2

	public static final QName CALDAV_CALENDAR_USER_TYPE = createCalendarQName(
		"calendar-user-type");

	public static final QName CALDAV_GETCTAG = createCalendarServerQName(
		"getctag");

	public static final QName CALDAV_GETETAG = createCalendarServerQName(
		"getetag");

	// https://tools.ietf.org/html/rfc4791#section-5.2.9

	public static final QName CALDAV_MAX_ATTENDEES_PER_INSTANCE =
		createCalendarQName("max-attendees-per-instance");

	// https://tools.ietf.org/html/rfc4791#section-5.2.7

	public static final QName CALDAV_MAX_DATE_TIME = createCalendarQName(
		"max-date-time");

	// https://tools.ietf.org/html/rfc4791#section-5.2.8

	public static final QName CALDAV_MAX_INSTANCES = createCalendarQName(
		"max-instances");

	// https://tools.ietf.org/html/rfc4791#section-5.2.5

	public static final QName CALDAV_MAX_RESOURCE_SIZE = createCalendarQName(
		"max-resource-size");

	// https://tools.ietf.org/html/rfc4791#section-5.2.6

	public static final QName CALDAV_MIN_DATE_TIME = createCalendarQName(
		"min-date-time");

	public static final QName CALDAV_SOURCE = createCalendarServerQName(
		"source");

	// https://tools.ietf.org/html/rfc4791#section-5.2.3

	public static final QName CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET =
		createCalendarQName("supported-calendar-component-set");

	// https://tools.ietf.org/html/rfc4791#section-5.2.4

	public static final QName CALDAV_SUPPORTED_CALENDAR_DATA =
		createCalendarQName("supported-calendar-data");

	// https://tools.ietf.org/html/rfc4791#section-5.2.10

	public static final QName CALDAV_VALID_CALENDAR_DATA =
		createCalendarQName("valid-calendar-data");


	// WebDAV properties RFC 4791

	public static final QName DAV_ALLPROP = createQName("allprop");

	//public static final QName DAV_COMP = SAXReaderUtil.createQName("comp");

	public static final QName DAV_COMP = createCalendarQName("comp");

	public static final QName DAV_CREATIONDATE = createQName("creationdate");

	public static final QName DAV_CURRENT_USER_PRINCIPAL = createQName(
		"current-user-principal");

	public static final QName DAV_CURRENT_USER_PRIVILEGE_SET = createQName(
		"current-user-privilege-set");

	public static final QName DAV_DISPLAYNAME = createQName("displayname");

	public static final QName DAV_GETCONTENTLENGTH = createQName(
		"getcontentlength");

	public static final QName DAV_GETCONTENTTYPE = createQName(
		"getcontenttype");

	public static final QName DAV_GETETAG = createQName("getetag");

	public static final QName DAV_GETLASTMODIFIED = createQName(
		"getlastmodified");

	public static final QName DAV_ISREADONLY = createQName("isreadonly");

	public static final QName DAV_LOCKDISCOVERY = createQName("lockdiscovery");

	public static final QName DAV_OWNER = createQName("owner");

	public static final QName DAV_PRINCIPAL_COLLECTION_SET = createQName(
		"principal-collection-set");

	public static final QName DAV_PRINCIPAL_URL = createQName("principal-URL");

	public static final QName DAV_PRINCIPAL_URL_ERR = createQName(
		"Principal-URL");

	// https://tools.ietf.org/html/rfc5842#section-3.1

	public static final QName DAV_RESOURCE_ID = createQName("resource-id");

	public static final QName DAV_RESOURCETYPE = createQName("resourcetype");

	public static final QName DAV_SOURCE = createQName("source");

	public static final QName DAV_SUPPORTED_PRIVILEGE_SET = createQName(
		"supported-privilege-set");

	public static final QName DAV_SUPPORTED_REPORT_SET = createQName(
		"supported-report-set");

	public static QName createAppleQName(String name) {
		return SAXReaderUtil.createQName(name, CalDAVUtil.NS_APPLE_URI);
	}

	public static QName createCalendarQName(String name) {
		return SAXReaderUtil.createQName(name, CalDAVUtil.NS_CALDAV_URI);
	}

	public static QName createCalendarServerQName(String name) {
		return SAXReaderUtil.createQName(
			name, CalDAVUtil.NS_CALENDAR_SERVER_URI);
	}

	public static QName createQName(String name) {
		return SAXReaderUtil.createQName(name, WebDAVUtil.DAV_URI);
	}

	public static Set<QName> getAllCollectionProps() {
		return _instance._getAllCollectionProList();
	}

	public static Set<QName> getAllResourceProps() {
		return _instance._getAllProList();
	}

	private CalDAVProps() {
		_allProps = new HashSet<>();
		_allCollectionProps = new HashSet<>();

		_allCollectionProps.add(DAV_CREATIONDATE);
		_allCollectionProps.add(DAV_DISPLAYNAME);
		_allCollectionProps.add(DAV_RESOURCETYPE);
		_allCollectionProps.add(DAV_GETCONTENTTYPE);
		_allCollectionProps.add(DAV_GETLASTMODIFIED);

		_allProps.add(DAV_CREATIONDATE);
		_allProps.add(DAV_DISPLAYNAME);
		_allProps.add(DAV_RESOURCETYPE);
		_allProps.add(DAV_GETCONTENTTYPE);
		_allProps.add(DAV_GETCONTENTLENGTH);
		_allProps.add(DAV_GETLASTMODIFIED);
		_allProps.add(DAV_ISREADONLY);
	}

	private Set<QName> _getAllCollectionProList() {
		return _allCollectionProps;
	}

	private Set<QName> _getAllProList() {
		return _allProps;
	}

	private static CalDAVProps _instance = new CalDAVProps();

	private Set<QName> _allCollectionProps;
	private Set<QName> _allProps;

}