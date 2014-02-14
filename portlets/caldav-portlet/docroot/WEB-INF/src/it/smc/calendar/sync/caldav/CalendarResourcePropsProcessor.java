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
import com.liferay.calendar.service.permission.CalendarResourcePermission;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.util.xml.DocUtil;

import it.smc.calendar.sync.caldav.util.CalDAVProps;
import it.smc.calendar.sync.caldav.util.CalDAVUtil;

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

		DocUtil.add(
			calendarHomeSetElement, CalDAVProps.createQName("href"),
			CalDAVUtil.getCalendarResourceURL(_calendarResource));
	}

	@Override
	protected void processDAVCurrentUserPrivilegeSet() {
		Element currentUserPrivilegeSetElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_CURRENT_USER_PRIVILEGE_SET);

		Element readPrivilegeElement = DocUtil.add(
			currentUserPrivilegeSetElement,
			CalDAVProps.createQName("privilege"));

		DocUtil.add(readPrivilegeElement, CalDAVProps.createQName("read"));

		if (CalendarResourcePermission.contains(
				webDAVRequest.getPermissionChecker(), _calendarResource,
				ActionKeys.UPDATE)) {

			DocUtil.add(readPrivilegeElement, CalDAVProps.createQName("write"));

			DocUtil.add(
				readPrivilegeElement, CalDAVProps.createQName("write-content"));
		}
	}

	private CalendarResource _calendarResource;

}