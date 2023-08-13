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

import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.Element;
import it.smc.calendar.caldav.sync.util.CalDAVProps;
import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import it.smc.calendar.caldav.sync.util.DocUtil;

/**
 * @author Fabio Pezzutto
 */
public class CalendarBookingPropsProcessor extends BasePropsProcessor {

	public CalendarBookingPropsProcessor(
		WebDAVRequest webDAVRequest, Resource resource, Element rootElement) {

		super(webDAVRequest, resource, rootElement);
	}

	@Override
	protected void processCalDAVGetCTag() {
		successPropElement.addElement(CalDAVProps.CALDAV_GETCTAG);
		successPropElement.addText(CalDAVUtil.getResourceETag(resource));
	}

	@Override
	protected void processDAVResourceType() {
		successPropElement.addElement(CalDAVProps.DAV_RESOURCETYPE);
	}

}