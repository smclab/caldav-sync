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

package it.smc.calendar.caldav.helper.api;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Mirko Zizzari
 */
public class UserAgentHelperUtil {

	public static UserAgentHelper getService() {
		if (_serviceTracker == null) {
			Bundle bundle = FrameworkUtil.getBundle(CalendarHelper.class);

			_serviceTracker = new ServiceTracker<>(
				bundle.getBundleContext(), UserAgentHelper.class, null);

			_serviceTracker.open();
		}

		return _serviceTracker.getService();
	}

	public static boolean isAndroid(String userAgent) {
		return getService().isAndroid(userAgent);
	}

	public static boolean isAndroidCalDAVSyncAdapter(String userAgent) {
		return getService().isAndroidCalDAVSyncAdapter(userAgent);
	}

	public static boolean isICal(String userAgent) {
		return getService().isICal(userAgent);
	}

	public static boolean isIOS(String userAgent) {
		return getService().isIOS(userAgent);
	}

	public static boolean isMacOSX(String userAgent) {
		return getService().isMacOSX(userAgent);
	}

	public static boolean isOpenSync(String userAgent) {
		return getService().isOpenSync(userAgent);
	}

	public static boolean isThunderbird(String userAgent) {
		return getService().isThunderbird(userAgent);
	}

	private static ServiceTracker<UserAgentHelper, UserAgentHelper>
		_serviceTracker;

}