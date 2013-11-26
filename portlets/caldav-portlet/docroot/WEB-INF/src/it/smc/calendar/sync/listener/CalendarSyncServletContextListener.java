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

package it.smc.calendar.sync.listener;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.BasePortalLifecycle;
import com.liferay.portal.kernel.webdav.WebDAVStorage;
import com.liferay.portal.kernel.webdav.WebDAVUtil;
import com.liferay.portal.kernel.webdav.methods.MethodFactory;
import com.liferay.portal.kernel.webdav.methods.MethodFactoryRegistryUtil;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.security.permission.ActionKeys;
import com.liferay.portal.service.ResourcePermissionLocalServiceUtil;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

import it.smc.calendar.sync.caldav.CalDAVMethodFactory;
import it.smc.calendar.sync.caldav.LiferayCalDAVStorageImpl;
import it.smc.calendar.sync.caldav.util.PortletKeys;
import it.smc.calendar.sync.caldav.util.WebKeys;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author Fabio Pezzutto
 */
public class CalendarSyncServletContextListener
	extends BasePortalLifecycle implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		portalDestroy();
	}

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		registerPortalLifecycle();

		// temporally workaround

		try {

			String[] actionKeys = new String[] {
				ActionKeys.ACCESS_IN_CONTROL_PANEL, ActionKeys.VIEW};

			for (long companyId : PortalUtil.getCompanyIds()) {
				Role roleUser = RoleLocalServiceUtil.getRole(
					companyId, RoleConstants.USER);

				for (String actionKey : actionKeys) {
					ResourcePermissionLocalServiceUtil.addResourcePermission(
						companyId, PortletKeys.CALDAV,
						ResourceConstants.SCOPE_COMPANY,
						String.valueOf(companyId),
						roleUser.getRoleId(), actionKey);
				}
			}
		}
		catch (Exception e) {
			_log.debug(e);
		}
	}

	@Override
	protected void doPortalDestroy() throws Exception {

		// Unregister CalDAVStorage

		if (_storage != null) {
			WebDAVUtil.deleteStorage(_storage);
		}

		// Unregister MethodFactory

		if (_methodFactory != null) {
			MethodFactoryRegistryUtil.unregisterMethodFactory(_methodFactory);
		}
	}

	@Override
	protected void doPortalInit() throws Exception {

		// Register CalDAVStorage

		_storage = new LiferayCalDAVStorageImpl();
		_storage.setToken(WebKeys.CALDAV_TOKEN);

		WebDAVUtil.addStorage(_storage);

		// Register MethodFactory

		_methodFactory = new CalDAVMethodFactory();

		MethodFactoryRegistryUtil.registerMethodFactory(_methodFactory);
	}

	private MethodFactory _methodFactory;
	private WebDAVStorage _storage;

	private static Log _log = LogFactoryUtil.getLog(
		CalendarSyncServletContextListener.class);

}
