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

import com.liferay.calendar.model.CalendarResource;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/*
 *@author Domenico Costa
 */
@Component(immediate = true, service = {})
public class CustomCalendarResourcePermission {

	public static boolean contains(
			PermissionChecker permissionChecker,
			CalendarResource calendarResource, String actionId)
		throws PortalException {

		return _calendarResourceModelResourcePermission.contains(
			permissionChecker, calendarResource, actionId);
	}

	@Reference(
		target = "(model.class.name=com.liferay.calendar.model.CalendarResource)",
		unbind = "-"
	)
	protected void setModelPermissionChecker(
		ModelResourcePermission<CalendarResource> modelResourcePermission) {

		_calendarResourceModelResourcePermission = modelResourcePermission;
	}

	private static ModelResourcePermission<CalendarResource>
		_calendarResourceModelResourcePermission;
}
