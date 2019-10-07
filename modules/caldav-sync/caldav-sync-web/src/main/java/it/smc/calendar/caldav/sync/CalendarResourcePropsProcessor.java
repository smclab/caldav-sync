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
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.util.xml.DocUtil;

import it.smc.calendar.caldav.helper.api.CalendarHelperUtil;
import it.smc.calendar.caldav.sync.util.CalDAVProps;
import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import it.smc.calendar.caldav.sync.util.CustomCalendarResourcePermission;

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
		Element calendarHomeSetElement = DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_HOME_SET);

		DocUtil.add(
			calendarHomeSetElement, CalDAVProps.createQName("href"),
			CalDAVUtil.getCalendarResourceURL(_calendarResource));
	}

	@Override
	protected void processCalDAVCalendarUserAddressSet() {
		Element calendarHomeSetElement = DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_USER_ADDRESS_SET);

		String address = CalDAVUtil.getCalendarResourceURL(_calendarResource);

		Optional<User> user = CalendarHelperUtil.getCalendarResourceUser(
			_calendarResource);

		if (user.isPresent()) {
			address = "mailto:" + user.get().getEmailAddress();
		}

		DocUtil.add(
			calendarHomeSetElement, CalDAVProps.createQName("href"), address);
	}

	@Override
	protected void processDAVCurrentUserPrivilegeSet() {
		Element currentUserPrivilegeSetElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_CURRENT_USER_PRIVILEGE_SET);

		Element readPrivilegeElement = DocUtil.add(
			currentUserPrivilegeSetElement,
			CalDAVProps.createQName("privilege"));

		DocUtil.add(readPrivilegeElement, CalDAVProps.createQName("read"));

		try {
			if (CustomCalendarResourcePermission.contains(
					webDAVRequest.getPermissionChecker(), _calendarResource,
					ActionKeys.UPDATE)) {

				DocUtil.add(readPrivilegeElement, CalDAVProps.createQName("write"));

				DocUtil.add(
					readPrivilegeElement, CalDAVProps.createQName("write-content"));
			}
		}
		catch (PortalException e) {
			//ignore
		}
	}

	@Override
	protected void processDAVOwner() {
		DocUtil.add(
			successPropElement, CalDAVProps.DAV_OWNER,
			CalDAVUtil.getPrincipalURL(_calendarResource.getUserId()));
	}

	protected void processDAVResourceId() {
		Element resourceIdElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_RESOURCE_ID);

		DocUtil.add(
			resourceIdElement, CalDAVProps.createQName("href"),
			"urn:uuid:".concat(_calendarResource.getUuid()));
	}

	private CalendarResource _calendarResource;

}