package it.smc.calendar.caldav.helper.util;

import com.liferay.portal.kernel.configuration.Configuration;
import com.liferay.portal.kernel.configuration.ConfigurationFactoryUtil;

/**
 * @author bta
 */
public class ConfigurationUtil {

	public static String get(String key) {
		return _configuration.get(key);
	}

	public static String[] getArray(String key) {
		return _configuration.getArray(key);
	}

	private static final Configuration _configuration =
		ConfigurationFactoryUtil.getConfiguration(
			ConfigurationUtil.class.getClassLoader(), "portlet");

}

