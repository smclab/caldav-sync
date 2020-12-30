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

package it.smc.calendar.caldav.helper;

import it.smc.calendar.caldav.helper.api.UserAgentHelper;
import it.smc.calendar.caldav.helper.util.PropsValues;

import org.osgi.service.component.annotations.Component;

/**
 * @author Mirko Zizzari
 */
@Component(immediate = true, service = UserAgentHelper.class)
public class UserAgentHelperImpl implements UserAgentHelper {

	@Override
	public boolean isAndroid(String userAgent) {
		return _contained(userAgent, PropsValues.USERAGENT_FINDER_ANDROID);
	}

	@Override
	public boolean isAndroidCalDAVSyncAdapter(String userAgent) {
		return _contained(
			userAgent, PropsValues.USERAGENT_FINDER_ANDROID_ICAL_ADAPTER);
	}

	@Override
	public boolean isICal(String userAgent) {
		return _contained(userAgent, PropsValues.USERAGENT_FINDER_ICAL);
	}

	@Override
	public boolean isIOS(String userAgent) {
		return _contained(userAgent, PropsValues.USERAGENT_FINDER_IOS);
	}

	@Override
	public boolean isMacOSX(String userAgent) {
		return _contained(userAgent, PropsValues.USERAGENT_FINDER_MACOSX);
	}

	@Override
	public boolean isOpenSync(String userAgent) {
		return _contained(userAgent, PropsValues.USERAGENT_FINDER_OPENSYNC);
	}

	@Override
	public boolean isThunderbird(String userAgent) {
		return _contained(userAgent, PropsValues.USERAGENT_FINDER_THUNDERBIRD);
	}

	private boolean _contained(String userAgent, String[] values) {
		for (String value : values) {
			if (userAgent.contains(value)) {
				return true;
			}
		}

		return false;
	}

}