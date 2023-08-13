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

import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.CalendarResourceLocalServiceUtil;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.model.WebDAVProps;
import com.liferay.portal.kernel.service.UserLocalServiceUtil;
import com.liferay.portal.kernel.service.UserServiceUtil;
import com.liferay.portal.kernel.service.WebDAVPropsLocalServiceUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.Namespace;
import com.liferay.portal.kernel.xml.QName;
import it.smc.calendar.caldav.helper.api.CalendarHelperUtil;
import it.smc.calendar.caldav.sync.util.CalDAVMethod;
import it.smc.calendar.caldav.sync.util.CalDAVProps;
import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import it.smc.calendar.caldav.sync.util.DocUtil;
import it.smc.calendar.caldav.sync.util.PropsProcessor;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Fabio Pezzutto
 */
public abstract class BasePropsProcessor implements PropsProcessor {

	public BasePropsProcessor() {
	}

	public BasePropsProcessor(
		WebDAVRequest webDAVRequest, Resource resource, Element rootElement) {

		init(webDAVRequest, resource, rootElement);
	}

	@Override
	public void processProperties(Set<QName> properties)
		throws PortalException {

		Set<QName> props = new HashSet<>(properties);

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
			processCalDAVCalendarTimeZone();
			props.remove(CalDAVProps.CALDAV_CALENDAR_TIMEZONE);
		}

		if (props.contains(CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET)) {
			processCalDAVCalendarUserAddressSet();
			props.remove(CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET);
		}

		if (props.contains(CalDAVProps.CALDAV_CALENDAR_USER_TYPE)) {
			processCalDAVCalendarUserType();
			props.remove(CalDAVProps.CALDAV_CALENDAR_USER_TYPE);
		}

		if (props.contains(CalDAVProps.CALDAV_GETCTAG)) {
			processCalDAVGetCTag();
			props.remove(CalDAVProps.CALDAV_GETCTAG);
		}

		if (props.contains(CalDAVProps.CALDAV_GETETAG)) {
			processCalDAVGetCTag();
			props.remove(CalDAVProps.CALDAV_GETETAG);
		}

		if (props.contains(CalDAVProps.CALDAV_MAX_RESOURCE_SIZE)) {
			processCalDAVMaxResourceSize();
			props.remove(CalDAVProps.CALDAV_MAX_RESOURCE_SIZE);
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

		if (props.contains(CalDAVProps.CALDAV_VALID_CALENDAR_DATA)) {
			processCalDAVValidCalendarData();
			props.remove(CalDAVProps.CALDAV_VALID_CALENDAR_DATA);
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

		if (props.contains(CalDAVProps.DAV_SOURCE) ||
			props.contains(CalDAVProps.CALDAV_SOURCE)) {

			processDAVSource();
			props.remove(CalDAVProps.DAV_SOURCE);
			props.remove(CalDAVProps.CALDAV_SOURCE);
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
		WebDAVRequest webDAVRequest, Resource resource, Element rootElement) {

		this.resource = resource;
		this.webDAVRequest = webDAVRequest;

		responseElement =
			rootElement.addElement(CalDAVProps.createQName("response"));

		responseElement.addElement(CalDAVProps.createQName("href"));
		responseElement.addText(resource.getHREF());

		// Build success and failure propstat elements

		successPropStatElement = responseElement.addElement(
			CalDAVProps.createQName("propstat"));
		successPropElement = successPropStatElement.addElement(
			CalDAVProps.createQName("prop"));

		successPropStatElement.addElement(CalDAVProps.createQName("status"));
		successPropStatElement.addText("HTTP/1.1 200 OK");

		failurePropStatElement = responseElement.addElement(
			CalDAVProps.createQName("propstat"));
		failurePropElement = failurePropStatElement.addElement(
			CalDAVProps.createQName("prop"));
		failurePropStatElement.addElement(CalDAVProps.createQName("status"));
		failurePropStatElement.addText("HTTP/1.1 404 Not Found");

		try {
			currentPrincipal = UserServiceUtil.getUserById(
				CalDAVUtil.getUserId(webDAVRequest));
			locale = currentPrincipal.getLocale();
		}
		catch (Exception e) {
			_log.error(e);
		}
	}

	protected void processCalDAVCalendarColor() {
		failurePropElement.addElement(CalDAVProps.CALDAV_CALENDAR_COLOR);
	}

	protected void processCalDAVCalendarDescription() {
		failurePropElement.addElement(CalDAVProps.CALDAV_CALENDAR_DESCRIPTION);
	}

	protected void processCalDAVCalendarHomeSet() {
		failurePropElement.addElement(CalDAVProps.CALDAV_CALENDAR_HOME_SET);
	}

	protected void processCalDAVCalendarTimeZone() {
		failurePropElement.addElement(CalDAVProps.CALDAV_CALENDAR_TIMEZONE);
	}

	protected void processCalDAVCalendarUserAddressSet() {
		long userId = CalDAVUtil.getUserId(webDAVRequest);

		User user = UserLocalServiceUtil.fetchUser(userId);

		if (user != null) {
			Element calendarHomeSetElement = successPropElement.addElement(
				CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET);

			calendarHomeSetElement.addElement(CalDAVProps.createQName("href"));
			calendarHomeSetElement.addText("mailto:" + user.getEmailAddress());
		}
		else {
			failurePropElement.addElement(
				CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET);
		}
	}

	protected void processCalDAVCalendarUserType() {
		CalendarResource calendarResource;

		try {
			calendarResource =
				CalendarResourceLocalServiceUtil.fetchCalendarResource(
					PortalUtil.getClassNameId(User.class),
					CalDAVUtil.getUserId(webDAVRequest));
		}
		catch (Exception e) {
			_log.error(e);

			return;
		}

		if (calendarResource == null) {
			failurePropElement.addElement(
				CalDAVProps.CALDAV_CALENDAR_USER_TYPE);
		}

		String className = calendarResource.getClassName();
		String cutypeparam = "UNKNOWN";

		if (CalendarHelperUtil.isCalendarResourceUserCalendar(
				calendarResource)) {

			cutypeparam = "INDIVIDUAL";
		}
		else if (className.equals(CalendarResource.class.getName())) {
			cutypeparam = "RESOURCE";
		}
		else if (className.equals(Group.class.getName())) {
			cutypeparam = "GROUP";
		}

		successPropElement.addElement(CalDAVProps.CALDAV_CALENDAR_USER_TYPE);
		successPropElement.addText(cutypeparam);
	}

	protected void processCalDAVGetCTag() {
		failurePropElement.addElement(CalDAVProps.CALDAV_GETCTAG);
	}

	protected void processCalDAVGetETag() {
		failurePropElement.addElement(CalDAVProps.CALDAV_GETETAG);
	}

	protected void processCalDAVMaxResourceSize() {
		failurePropElement.addElement(CalDAVProps.CALDAV_MAX_RESOURCE_SIZE);
	}

	protected void processCalDAVSupportedCalendarComponentSet() {
		failurePropElement.addElement(
			CalDAVProps.CALDAV_SUPPORTED_CALENDAR_COMPONENT_SET);
	}

	protected void processCalDAVSupportedCalendarData() {
		failurePropElement.addElement(
			CalDAVProps.CALDAV_SUPPORTED_CALENDAR_DATA);
	}

	protected void processCalDAVValidCalendarData() {
		failurePropElement.addElement(CalDAVProps.CALDAV_VALID_CALENDAR_DATA);
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

				successPropElement.addElement(qname);
				successPropElement.addText(text);
			}
			else {
				failurePropElement.addElement(qname);
			}
		}
	}

	protected void processDAVCreationDate() {
		successPropElement.addElement(CalDAVProps.DAV_CREATIONDATE);
		successPropElement.addText(resource.getCreateDateString());
	}

	protected void processDAVCurrentUserPrincipal() {
		if (CalDAVUtil.isIOS(webDAVRequest) ||
			CalDAVUtil.isICal(webDAVRequest)) {

			failurePropElement.addElement(
				CalDAVProps.DAV_CURRENT_USER_PRINCIPAL);

			return;
		}

		// TODO: review from specifications

		Element principalUrlElement = successPropElement.addElement(
			CalDAVProps.DAV_CURRENT_USER_PRINCIPAL);

		principalUrlElement.addElement(CalDAVProps.createQName("href"));
		principalUrlElement.addText(
			CalDAVUtil.getPrincipalURL(CalDAVUtil.getUserId(webDAVRequest)));
	}

	protected void processDAVCurrentUserPrivilegeSet() {
		failurePropElement.addElement(
			CalDAVProps.DAV_CURRENT_USER_PRIVILEGE_SET);
	}

	protected void processDAVDisplayName() {
		successPropElement.addElement(CalDAVProps.DAV_DISPLAYNAME);
		successPropElement.addText(
			resource.getDisplayName().replaceAll(
				StringPool.SPACE, StringPool.BLANK));
	}

	protected void processDAVGetContentLength() {
		failurePropElement.addElement(CalDAVProps.DAV_GETCONTENTLENGTH);
	}

	protected void processDAVGetContentType() {
		if (resource instanceof CalendarBookingResourceImpl) {
			successPropElement.addElement(CalDAVProps.DAV_GETCONTENTTYPE);
			successPropElement.addText(resource.getContentType());
		}
		else {
			failurePropElement.addElement(CalDAVProps.DAV_GETCONTENTTYPE);
		}
	}

	protected void processDAVGetETag() {
		String id = CalDAVUtil.getResourceETag(resource);

		successPropElement.addElement(CalDAVProps.DAV_GETETAG);
		successPropElement.addText(id);
	}

	protected void processDAVGetLastModified() {
		successPropElement.addElement(CalDAVProps.DAV_GETLASTMODIFIED);
		successPropElement.addText(resource.getModifiedDate());
	}

	protected void processDAVIsReadOnly() {
		successPropElement.addElement(CalDAVProps.DAV_ISREADONLY);
		successPropElement.addText(String.valueOf(resource.isLocked()));
	}

	protected void processDAVOwner() {
		failurePropElement.addElement(CalDAVProps.DAV_OWNER);
	}

	protected void processDAVPrincipalCollectionSet() {
		CalendarResource resource = null;

		try {
			resource = CalendarResourceLocalServiceUtil.fetchCalendarResource(
				PortalUtil.getClassNameId(User.class),
				CalDAVUtil.getUserId(webDAVRequest));
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(e);
			}
		}

		if (resource != null) {
			Element calendarHomeSetElement = successPropElement.addElement(
				CalDAVProps.DAV_PRINCIPAL_COLLECTION_SET);

			calendarHomeSetElement.addElement(CalDAVProps.createQName("href"));
			calendarHomeSetElement.addText(
				CalDAVUtil.getCalendarResourceURL(resource));
		}
		else {
			failurePropElement.addElement(
				CalDAVProps.DAV_PRINCIPAL_COLLECTION_SET);
		}
	}

	protected void processDAVPrincipalURL() {
		Element principalUrlElement = successPropElement.addElement(
			CalDAVProps.DAV_PRINCIPAL_URL);

		principalUrlElement.addElement( CalDAVProps.createQName("href"));
		principalUrlElement.addText(
			CalDAVUtil.getPrincipalURL(CalDAVUtil.getUserId(webDAVRequest)));
	}

	protected void processDAVResourceId() {
		failurePropElement.addElement(CalDAVProps.DAV_RESOURCE_ID);
	}

	protected void processDAVResourceType() {
		Element resourceTypeElement = successPropElement.addElement(
			CalDAVProps.DAV_RESOURCETYPE);

		resourceTypeElement.addElement(CalDAVProps.createQName("collection"));
	}

	protected void processDAVSource() {
		successPropElement.addElement(CalDAVProps.DAV_SOURCE);
	}

	protected void processDAVSupportedReportSet() {
		Element supportedResportSetElement = successPropElement.addElement(
			CalDAVProps.DAV_SUPPORTED_REPORT_SET);

		for (String reportSet : CalDAVMethod.SUPPORTED_CALDAV_REPORT_SET) {
			Element supportedResportElement =
				supportedResportSetElement.addElement(
					CalDAVProps.createQName("supported-report"));

			supportedResportElement.addElement(
				CalDAVProps.createQName("report"));
			supportedResportElement.addText(reportSet);
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