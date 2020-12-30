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

package it.smc.calendar.caldav.helper.util;

import com.liferay.portal.kernel.util.GetterUtil;

/**
 * @author Fabio Pezzutto
 */
public class PropsValues {

	public static final boolean HIDE_PERSONAL_CALENDAR = GetterUtil.getBoolean(
		ConfigurationUtil.get(PropsKeys.HIDE_PERSONAL_CALENDAR));

	public static final String INVITED_USERS_CUSTOM_FIELD_NAME =
		GetterUtil.getString(
			ConfigurationUtil.get(PropsKeys.INVITED_USERS_CUSTOM_FIELD_NAME));

	public static final String INVITED_USERS_LABEL_CUSTOM_FIELD_NAME =
		GetterUtil.getString(
			ConfigurationUtil.get(
				PropsKeys.INVITED_USERS_LABEL_CUSTOM_FIELD_NAME));

	public static final boolean PROPFIND_PROVIDE_SESSIONCLICKS_CALENDARS =
		GetterUtil.getBoolean(
			ConfigurationUtil.get(
				PropsKeys.PROPFIND_PROVIDE_SESSIONCLICKS_CALENDARS));

	public static final boolean PROPFIND_PROVIDE_USER_GROUPS =
		GetterUtil.getBoolean(
			ConfigurationUtil.get(PropsKeys.PROPFIND_PROVIDE_USER_GROUPS));

	public static final String[] USERAGENT_FINDER_ANDROID =
		GetterUtil.getStringValues(
			ConfigurationUtil.getArray(PropsKeys.USERAGENT_FINDER_ANDROID));

	public static final String[] USERAGENT_FINDER_ANDROID_ICAL_ADAPTER =
		GetterUtil.getStringValues(
			ConfigurationUtil.getArray(
				PropsKeys.USERAGENT_FINDER_ANDROID_ICAL_ADAPTER));

	public static final String[] USERAGENT_FINDER_ICAL =
		GetterUtil.getStringValues(
			ConfigurationUtil.getArray(PropsKeys.USERAGENT_FINDER_ICAL));

	public static final String[] USERAGENT_FINDER_IOS =
		GetterUtil.getStringValues(
			ConfigurationUtil.getArray(PropsKeys.USERAGENT_FINDER_IOS));

	public static final String[] USERAGENT_FINDER_MACOSX =
		GetterUtil.getStringValues(
			ConfigurationUtil.getArray(PropsKeys.USERAGENT_FINDER_MACOSX));

	public static final String[] USERAGENT_FINDER_OPENSYNC =
		GetterUtil.getStringValues(
			ConfigurationUtil.getArray(PropsKeys.USERAGENT_FINDER_OPENSYNC));

	public static final String[] USERAGENT_FINDER_THUNDERBIRD =
		GetterUtil.getStringValues(
			ConfigurationUtil.getArray(PropsKeys.USERAGENT_FINDER_THUNDERBIRD));

}