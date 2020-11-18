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

package it.smc.calendar.caldav.sync.listener;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Fabio Pezzutto
 */
public class ICSContentImportExportFactoryUtil {

	public static ICSImportExportListener newInstance() {
		return _serviceTracker.getService();
	}

	private static ServiceTracker
		<ICSImportExportListener, ICSImportExportListener> _serviceTracker;

	static {
		Bundle bundle = FrameworkUtil.getBundle(ICSImportExportListener.class);

		ServiceTracker<ICSImportExportListener, ICSImportExportListener>
			serviceTracker = new ServiceTracker<>(
				bundle.getBundleContext(), ICSImportExportListener.class, null);

		serviceTracker.open();

		_serviceTracker = serviceTracker;
	}

}