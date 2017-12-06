<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %><%@
taglib uri="http://liferay.com/tld/security" prefix="liferay-security" %>

<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui" %><%@
taglib uri="http://liferay.com/tld/portlet" prefix="liferay-portlet" %><%@
taglib uri="http://liferay.com/tld/theme" prefix="liferay-theme" %><%@
taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>

<%@page import="com.liferay.calendar.constants.CalendarActionKeys"%>
<%@page import="com.liferay.calendar.service.CalendarLocalServiceUtil"%>
<%@page import="com.liferay.portal.kernel.dao.orm.QueryUtil"%>
<%@page import="com.liferay.portal.kernel.theme.ThemeDisplay"%>
<%@page import="com.liferay.portal.kernel.util.ThemeFactoryUtil"%>
<%@page import="com.liferay.portal.kernel.security.permission.PermissionChecker"%>
<%@page import="com.liferay.portal.kernel.model.User"%>
<%@page import="com.liferay.portal.kernel.language.LanguageUtil"%>
<%@page import="com.liferay.calendar.service.permission.CalendarPermission"%>
<%@page import="com.liferay.portal.kernel.util.StringPool"%>
<%@page import="com.liferay.calendar.model.CalendarResource"%>
<%@page import="it.smc.calendar.caldav.sync.util.CalDAVUtil"%>
<%@page import="com.liferay.calendar.model.Calendar"%>
<%@page import="com.liferay.portal.kernel.util.PortalUtil"%>
<%@page import="com.liferay.portal.kernel.util.Validator"%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.List"%>

<%@page import="it.smc.calendar.caldav.util.CalendarUtil"%>
<%@page import="it.smc.calendar.caldav.util.PropsValues"%>

<liferay-theme:defineObjects />

<portlet:defineObjects />