package it.smc.calendar.caldav.helper.api;

/**
 * @author Mirko Zizzari
 */
public interface UserAgentHelper {

	boolean isAndroid(String userAgent);

	boolean isAndroidCalDAVSyncAdapter(String userAgent);

	boolean isICal(String userAgent);

	boolean isIOS(String userAgent);

	boolean isMacOSX(String userAgent);

	boolean isOpenSync(String userAgent);

	boolean isThunderbird(String userAgent);
}
