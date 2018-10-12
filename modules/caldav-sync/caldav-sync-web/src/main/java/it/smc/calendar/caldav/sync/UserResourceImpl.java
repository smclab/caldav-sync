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

import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.webdav.BaseResourceImpl;

import java.util.Locale;

/**
 * @author Fabio Pezzutto
 */
public class UserResourceImpl extends BaseResourceImpl {

	public UserResourceImpl(User user, String parentPath, Locale locale) {
		super(
			parentPath, StringPool.BLANK, user.getFullName(),
			user.getCreateDate(), user.getModifiedDate());

		setModel(user);
		setClassName(User.class.getName());
		setPrimaryKey(user.getPrimaryKey());
	}

	@Override
	public boolean isCollection() {
		return false;
	}

}