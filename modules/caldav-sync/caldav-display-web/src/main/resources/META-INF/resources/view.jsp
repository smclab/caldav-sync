<%--
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
--%>

<%@ include file="/init.jsp" %>

<liferay-portlet:renderURL var="selectURL" windowState="<%= LiferayWindowState.POP_UP.toString() %>">
	<portlet:param name="mvcRenderCommandName" value="/smc/calDav/display_links" />
</liferay-portlet:renderURL>

<div>
	<aui:button-row>
		<aui:button cssClass="btn-lg" name="showLinks" type="submit" value="hide-show-urls" />
	</aui:button-row>
</div>

<aui:script use="aui-base">

var showlinks = A.one('#<portlet:namespace />showLinks');

showlinks.on(
	'click', function() {
		Liferay.Util.openWindow({
			dialog: {
				constrain: true,
				modal: true,
				width: 1000
			},
			title:'<liferay-ui:message key="hide-show-urls" />',
			uri: '<%= selectURL.toString() %>'
		});
	}
);
</aui:script>