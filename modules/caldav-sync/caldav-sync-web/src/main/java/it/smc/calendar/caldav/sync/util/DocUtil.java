/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.kernel.xml.Namespace;
import com.liferay.portal.kernel.xml.QName;
import com.liferay.portal.kernel.xml.SAXReaderUtil;

/**
 * Liferay removed this class between 7.4 GA30 and 7.4 GA40. Our code and the
 * code we use still need it.
 * <br/>
 *
 * @author Brian Wing Shun Chan
 * @author Alexander Chow
 */
public class DocUtil {

	public static Element add(Element element, QName qName) {
		return element.addElement(qName);
	}

	public static Element add(Element element, QName qName, boolean text) {
		return add(element, qName, String.valueOf(text));
	}

	public static Element add(Element element, QName qName, double text) {
		return add(element, qName, String.valueOf(text));
	}

	public static Element add(Element element, QName qName, float text) {
		return add(element, qName, String.valueOf(text));
	}

	public static Element add(Element element, QName qName, int text) {
		return add(element, qName, String.valueOf(text));
	}

	public static Element add(Element element, QName qName, long text) {
		return add(element, qName, String.valueOf(text));
	}

	public static Element add(Element element, QName qName, Object text) {
		return add(element, qName, String.valueOf(text));
	}

	public static Element add(Element element, QName qName, short text) {
		return add(element, qName, String.valueOf(text));
	}

	public static Element add(Element element, QName qName, String text) {
		Element childElement = element.addElement(qName);

		childElement.addText(GetterUtil.getString(text));

		return childElement;
	}

	public static Element add(Element element, String name, boolean text) {
		return add(element, name, String.valueOf(text));
	}

	public static Element add(Element element, String name, double text) {
		return add(element, name, String.valueOf(text));
	}

	public static Element add(Element element, String name, float text) {
		return add(element, name, String.valueOf(text));
	}

	public static Element add(Element element, String name, int text) {
		return add(element, name, String.valueOf(text));
	}

	public static Element add(Element element, String name, long text) {
		return add(element, name, String.valueOf(text));
	}

	public static Element add(
		Element element, String name, Namespace namespace) {

		QName qName = SAXReaderUtil.createQName(name, namespace);

		return element.addElement(qName);
	}

	public static Element add(
		Element element, String name, Namespace namespace, boolean text) {

		return add(element, name, namespace, String.valueOf(text));
	}

	public static Element add(
		Element element, String name, Namespace namespace, double text) {

		return add(element, name, namespace, String.valueOf(text));
	}

	public static Element add(
		Element element, String name, Namespace namespace, float text) {

		return add(element, name, namespace, String.valueOf(text));
	}

	public static Element add(
		Element element, String name, Namespace namespace, int text) {

		return add(element, name, namespace, String.valueOf(text));
	}

	public static Element add(
		Element element, String name, Namespace namespace, long text) {

		return add(element, name, namespace, String.valueOf(text));
	}

	public static Element add(
		Element element, String name, Namespace namespace, Object text) {

		return add(element, name, namespace, String.valueOf(text));
	}

	public static Element add(
		Element element, String name, Namespace namespace, short text) {

		return add(element, name, namespace, String.valueOf(text));
	}

	public static Element add(
		Element element, String name, Namespace namespace, String text) {

		QName qName = SAXReaderUtil.createQName(name, namespace);

		return add(element, qName, text);
	}

	public static Element add(Element element, String name, Object text) {
		return add(element, name, String.valueOf(text));
	}

	public static Element add(Element element, String name, short text) {
		return add(element, name, String.valueOf(text));
	}

	public static Element add(Element element, String name, String text) {
		Element childElement = element.addElement(name);

		childElement.addText(GetterUtil.getString(text));

		return childElement;
	}

}