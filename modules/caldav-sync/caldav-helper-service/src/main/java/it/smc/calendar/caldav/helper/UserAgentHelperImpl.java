package it.smc.calendar.caldav.helper;

import it.smc.calendar.caldav.helper.api.UserAgentHelper;
import org.osgi.service.component.annotations.Component;

/**
 * @author Mirko Zizzari
 */
@Component(
	immediate = true,
	service = UserAgentHelper.class
)
public class UserAgentHelperImpl implements UserAgentHelper {

	@Override
	public boolean isAndroid(String userAgent) {
		return userAgent.contains("Android");
	}

	@Override
	public boolean isAndroidCalDAVSyncAdapter(String userAgent) {
		return userAgent.contains("CalDAV Sync Adapter");
	}

	@Override
	public boolean isICal(String userAgent) {
		return userAgent.contains("iCal");
	}

	@Override
	public boolean isIOS(String userAgent) {
		return userAgent.contains("iOS");
	}

	@Override
	public boolean isMacOSX(String userAgent) {
		return userAgent.contains("OS+X") || userAgent.contains("Mac+OS") ||
			   userAgent.contains("OS X") || userAgent.contains("Core") ||
			   userAgent.contains("OS_X") || userAgent.contains("macOS");
	}

	@Override
	public boolean isOpenSync(String userAgent) {
		return userAgent.contains("Thunderbird") ||
			   userAgent.contains("Lightning");
	}

	@Override
	public boolean isThunderbird(String userAgent) {
		return false;
	}

}
