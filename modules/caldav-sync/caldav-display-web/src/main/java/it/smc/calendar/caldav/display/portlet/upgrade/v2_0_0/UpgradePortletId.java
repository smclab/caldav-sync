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

package it.smc.calendar.caldav.display.portlet.upgrade.v2_0_0;

import com.liferay.portal.kernel.upgrade.BasePortletIdUpgradeProcess;
import it.smc.calendar.caldav.display.portlet.constants.CalDAVDisplayPortletKeys;

/**
 * @author Mirko Zizzari
 */
public class UpgradePortletId extends BasePortletIdUpgradeProcess {

	@Override
	protected String[][] getRenamePortletIdsArray() {
		return new String[][] {
			{
				_oldPortlet,
				CalDAVDisplayPortletKeys.CalDAVDisplay
			}
		};
	}

	private String _oldPortlet = "it_smc_calendar_caldav_display_portlet_portlet_CalDAVDisplayPortlet";
}
