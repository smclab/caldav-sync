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

import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.CalendarResourceLocalServiceUtil;
import com.liferay.compat.portal.util.PortalUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.Namespace;
import com.liferay.portal.kernel.xml.QName;
import com.liferay.portal.model.User;
import com.liferay.portal.model.WebDAVProps;
import com.liferay.portal.service.UserServiceUtil;
import com.liferay.portal.service.WebDAVPropsLocalServiceUtil;
import com.liferay.util.xml.DocUtil;

import it.smc.calendar.sync.caldav.util.CalDAVProps;
import it.smc.calendar.sync.caldav.util.CalDAVUtil;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Fabio Pezzutto
 */
public abstract class BasePropsProcessor implements PropsProcessor {

	public BasePropsProcessor(
			WebDAVRequest webDAVRequest, Resource resource,
			Element rootElement) {

		init(webDAVRequest, resource, rootElement);
	}

	@Override
	public void processProperties(Set<QName> properties)
		throws PortalException {

		Set<QName> props = new HashSet<QName>(properties);

		if (props.contains(CalDAVProps.CALDAV_CALENDAR_COLOR)) {
			processCalDAVCalendarColor();
			props.remove(CalDAVProps.CALDAV_CALENDAR_COLOR);
		}

		if (props.contains(CalDAVProps.CALDAV_CALENDAR_DESCRIPTION)) {
			processCalDAVCalendarDescription();
			props.remove(CalDAVProps.CALDAV_CALENDAR_DESCRIPTION);
		}

		if (props.contains(CalDAVProps.CALDAV_CALENDAR_HOME_SET)) {
			processCalDAVCalendarHomeSet();
			props.remove(CalDAVProps.CALDAV_CALENDAR_HOME_SET);
		}

		if (props.contains(CalDAVProps.CALDAV_CALENDAR_TIMEZONE)) {

			// processCalDAVCalendarHomeSet();

			processCalDAVCalendarTimeZone();
			props.remove(CalDAVProps.CALDAV_CALENDAR_TIMEZONE);
		}

		if (props.contains(CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET)) {
			processCalDAVCalendarUserAddressSet();
			props.remove(CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET);
		}

		if (props.contains(CalDAVProps.CALDAV_GETCTAG)) {
			processCalDAVGetCTag();
			props.remove(CalDAVProps.CALDAV_GETCTAG);
		}

		if (props.contains(
				CalDAVProps.CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET)) {

			processCalDAVSupportedCalendarComponentSet();
			props.remove(CalDAVProps.CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET);
		}

		if (props.contains(CalDAVProps.CALDAV_SUPPORTED_CALENDAR_DATA)) {
			processCalDAVSupportedCalendarData();
			props.remove(CalDAVProps.CALDAV_SUPPORTED_CALENDAR_DATA);
		}

		if (props.contains(CalDAVProps.DAV_CREATIONDATE)) {
			processDAVCreationDate();
			props.remove(CalDAVProps.DAV_CREATIONDATE);
		}

		if (props.contains(CalDAVProps.DAV_DISPLAYNAME)) {
			processDAVDisplayName();
			props.remove(CalDAVProps.DAV_DISPLAYNAME);
		}

		if (props.contains(CalDAVProps.DAV_CURRENT_USER_PRINCIPAL)) {
			processDAVCurrentUserPrincipal();
			props.remove(CalDAVProps.DAV_CURRENT_USER_PRINCIPAL);
		}

		if (props.contains(CalDAVProps.DAV_CURRENT_USER_PRIVILEGE_SET)) {
			processDAVCurrentUserPrivilegeSet();
			props.remove(CalDAVProps.DAV_CURRENT_USER_PRIVILEGE_SET);
		}

		if (props.contains(CalDAVProps.DAV_GETCONTENTLENGTH)) {
			processDAVGetContentLength();
			props.remove(CalDAVProps.DAV_GETCONTENTLENGTH);
		}

		if (props.contains(CalDAVProps.DAV_GETCONTENTTYPE)) {
			processDAVGetContentType();
			props.remove(CalDAVProps.DAV_GETCONTENTTYPE);
		}

		if (props.contains(CalDAVProps.DAV_GETETAG)) {
			processDAVGetETag();
			props.remove(CalDAVProps.DAV_GETETAG);
		}

		if (props.contains(CalDAVProps.DAV_GETLASTMODIFIED)) {
			processDAVGetLastModified();
			props.remove(CalDAVProps.DAV_GETLASTMODIFIED);
		}

		if (props.contains(CalDAVProps.DAV_ISREADONLY)) {
			processDAVIsReadOnly();
			props.remove(CalDAVProps.DAV_ISREADONLY);
		}

		if (props.contains(CalDAVProps.DAV_OWNER)) {
			processDAVOwner();
			props.remove(CalDAVProps.DAV_OWNER);
		}

		if (props.contains(CalDAVProps.DAV_PRINCIPAL_COLLECTION_SET)) {
			processDAVPrincipalCollectionSet();
			props.remove(CalDAVProps.DAV_PRINCIPAL_COLLECTION_SET);
		}

		if (props.contains(CalDAVProps.DAV_PRINCIPAL_URL)) {
			processDAVPrincipalURL();
			props.remove(CalDAVProps.DAV_PRINCIPAL_URL);
		}

		if (props.contains(CalDAVProps.DAV_RESOURCETYPE)) {
			processDAVResourceType();
			props.remove(CalDAVProps.DAV_RESOURCETYPE);
		}

		if (props.contains(CalDAVProps.DAV_SOURCE)) {
			processDAVSource();
			props.remove(CalDAVProps.DAV_SOURCE);
		}

		if (props.contains(CalDAVProps.DAV_SUPPORTED_REPORT_SET)) {
			processDAVSupportedReportSet();
			props.remove(CalDAVProps.DAV_SUPPORTED_REPORT_SET);
		}

		// Check remaining properties against custom properties

		try {
			processCustomProperties(props);
		}
		catch (Exception e) {
			_log.error(e);
		}

		// remove empty result elements

		if (!successPropElement.hasContent()) {
			responseElement.remove(successPropStatElement);
		}

		if (!failurePropElement.hasContent()) {
			responseElement.remove(failurePropStatElement);
		}
	}

	protected void init(
			WebDAVRequest webDAVRequest, Resource resource,
			Element rootElement) {

		this.resource = resource;
		this.webDAVRequest = webDAVRequest;

		responseElement = DocUtil.add(
			rootElement, CalDAVProps.createQName("response"));

		DocUtil.add(
			responseElement, CalDAVProps.createQName("href"),
			resource.getHREF());

		// Build success and failure propstat elements

		successPropStatElement = DocUtil.add(
			responseElement, CalDAVProps.createQName("propstat"));
		successPropElement = DocUtil.add(
			successPropStatElement, CalDAVProps.createQName("prop"));
		DocUtil.add(
			successPropStatElement, CalDAVProps.createQName("status"),
			"HTTP/1.1 200 OK");

		failurePropStatElement = DocUtil.add(
			responseElement, CalDAVProps.createQName("propstat"));
		failurePropElement = DocUtil.add(
			failurePropStatElement, CalDAVProps.createQName("prop"));
		DocUtil.add(
			failurePropStatElement, CalDAVProps.createQName("status"),
			"HTTP/1.1 404 Not Found");

		try {
			currentPrincipal = UserServiceUtil.getUserById(
				webDAVRequest.getUserId());
			locale = currentPrincipal.getLocale();
		}
		catch (Exception e) {
			_log.error(e);
		}
	}

	protected void processCalDAVCalendarColor() {
		DocUtil.add(failurePropElement, CalDAVProps.CALDAV_CALENDAR_COLOR);
	}

	protected void processCalDAVCalendarDescription() {
		DocUtil.add(
			failurePropElement, CalDAVProps.CALDAV_CALENDAR_DESCRIPTION);
	}

	protected void processCalDAVCalendarHomeSet() {
		DocUtil.add(failurePropElement, CalDAVProps.CALDAV_CALENDAR_HOME_SET);
	}

	protected void processCalDAVCalendarTimeZone() {
		DocUtil.add(failurePropElement, CalDAVProps.CALDAV_CALENDAR_TIMEZONE);
	}

	protected void processCalDAVCalendarUserAddressSet() {

		// TODO: check for iCal

		CalendarResource resource = null;

		try {
			resource = CalendarResourceLocalServiceUtil.fetchCalendarResource(
				PortalUtil.getClassNameId(User.class),
				webDAVRequest.getUserId());
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e);
			}
		}

		if (resource != null) {
			Element calendarHomeSetElement = DocUtil.add(
				successPropElement,
				CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET);

			DocUtil.add(
				calendarHomeSetElement, CalDAVProps.createQName("href"),
				CalDAVUtil.getCalendarResourceURL(resource));
		}
		else {
			DocUtil.add(
				failurePropElement,
				CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET);
		}
	}

	protected void processCalDAVGetCTag() {
		DocUtil.add(failurePropElement, CalDAVProps.CALDAV_GETCTAG);
	}

	protected void processCalDAVSupportedCalendarComponentSet() {
		DocUtil.add(
			failurePropElement,
			CalDAVProps.CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET);
	}

	protected void processCalDAVSupportedCalendarData() {
		DocUtil.add(
			failurePropElement, CalDAVProps.CALDAV_SUPPORTED_CALENDAR_DATA);
	}

	protected void processCustomProperties(Set<QName> props) throws Exception {

		WebDAVProps webDavProps = WebDAVPropsLocalServiceUtil.getWebDAVProps(
			webDAVRequest.getCompanyId(), resource.getClassName(),
			resource.getPrimaryKey());

		Set<QName> customProps = webDavProps.getPropsSet();

		for (QName qname : props) {
			String name = qname.getName();
			Namespace namespace = qname.getNamespace();

			String prefix = namespace.getPrefix();
			String uri = namespace.getURI();

			if (customProps.contains(qname)) {
				String text = webDavProps.getText(name, prefix, uri);

				DocUtil.add(successPropElement, qname, text);
			}
			else {
				DocUtil.add(failurePropElement, qname);
			}
		}
	}

	protected void processDAVCreationDate() {
		DocUtil.add(
			successPropElement, CalDAVProps.DAV_CREATIONDATE,
			resource.getCreateDate());
	}

	protected void processDAVCurrentUserPrincipal() {

		if (CalDAVUtil.isIOS(webDAVRequest)) {
			DocUtil.add(
				failurePropElement, CalDAVProps.DAV_CURRENT_USER_PRINCIPAL);
			return;
		}

		// TODO: check

		Element principalUrlElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_CURRENT_USER_PRINCIPAL);

		DocUtil.add(
			principalUrlElement, CalDAVProps.createQName("href"),
			CalDAVUtil.getPrincipalURL(webDAVRequest.getUserId()));
	}

	protected void processDAVCurrentUserPrivilegeSet() {
		DocUtil.add(
			failurePropElement, CalDAVProps.DAV_CURRENT_USER_PRIVILEGE_SET);
	}

	protected void processDAVDisplayName() {

		// TODO: remove replaceAll if not necessary for iCal

		DocUtil.add(
			successPropElement, CalDAVProps.DAV_DISPLAYNAME,
			resource.getDisplayName().replaceAll(
				StringPool.SPACE, StringPool.BLANK));
	}

	protected void processDAVGetContentLength() {
		DocUtil.add(failurePropElement, CalDAVProps.DAV_GETCONTENTLENGTH);
	}

	protected void processDAVGetContentType() {
		DocUtil.add(
			successPropElement, CalDAVProps.DAV_GETCONTENTTYPE,
			resource.getContentType());
	}

	protected void processDAVGetETag() {
		String id = CalDAVUtil.getResourceETag(resource);

		DocUtil.add(successPropElement, CalDAVProps.DAV_GETETAG, id);
	}

	protected void processDAVGetLastModified() {
		DocUtil.add(
			successPropElement, CalDAVProps.DAV_GETLASTMODIFIED,
			resource.getModifiedDate());
	}

	protected void processDAVIsReadOnly() {
		DocUtil.add(
			successPropElement, CalDAVProps.DAV_ISREADONLY,
			resource.isLocked());
	}

	protected void processDAVOwner() {
		DocUtil.add(failurePropElement, CalDAVProps.DAV_OWNER);
	}

	protected void processDAVPrincipalCollectionSet() {

		// TODO: check for iCal

		CalendarResource resource = null;

		try {
			resource = CalendarResourceLocalServiceUtil.fetchCalendarResource(
				PortalUtil.getClassNameId(User.class),
				webDAVRequest.getUserId());
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e);
			}
		}

		if (resource != null) {
			Element calendarHomeSetElement = DocUtil.add(
				successPropElement, CalDAVProps.DAV_PRINCIPAL_COLLECTION_SET);

			DocUtil.add(
				calendarHomeSetElement, CalDAVProps.createQName("href"),
				CalDAVUtil.getCalendarResourceURL(resource));
		}
		else {
			DocUtil.add(
				failurePropElement, CalDAVProps.DAV_PRINCIPAL_COLLECTION_SET);
		}
	}

	protected void processDAVPrincipalURL() {
		Element principalUrlElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_PRINCIPAL_URL);

		DocUtil.add(
			principalUrlElement, CalDAVProps.createQName("href"),
			CalDAVUtil.getPrincipalURL(webDAVRequest.getUserId()));
	}

	protected void processDAVResourceType() {
		Element resourceTypeElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_RESOURCETYPE);

		DocUtil.add(resourceTypeElement, CalDAVProps.createQName("collection"));
	}

	protected void processDAVSource() {
		DocUtil.add(successPropElement, CalDAVProps.DAV_SOURCE);
	}

	protected void processDAVSupportedReportSet() {
		Element supportedResportSetElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_SUPPORTED_REPORT_SET);

		for (String reportSet : CalDAVMethod.SUPPORTED_CALDAV_REPORT_SET) {
			Element supportedResportElement = DocUtil.add(
				supportedResportSetElement,
				CalDAVProps.createCalendarQName("supported-report"));

			DocUtil.add(
				supportedResportElement,
				CalDAVProps.createCalendarQName("report"), reportSet);
		}
	}

	protected User currentPrincipal;
	protected Element failurePropElement;
	protected Element failurePropStatElement;
	protected Locale locale;
	protected Resource resource;
	protected Element responseElement;
	protected Element successPropElement;
	protected Element successPropStatElement;
	protected WebDAVProps webDAVProps;
	protected WebDAVRequest webDAVRequest;

	private static Log _log = LogFactoryUtil.getLog(BasePropsProcessor.class);

}