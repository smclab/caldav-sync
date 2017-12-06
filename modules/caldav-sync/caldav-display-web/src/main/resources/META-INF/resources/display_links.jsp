<%@ include file="init.jsp" %>

<%

String portalURL = PortalUtil.getPortalURL(request);

List<Calendar> calendars = 
	CalendarUtil.getAllCalendars(permissionChecker);

String userCalDAVURL = portalURL + CalDAVUtil.getPrincipalURL(themeDisplay.getUserId());

if(!calendars.isEmpty()) { 
%>

<div class="container-fluid-1280 ">
	<h2>
		<liferay-ui:message key="caldav-url-for-collection" />
		<liferay-ui:icon-help message="caldav-url-for-collection-help" />
	</h2>
	<aui:fieldset>
		<p class="calendar-url-entry">
			<span class="entry-title"><%= user.getFullName() %></span>
			<span class="entry-url"><%= userCalDAVURL %></span>
		</p>
	</aui:fieldset>
</div>

<div class="container-fluid-1280 ">
	<h2>
		<liferay-ui:message key="caldav-url-without-collection" />
		<liferay-ui:icon-help message="caldav-url-without-collection-help" />
	</h2>
	<aui:fieldset>

		<%
		String calDAVURL;
		StringBuilder calendarTitleSb;
		CalendarResource calendarResource;
		for (Calendar calendar : calendars) {
			calDAVURL = portalURL + CalDAVUtil.getCalendarURL(calendar);

			calendarResource = calendar.getCalendarResource();

			calendarTitleSb = new StringBuilder();
			calendarTitleSb.append(calendarResource.getName(locale));
			calendarTitleSb.append(StringPool.SPACE);
			calendarTitleSb.append(StringPool.MINUS);
			calendarTitleSb.append(StringPool.SPACE);
			calendarTitleSb.append(calendar.getName(locale));

			if (!CalendarPermission.contains(permissionChecker, calendar, CalendarActionKeys.MANAGE_BOOKINGS)) {
				calendarTitleSb.append(StringPool.SPACE);
				calendarTitleSb.append(StringPool.OPEN_PARENTHESIS);
				calendarTitleSb.append(LanguageUtil.get(locale, "read-only"));
				calendarTitleSb.append(StringPool.CLOSE_PARENTHESIS);
			}
			
			String color = StringPool.POUND.concat(String.format("%06X", (0xFFFFFF & calendar.getColor())));
			%>

			<p class="calendar-url-entry">
				<span class="calendar-list-item-color" style="background-color: <%= color %>; border-color: <%= color %>"></span>
				<span class="entry-title"><%= calendarTitleSb.toString() %></span>
				<span class="entry-url"><%= calDAVURL %></span>
			</p>

			<%
		}
		%>
	</aui:fieldset>
</div>

<% } %>
