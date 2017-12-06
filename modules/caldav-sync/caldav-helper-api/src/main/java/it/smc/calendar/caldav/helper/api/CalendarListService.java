package it.smc.calendar.caldav.helper.api;

import java.util.List;

import com.liferay.calendar.model.Calendar;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.security.permission.PermissionChecker;

/**
 * @author rge
 */
public interface CalendarListService {

	public List<Calendar> getAllCalendars(
			PermissionChecker permissionChecker)
		throws PortalException, SystemException;
}