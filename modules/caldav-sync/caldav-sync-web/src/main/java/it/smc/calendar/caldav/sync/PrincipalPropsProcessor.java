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
import com.liferay.calendar.service.CalendarResourceLocalServiceUtil;

import com.liferay.petra.xml.DocUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.Element;

import it.smc.calendar.caldav.sync.util.CalDAVProps;
import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import it.smc.calendar.caldav.sync.util.CustomCalendarResourcePermission;
import it.smc.calendar.caldav.util.CalendarUtil;
import org.osgi.service.component.annotations.Component;

import java.util.List;

/**
 * @author Fabio Pezzutto
 */

public class PrincipalPropsProcessor extends BasePropsProcessor {

	public PrincipalPropsProcessor(
		WebDAVRequest webDAVRequest, Resource resource, Element rootElement) {

		super(webDAVRequest, resource, rootElement);
	}

	@Override
	protected void processCalDAVCalendarHomeSet() {
		Element calendarHomeSetElement = DocUtil.add(
			successPropElement, CalDAVProps.CALDAV_CALENDAR_HOME_SET);

		try {
			if (CalDAVUtil.isIOS(webDAVRequest) ||
				CalDAVUtil.isICal(webDAVRequest)) {

				CalendarResource calendarResource =
					CalendarResourceLocalServiceUtil.fetchCalendarResource(
						PortalUtil.getClassNameId(User.class),
						CalDAVUtil.getUserId(webDAVRequest));

				if ((calendarResource == null) && _log.isWarnEnabled()) {
					_log.warn(
						"No calendar resource found for userId " +
							CalDAVUtil.getUserId(webDAVRequest));
				}

				if (calendarResource != null) {
					DocUtil.add(
						calendarHomeSetElement, CalDAVProps.createQName("href"),
						CalDAVUtil.getCalendarResourceURL(calendarResource));

					return;
				}
			}

			List<Calendar> allCalendars = CalendarUtil.getAllCalendars(
				webDAVRequest.getPermissionChecker());

			long[] calendarResourceIds = allCalendars.stream().mapToLong(
				cal -> cal.getCalendarResourceId()).distinct().toArray();

			for (long calendarResourceId : calendarResourceIds) {
				CalendarResource calendarResource =
					CalendarResourceLocalServiceUtil.fetchCalendarResource(
						calendarResourceId);

				if ((calendarResource != null) &&
					CustomCalendarResourcePermission.contains(
						webDAVRequest.getPermissionChecker(), calendarResource,
						ActionKeys.VIEW)) {

					DocUtil.add(
						calendarHomeSetElement, CalDAVProps.createQName("href"),
						CalDAVUtil.getCalendarResourceURL(calendarResource));
				}
			}
		}
		catch (Exception e) {
			_log.error(e);
			return;
		}
	}

	@Override
	protected void processDAVOwner() {
		DocUtil.add(
			successPropElement, CalDAVProps.DAV_OWNER,
			CalDAVUtil.getPrincipalURL(CalDAVUtil.getUserId(webDAVRequest)));
	}

	@Override
	protected void processDAVResourceType() {
		Element resourceTypeElement = DocUtil.add(
			successPropElement, CalDAVProps.DAV_RESOURCETYPE);

		DocUtil.add(resourceTypeElement, CalDAVProps.createQName("collection"));

		DocUtil.add(resourceTypeElement, CalDAVProps.createQName("principal"));
	}

	private static Log _log = LogFactoryUtil.getLog(
		PrincipalPropsProcessor.class);

}