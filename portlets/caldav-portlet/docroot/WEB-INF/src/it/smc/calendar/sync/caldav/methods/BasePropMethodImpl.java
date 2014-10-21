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
import com.liferay.portal.kernel.util.ContentTypes;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.webdav.Resource;
import com.liferay.portal.kernel.webdav.WebDAVRequest;
import com.liferay.portal.kernel.webdav.WebDAVStorage;
import com.liferay.portal.kernel.webdav.WebDAVUtil;
import com.liferay.portal.kernel.webdav.methods.Method;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.QName;
import com.liferay.portal.kernel.xml.SAXReaderUtil;

import it.smc.calendar.sync.caldav.PropsProcessor;
import it.smc.calendar.sync.caldav.util.CalDAVProps;
import it.smc.calendar.sync.caldav.util.CalDAVPropsProcessorFactory;
import it.smc.calendar.sync.caldav.util.CalDAVUtil;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
public abstract class BasePropMethodImpl implements Method {

	protected void addResponse(
			WebDAVRequest webDAVRequest, Resource resource, Element multistatus,
			Set<QName> props)
		throws Exception {

		// Make a deep copy of the props

		if (props.contains(CalDAVProps.DAV_ALLPROP)) {
			props.remove(CalDAVProps.DAV_ALLPROP);

			if (resource.isCollection()) {
				props.addAll(CalDAVProps.getAllCollectionProps());
			}
			else {
				props.addAll(CalDAVProps.getAllResourceProps());
			}
		}

		PropsProcessor propsProcessor = CalDAVPropsProcessorFactory.create(
			webDAVRequest, resource, multistatus);

		propsProcessor.processProperties(props);
	}

	protected void addResponse(
			WebDAVStorage storage, WebDAVRequest webDAVRequest,
			Resource resource, Set<QName> props, Element multistatusElement,
			long depth)
		throws Exception {

		addResponse(webDAVRequest, resource, multistatusElement, props);

		if (resource.isCollection() && (depth != 0)) {
			List<Resource> calendarResources = storage.getResources(
				webDAVRequest);

			for (Resource calendarResource : calendarResources) {
				addResponse(
					webDAVRequest, calendarResource, multistatusElement, props);
			}
		}
	}

	protected int writeResponseXML(
			WebDAVRequest webDAVRequest, Set<QName> props)
		throws Exception {

		WebDAVStorage storage = webDAVRequest.getWebDAVStorage();

		long depth = WebDAVUtil.getDepth(webDAVRequest.getHttpServletRequest());

		Document document = SAXReaderUtil.createDocument();

		Element multistatusElement = SAXReaderUtil.createElement(
			CalDAVProps.createQName("multistatus"));

		document.setRootElement(multistatusElement);

		Resource resource = storage.getResource(webDAVRequest);

		if (resource != null) {
			addResponse(
				storage, webDAVRequest, resource, props, multistatusElement,
				depth);

			String xml = document.formattedString(StringPool.FOUR_SPACES);

			if (_log.isDebugEnabled()) {
				_log.debug("Response XML\n" + xml);
			}

			// Set the status prior to writing the XML

			int status = WebDAVUtil.SC_MULTI_STATUS;

			HttpServletResponse response =
				webDAVRequest.getHttpServletResponse();

			response.setContentType(ContentTypes.TEXT_XML_UTF8);
			response.setStatus(status);

			try {
				ServletResponseUtil.write(response, xml);

				response.flushBuffer();
			}
			catch (Exception e) {
				if (_log.isWarnEnabled()) {
					_log.warn(e);
				}
			}

			return status;
		}
		else {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"No resource found for " + storage.getRootPath() +
						webDAVRequest.getPath());
			}

			return HttpServletResponse.SC_NOT_FOUND;
		}
	}

	private static Log _log = LogFactoryUtil.getLog(BasePropMethodImpl.class);

}