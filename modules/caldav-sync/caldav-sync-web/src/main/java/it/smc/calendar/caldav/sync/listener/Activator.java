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

import com.liferay.portal.kernel.util.BasePortalLifecycle;
import com.liferay.portal.kernel.webdav.WebDAVStorage;
import com.liferay.portal.kernel.webdav.WebDAVUtil;
import com.liferay.portal.kernel.webdav.methods.MethodFactory;
import com.liferay.portal.kernel.webdav.methods.MethodFactoryRegistryUtil;

import it.smc.calendar.caldav.sync.CalDAVMethodFactory;
import it.smc.calendar.caldav.sync.LiferayCalDAVStorageImpl;
import it.smc.calendar.caldav.sync.util.WebKeys;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

/**
 * @author bta
 */
public class Activator
	extends BasePortalLifecycle implements BundleActivator, BundleListener {

	@Override
	public void bundleChanged(BundleEvent event) {
		registerPortalLifecycle();
	}

	@Override
	public void start(BundleContext context) throws Exception {
		context.addBundleListener(this);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		portalDestroy();
		context.removeBundleListener(this);
	}

	@Override
	protected void doPortalDestroy() throws Exception {

		if (_methodFactory != null) {
			MethodFactoryRegistryUtil.unregisterMethodFactory(_methodFactory);
		}
	}

	@Override
	protected void doPortalInit() throws Exception {
		_methodFactory = new CalDAVMethodFactory();

		MethodFactoryRegistryUtil.registerMethodFactory(_methodFactory);
	}

	private MethodFactory _methodFactory;

}