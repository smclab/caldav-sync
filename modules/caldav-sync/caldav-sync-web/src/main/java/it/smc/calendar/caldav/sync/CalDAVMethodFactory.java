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

package it.smc.calendar.caldav.sync;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.FileUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.webdav.WebDAVException;
import com.liferay.portal.kernel.webdav.methods.Method;
import com.liferay.portal.kernel.webdav.methods.MethodFactory;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.security.auth.PrincipalThreadLocal;
import com.liferay.portal.kernel.util.PortalUtil;

import it.smc.calendar.caldav.sync.methods.DeleteMethodImpl;
import it.smc.calendar.caldav.sync.methods.GetMethodImpl;
import it.smc.calendar.caldav.sync.methods.OptionsMethodImpl;
import it.smc.calendar.caldav.sync.methods.PropfindMethodImpl;
import it.smc.calendar.caldav.sync.methods.ProppatchMethodImpl;
import it.smc.calendar.caldav.sync.methods.PutMethodImpl;
import it.smc.calendar.caldav.sync.methods.ReportMethodImpl;
import it.smc.calendar.caldav.sync.util.CalDAVHttpMethods;
import it.smc.calendar.caldav.sync.util.CalDAVRequestThreadLocal;
import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import it.smc.calendar.caldav.sync.util.WebKeys;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Fabio Pezzutto
 */
public class CalDAVMethodFactory implements MethodFactory {

	public CalDAVMethodFactory() {
		_methods = new HashMap<String, Object>();

		_methods.put(CalDAVHttpMethods.DELETE, new DeleteMethodImpl());
		_methods.put(CalDAVHttpMethods.GET, new GetMethodImpl());
		_methods.put(CalDAVHttpMethods.OPTIONS, new OptionsMethodImpl());
		_methods.put(CalDAVHttpMethods.PROPFIND, new PropfindMethodImpl());
		_methods.put(CalDAVHttpMethods.PROPPATCH, new ProppatchMethodImpl());
		_methods.put(CalDAVHttpMethods.PUT, new PutMethodImpl());
		_methods.put(CalDAVHttpMethods.REPORT, new ReportMethodImpl());
	}

	public Method create(HttpServletRequest request) throws WebDAVException {
		String method = request.getMethod();

		if (_log.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();

			sb.append("Serving CalDAV request: ");
			sb.append(PortalUtil.getCurrentCompleteURL(request));
			sb.append(StringPool.RETURN_NEW_LINE);
			sb.append(method);
			sb.append(StringPool.RETURN_NEW_LINE);
			sb.append("User: ");
			sb.append(PrincipalThreadLocal.getUserId());

			Enumeration<String> headerNames = request.getHeaderNames();

			String headerName;

			while (headerNames.hasMoreElements()) {
				headerName = headerNames.nextElement();
				sb.append(headerName);
				sb.append(StringPool.COLON);
				sb.append(StringPool.SPACE);
				sb.append(request.getHeader(headerName));
				sb.append(StringPool.RETURN_NEW_LINE);
			}

			_log.debug(sb.toString());
		}

		Method methodImpl = (Method)_methods.get(
			StringUtil.toUpperCase(method));

		if (methodImpl == null) {
			throw new WebDAVException(
				"Method " + method + " is not implemented");
		}

		try {
			String content = new String(
				FileUtil.getBytes(request.getInputStream()));

			if (Validator.isNotNull(content)) {
				CalDAVRequestThreadLocal.setRequestContent(content);

				if (_log.isDebugEnabled()) {
					String formattedContent = content;

					if (CalDAVUtil.isRequestContentXML(request)) {
						Document document =
							CalDAVRequestThreadLocal.getRequestDocument();
						formattedContent = document.formattedString(
							StringPool.FOUR_SPACES);
					}

					_log.debug("Request content: \n" + formattedContent);
				}
			}
		}
		catch (Exception e) {
			_log.error(e, e);
		}

		return methodImpl;
	}

	public String getType() {

		return WebKeys.CALDAV_TOKEN;
	}

	private static Log _log = LogFactoryUtil.getLog(CalDAVMethodFactory.class);

	private Map<String, Object> _methods;

}