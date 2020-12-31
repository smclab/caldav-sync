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

/**
 * @author Mirko Zizzari
 */
public interface UserAgentHelper {

	public boolean isAndroid(String userAgent);

	public boolean isAndroidCalDAVSyncAdapter(String userAgent);

	public boolean isICal(String userAgent);

	public boolean isIOS(String userAgent);

	public boolean isMacOSX(String userAgent);

	public boolean isOpenSync(String userAgent);

	public boolean isThunderbird(String userAgent);

}