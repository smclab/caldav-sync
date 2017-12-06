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

import com.liferay.portal.kernel.exception.PortalException;

/**
 * @author Fabio Pezzutto
 */
@SuppressWarnings("serial")
public class InvalidRequestException extends PortalException {

	public InvalidRequestException() {
		super();
	}

	public InvalidRequestException(String msg) {
		super(msg);
	}

	public InvalidRequestException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public InvalidRequestException(Throwable cause) {
		super(cause);
	}

}