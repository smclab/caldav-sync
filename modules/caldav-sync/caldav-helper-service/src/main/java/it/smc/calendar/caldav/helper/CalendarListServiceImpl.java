package it.smc.calendar.caldav.helper;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import com.liferay.calendar.model.Calendar;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.CalendarLocalServiceUtil;
import com.liferay.calendar.service.CalendarResourceServiceUtil;
import com.liferay.calendar.service.CalendarServiceUtil;
import com.liferay.calendar.service.permission.CalendarPermission;
import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.User;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.security.permission.ActionKeys;
import com.liferay.portal.kernel.security.permission.PermissionChecker;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.SessionClicks;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import it.smc.calendar.caldav.helper.api.CalendarListService;
import it.smc.calendar.caldav.helper.util.PropsValues;

/**
 * @author rge
 */
@Component(
	immediate = true,
	service = CalendarListService.class
)
public class CalendarListServiceImpl implements CalendarListService {

	@Override
	public List<Calendar> getAllCalendars(
			PermissionChecker permissionChecker)
		throws PortalException, SystemException {
		
		boolean useSessionClicksCalendars =
			PropsValues.PROPFIND_PROVIDE_SESSIONCLICKS_CALENDARS;
		boolean hidePersonalCalendar = PropsValues.HIDE_PERSONAL_CALENDAR;

		List<Calendar> calendars = new ArrayList<Calendar>();

		if (useSessionClicksCalendars) {
			if (!hidePersonalCalendar) {
				List<Calendar> userCalendars = 
					getUserCalendars(permissionChecker.getUserId());
				if (Validator.isNotNull(userCalendars)) {
					calendars.addAll(userCalendars);
				}
			}
			
			List<Calendar> userGroupCalendars = 
				getUserGroupCalendars(permissionChecker);
			if(Validator.isNotNull(userGroupCalendars)){
				calendars.addAll(userGroupCalendars);
			}
			
			List<Calendar> selectedCalendars = 
				getSelectedCalendars(permissionChecker.getUserId());
			if(Validator.isNotNull(selectedCalendars)){
				calendars.addAll(selectedCalendars);
			}
		}
		else {
			List<Calendar> allCalendars = CalendarLocalServiceUtil.getCalendars(
				QueryUtil.ALL_POS, QueryUtil.ALL_POS);

			for (Calendar calendar : allCalendars) {
				if (CalendarPermission.contains(
						permissionChecker, calendar, ActionKeys.VIEW)) {

					calendars.add(calendar);
				}
			}
		}

		return calendars;
	}
	
	public static List<Calendar> getSelectedCalendars(long userId)
		throws PortalException, SystemException {

		ArrayList<Calendar> calendars = new ArrayList<Calendar>();

		String otherCalendarPreferences =
			"com.liferay.calendar.web_otherCalendars";

		long[] calendarIds = GetterUtil.getLongValues(
			StringUtil.split(
				PortletPreferencesFactoryUtil.getPortalPreferences(
					userId, true).getValue(
						SessionClicks.class.getName(),
						otherCalendarPreferences)));

		for (long calendarId : calendarIds) {
			calendars.add(CalendarServiceUtil.getCalendar(calendarId));
		}

		return calendars;
	}
	
	public List<Calendar> getUserCalendars(long userId) {

		long classNameId = PortalUtil.getClassNameId(User.class.getName());

		CalendarResource calendarResource = null;
		try {
			calendarResource =
				CalendarResourceServiceUtil.fetchCalendarResource(
					classNameId, userId);
			if (Validator.isNull(calendarResource)) {
				return null;
			}
		}
		catch (PortalException e) {
			// TODO Auto-generated catch block
		}
		return calendarResource.getCalendars();
	}

	public static List<Calendar> getUserGroupCalendars(
		PermissionChecker permissionChecker)
		throws PortalException, SystemException {

		List<Calendar> calendars = new ArrayList<Calendar>();

		long classNameId = PortalUtil.getClassNameId(Group.class.getName());

		List<CalendarResource> calendarResources =
			CalendarResourceServiceUtil.search(
				permissionChecker.getCompanyId(), new long[] {}, new long[] {
					classNameId
				}, null, true, true, QueryUtil.ALL_POS, QueryUtil.ALL_POS, null);

		for (CalendarResource calendarResource : calendarResources) {
			for (Calendar calendar : calendarResource.getCalendars()) {
				if (CalendarPermission.contains(
					permissionChecker, calendar, ActionKeys.VIEW)) {

					calendars.add(calendar);
				}
			}
		}

		return calendars;
	}

}