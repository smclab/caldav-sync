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

import com.liferay.calendar.model.Calendar;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.security.permission.resource.ModelResourcePermission;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 *@author Domenico Costa
 */
@Component(immediate = true, service = {})
public class CalendarModelPermission {

	public static boolean contains(
		PermissionChecker permissionChecker, Calendar calendar,
		String actionId) {

		try {
			return _calendarModelResourcePermission.contains(
				permissionChecker, calendar, actionId);
		}
		catch (PortalException e) {
			_log.error(e.getClass() + " " + e.getMessage());
		}

		return false;
	}

	public static boolean contains(
		PermissionChecker permissionChecker, long calendarId, String actionId) {

		try {
			return _calendarModelResourcePermission.contains(
				permissionChecker, calendarId, actionId);
		}
		catch (PortalException e) {
			//ignore
		}

		return false;
	}

	@Reference(
		target = "(model.class.name=com.liferay.calendar.model.Calendar)",
		unbind = "-"
	)
	protected void setModelPermissionChecker(
		ModelResourcePermission<Calendar> modelResourcePermission) {

		_calendarModelResourcePermission = modelResourcePermission;
	}

	private static final Log _log = LogFactoryUtil.getLog(
		CalendarModelPermission.class);

	private static ModelResourcePermission<Calendar>
		_calendarModelResourcePermission;

}