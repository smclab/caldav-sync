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

import com.liferay.portal.kernel.util.ReleaseInfo;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

/**
 * @author Fabio Pezzutto
 */
public class ICalUtil {

	public static Calendar getVTimeZoneCalendar() {
		TimeZoneRegistry registry =
			TimeZoneRegistryFactory.getInstance().createRegistry();

		TimeZone timeZone = registry.getTimeZone("GMT");

		VTimeZone vTimeZone = timeZone.getVTimeZone();

		net.fortuna.ical4j.model.Calendar iCalCalendar =
			new net.fortuna.ical4j.model.Calendar();

		PropertyList propertiesList = iCalCalendar.getProperties();

		ProdId prodId = new ProdId(
			"-//Liferay Inc//Liferay Portal " + ReleaseInfo.getVersion() +
			"//EN");

		propertiesList.add(prodId);
		propertiesList.add(Version.VERSION_2_0);

		iCalCalendar.getComponents().add(vTimeZone);

		return iCalCalendar;
	}

}
