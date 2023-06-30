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

package it.smc.calendar.caldav.sync.upgrade.v2_0_0;

import com.liferay.portal.kernel.upgrade.BasePortletIdUpgradeProcess;
import it.smc.calendar.caldav.sync.constants.CaldavSyncPortletKeys;

/**
 * @author Mirko Zizzari
 */
public class UpgradePortletId extends BasePortletIdUpgradeProcess {

	@Override
	protected String[][] getRenamePortletIdsArray() {
		return new String[][] {
			{
				_oldPortlet,
				CaldavSyncPortletKeys.CaldavSync
			}
		};
	}

	private String _oldPortlet = "CaldavSync";
}
