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

package it.smc.calendar.caldav.sync.methods;

import com.liferay.portal.kernel.servlet.HttpHeaders;
import com.liferay.portal.kernel.webdav.WebDAVException;
import com.liferay.portal.kernel.webdav.WebDAVRequest;

import it.smc.calendar.caldav.sync.util.CalDAVMethod;

import javax.servlet.http.HttpServletResponse;

/**
 * @author Fabio Pezzutto
 */
public class OptionsMethodImpl implements CalDAVMethod {

	@Override
	public int process(WebDAVRequest webDAVRequest) throws WebDAVException {
		HttpServletResponse response = webDAVRequest.getHttpServletResponse();

		StringBuilder sb = new StringBuilder();

		if (webDAVRequest.getWebDAVStorage().isSupportsClassTwo()) {
			sb.append("1,2, ");
		}
		else {
			sb.append("1, ");
		}

		sb.append("calendar-access, ");
		sb.append("calendar-auto-schedule");

		response.addHeader("DAV", sb.toString());

		response.addHeader("Allow", SUPPORTED_CALDAV_METHODS_NAMES);
		response.addHeader("MS-Author-Via", "DAV");

		response.addHeader(HttpHeaders.CONTENT_LENGTH, "0");

		return HttpServletResponse.SC_OK;
	}

}