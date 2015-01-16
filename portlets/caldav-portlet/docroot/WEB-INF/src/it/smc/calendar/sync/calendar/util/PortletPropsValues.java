package it.smc.calendar.sync.calendar.util;

import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.util.portlet.PortletProps;

public class PortletPropsValues {

	public static final boolean PROPFIND_PROVIDE_SESSIONCLICKS_CALENDARS =
		GetterUtil.getBoolean(PortletProps.get(
			PortletPropsKeys.PROPFIND_PROVIDE_SESSIONCLICKS_CALENDARS));

}
