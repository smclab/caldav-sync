package it.smc.calendar.caldav.helper.api;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Mirko Zizzari
 */
public class UserAgentHelperUtil {

	public static boolean isAndroid(String userAgent) {
		return getService().isAndroid(userAgent);
	}

	public static boolean isAndroidCalDAVSyncAdapter(String userAgent) {
		return getService().isAndroidCalDAVSyncAdapter(userAgent);
	}

	public static boolean isICal(String userAgent) {
		return getService().isICal(userAgent);
	}

	public static boolean isIOS(String userAgent) {
		return getService().isIOS(userAgent);
	}

	public static boolean isMacOSX(String userAgent) {
		return getService().isMacOSX(userAgent);
	}

	public static boolean isOpenSync(String userAgent) {
		return getService().isOpenSync(userAgent);
	}

	public static boolean isThunderbird(String userAgent) {
		return getService().isThunderbird(userAgent);
	}

	public static UserAgentHelper getService() {
		if (_serviceTracker == null) {
			Bundle bundle = FrameworkUtil.getBundle(CalendarHelper.class);

			_serviceTracker =
				new ServiceTracker<UserAgentHelper, UserAgentHelper>(
					bundle.getBundleContext(), UserAgentHelper.class, null);

			_serviceTracker.open();
		}

		return _serviceTracker.getService();
	}

	private static ServiceTracker<UserAgentHelper, UserAgentHelper>
		_serviceTracker;
}
