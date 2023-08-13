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
import it.smc.calendar.caldav.sync.util.CalendarResourceModelPermission;
import it.smc.calendar.caldav.sync.util.DocUtil;
import it.smc.calendar.caldav.util.CalendarUtil;

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
		Element calendarHomeSetElement = successPropElement.addElement(
			CalDAVProps.CALDAV_CALENDAR_HOME_SET);

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
					calendarHomeSetElement.addElement(
						CalDAVProps.createQName("href"));
					calendarHomeSetElement.addText(
						CalDAVUtil.getCalendarResourceURL(calendarResource));

					return;
				}
			}

			List<Calendar> allCalendars = CalendarUtil.getAllCalendars(
				webDAVRequest.getPermissionChecker());

			long[] calendarResourceIds = allCalendars.stream(
			).mapToLong(
				cal -> cal.getCalendarResourceId()
			).distinct(
			).toArray();

			for (long calendarResourceId : calendarResourceIds) {
				CalendarResource calendarResource =
					CalendarResourceLocalServiceUtil.fetchCalendarResource(
						calendarResourceId);

				if ((calendarResource != null) &&
					CalendarResourceModelPermission.contains(
						webDAVRequest.getPermissionChecker(), calendarResource,
						ActionKeys.VIEW)) {

					calendarHomeSetElement.addElement(
						CalDAVProps.createQName("href"));
					calendarHomeSetElement.addText(
						CalDAVUtil.getCalendarResourceURL(calendarResource));
				}
			}
		}
		catch (Exception e) {
			_log.error(e);
		}
	}

	@Override
	protected void processDAVOwner() {
		successPropElement.addElement(CalDAVProps.DAV_OWNER);
		successPropElement.addText(
			CalDAVUtil.getPrincipalURL(CalDAVUtil.getUserId(webDAVRequest)));
	}

	@Override
	protected void processDAVResourceType() {
		Element resourceTypeElement = successPropElement.addElement(
			CalDAVProps.DAV_RESOURCETYPE);

		resourceTypeElement.addElement(CalDAVProps.createQName("collection"));
		resourceTypeElement.addElement(CalDAVProps.createQName("principal"));
	}

	private static Log _log = LogFactoryUtil.getLog(
		PrincipalPropsProcessor.class);

}
