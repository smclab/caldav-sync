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

package it.smc.calendar.sync.caldav;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.AutoResetThreadLocal;
import com.liferay.portal.kernel.xml.Document;
import com.liferay.portal.kernel.xml.DocumentException;
import com.liferay.portal.kernel.xml.SAXReaderUtil;

/**
 * @author Fabio Pezzutto
 */
public class CalDAVRequestThreadLocal {

	public static String getRequestContent() {
		return _content.get();
	}

	public static Document getRequestDocument() {

		if (_document.get() == null) {
			try {
				setRequestDocument(SAXReaderUtil.read(_content.get()));
			}
			catch (DocumentException e) {
				_log.error(e);
			}
		}

		return _document.get();
	}

	public static void setRequestContent(String content) {
		_content.set(content);
	}

	public static void setRequestDocument(Document content) {
		_document.set(content);
	}

	private static Log _log = LogFactoryUtil.getLog(
		CalDAVRequestThreadLocal.class);

	private static ThreadLocal<String> _content =
		new AutoResetThreadLocal<String>(
			CalDAVRequestThreadLocal.class + "._content");
	private static ThreadLocal<Document> _document =
		new AutoResetThreadLocal<Document>(
			CalDAVRequestThreadLocal.class + "._document");

}