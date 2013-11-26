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

package it.smc.calendar.sync.caldav.methods;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.ServletResponseUtil;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVException;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.webdav.WebDAVStorage;
import com.liferay.portal.kernel.webdav.methods.Method;
import com.liferay.portal.security.auth.PrincipalException;

import it.smc.calendar.sync.caldav.ResourceNotFoundException;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public class GetMethodImpl implements Method {

	@Override
	public int process(WebDAVRequest webDAVRequest) throws WebDAVException {
		InputStream is = null;

		try {
			WebDAVStorage storage = webDAVRequest.getWebDAVStorage();
			HttpServletRequest request = webDAVRequest.getHttpServletRequest();
			HttpServletResponse response =
				webDAVRequest.getHttpServletResponse();

			Resource resource = storage.getResource(webDAVRequest);

			if (resource == null) {
				return HttpServletResponse.SC_NOT_FOUND;
			}

			try {
				is = resource.getContentAsStream();
			}
			catch (Exception e) {
				if (_log.isErrorEnabled()) {
					_log.error(e.getMessage());
				}
			}

			if (is != null) {
				try {
					ServletResponseUtil.sendFile(
						request, response, resource.getDisplayName(), is,
						resource.getSize(), resource.getContentType());
				}
				catch (Exception e) {
					if (_log.isWarnEnabled()) {
						_log.warn(e);
					}
				}

				return HttpServletResponse.SC_OK;
			}

			return HttpServletResponse.SC_NOT_FOUND;
		}
		catch (ResourceNotFoundException rnfe) {
			return HttpServletResponse.SC_NOT_FOUND;
		}
		catch (WebDAVException pe) {
			if (pe.getCause() instanceof PrincipalException) {
				return HttpServletResponse.SC_UNAUTHORIZED;
			}
			else if (pe.getCause() instanceof ResourceNotFoundException) {
				return HttpServletResponse.SC_NOT_FOUND;
			}

			throw pe;
		}
		catch (Exception e) {
			throw new WebDAVException(e);
		}
	}

	private static Log _log = LogFactoryUtil.getLog(GetMethodImpl.class);

}