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

package it.smc.calendar.caldav.util;

import com.liferay.portal.kernel.util.GetterUtil;

public class PropsValues {

	public static final boolean EXTENDED_DISPLAY_NAME =
		GetterUtil.getBoolean(
			ConfigurationUtil.get(PropsKeys.EXTENDED_DISPLAY_NAME), true);

}