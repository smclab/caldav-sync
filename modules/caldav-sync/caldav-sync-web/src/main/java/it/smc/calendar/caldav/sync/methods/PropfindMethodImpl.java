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

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.webdav.WebDAVException;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.xml.QName;
import com.liferay.portal.kernel.security.auth.PrincipalException;

import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import it.smc.calendar.caldav.sync.util.CalDAVUtil;
import it.smc.calendar.caldav.sync.util.InvalidRequestException;
import it.smc.calendar.caldav.sync.util.ResourceNotFoundException;
public class PropfindMethodImpl extends BasePropMethodImpl {

	@Override
	public int process(WebDAVRequest webDAVRequest) throws WebDAVException {
		try {
			Set<QName> props = CalDAVUtil.getRequestDAVProps(webDAVRequest);

			return writeResponseXML(webDAVRequest, props);
		}
		catch (InvalidRequestException ire) {
			if (_log.isWarnEnabled()) {
				_log.warn(ire);
			}

			return HttpServletResponse.SC_BAD_REQUEST;
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

	private static Log _log = LogFactoryUtil.getLog(PropfindMethodImpl.class);

}