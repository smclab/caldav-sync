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

import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.Element;

import it.smc.calendar.caldav.sync.CalendarBookingPropsProcessor;
import it.smc.calendar.caldav.sync.CalendarBookingResourceImpl;
import it.smc.calendar.caldav.sync.CalendarPropsProcessor;
import it.smc.calendar.caldav.sync.CalendarResourcePropsProcessor;
import it.smc.calendar.caldav.sync.CalendarResourceResourceImpl;
import it.smc.calendar.caldav.sync.PrincipalPropsProcessor;
import it.smc.calendar.caldav.sync.UserResourceImpl;

/**
 * @author Fabio Pezzutto
 */
public class CalDAVPropsProcessorFactory {

	public static PropsProcessor create(
		WebDAVRequest webDAVRequest, Resource resource, Element rootElement) {

		if (resource instanceof CalendarResourceResourceImpl) {
			return new CalendarResourcePropsProcessor(
				webDAVRequest, resource, rootElement);
		}
		else if (resource instanceof UserResourceImpl) {
			return new PrincipalPropsProcessor(
				webDAVRequest, resource, rootElement);
		}
		else if (resource instanceof CalendarBookingResourceImpl) {
			return new CalendarBookingPropsProcessor(
				webDAVRequest, resource, rootElement);
		}
		else {
			return new CalendarPropsProcessor(
				webDAVRequest, resource, rootElement);
		}
	}

}