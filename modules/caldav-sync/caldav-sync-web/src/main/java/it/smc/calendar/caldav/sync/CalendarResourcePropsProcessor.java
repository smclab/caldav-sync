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
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.Element;
import it.smc.calendar.caldav.helper.api.CalendarHelperUtil;
import it.smc.calendar.caldav.sync.util.CalDAVProps;
import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import it.smc.calendar.caldav.sync.util.CalendarResourceModelPermission;

import java.util.Optional;

/**
 * @author Fabio Pezzutto
 */
public class CalendarResourcePropsProcessor extends BasePropsProcessor {

	public CalendarResourcePropsProcessor(
		WebDAVRequest webDAVRequest, Resource resource, Element rootElement) {

		super(webDAVRequest, resource, rootElement);

		_calendarResource = (CalendarResource)resource.getModel();
	}

	@Override
	protected void processCalDAVCalendarHomeSet() {

		Element calendarHomeSetElement = successPropElement.addElement(
			CalDAVProps.CALDAV_CALENDAR_HOME_SET);

		calendarHomeSetElement.addElement(
			CalDAVProps.createQName("href")).addText(
				CalDAVUtil.getCalendarResourceURL(_calendarResource));
	}

	@Override
	protected void processCalDAVCalendarUserAddressSet() {

		Element calendarHomeSetElement = successPropElement.addElement(
			CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET);

		String address = CalDAVUtil.getCalendarResourceURL(_calendarResource);

		Optional<User> user = CalendarHelperUtil.getCalendarResourceUser(
			_calendarResource);

		if (user.isPresent()) {
			address =
				"mailto:" +
					user.get(
					).getEmailAddress();
		}

		calendarHomeSetElement.addElement(
			CalDAVProps.createQName("href")).addText(address);
	}

	@Override
	protected void processDAVCurrentUserPrivilegeSet() {
		Element currentUserPrivilegeSetElement = successPropElement.addElement(
			 CalDAVProps.DAV_CURRENT_USER_PRIVILEGE_SET);

		Element readPrivilegeElement =
			currentUserPrivilegeSetElement.addElement(
				CalDAVProps.createQName("privilege"));

		readPrivilegeElement.addElement( CalDAVProps.createQName("read"));

		if (CalendarResourceModelPermission.contains(
				webDAVRequest.getPermissionChecker(), _calendarResource,
				ActionKeys.UPDATE)) {

			readPrivilegeElement.addElement(CalDAVProps.createQName("write"));
			readPrivilegeElement.addElement(
				CalDAVProps.createQName("write-content"));
		}
	}

	@Override
	protected void processDAVOwner() {
		successPropElement.addElement(CalDAVProps.DAV_OWNER);
		successPropElement.addText(
			CalDAVUtil.getPrincipalURL(_calendarResource.getUserId()));
	}

	protected void processDAVResourceId() {
		Element resourceIdElement = successPropElement.addElement(
			CalDAVProps.DAV_RESOURCE_ID);
		resourceIdElement.addElement(CalDAVProps.createQName("href"));
		resourceIdElement.addText(
			"urn:uuid:".concat(_calendarResource.getUuid()));
	}

	private CalendarResource _calendarResource;

}
