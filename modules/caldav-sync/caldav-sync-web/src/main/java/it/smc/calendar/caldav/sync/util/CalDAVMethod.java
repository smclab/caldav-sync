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

package it.smc.calendar.caldav.sync.util;

import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.webdav.methods.Method;

/**
 * @author Fabio Pezzutto
 */
public interface CalDAVMethod extends Method {

	public static final String POST = "POST";

	public static final String REPORT = "REPORT";

	public static final String REPORT_EXPAND_PROPERTY = "expand-property";

	public static final String REPORT_PRINCIPAL_PROPERTY_SEARCH =
		"principal-property-search";

	public static final String REPORT_PRINCIPAL_SEARCH_PROPERTY_SET =
		"principal-search-property-set";

	public static final String REPORT_SYNC_COLLECTION = "sync-collection";

	public static final String[] SUPPORTED_CALDAV_METHODS_ARRAY =
		{ OPTIONS, PROPFIND, PROPPATCH, GET, REPORT, PUT, DELETE, POST };

	public static final String TRACE = "TRACE";

	public static String SUPPORTED_CALDAV_METHODS_NAMES = StringUtil.merge(
		SUPPORTED_CALDAV_METHODS_ARRAY);

	public static String[] SUPPORTED_CALDAV_REPORT_SET = {
		REPORT_EXPAND_PROPERTY, REPORT_PRINCIPAL_PROPERTY_SEARCH,
		REPORT_PRINCIPAL_SEARCH_PROPERTY_SET, REPORT_SYNC_COLLECTION };

}