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

<%
String portletId = PortalUtil.getPortletId(request);

PortletURL selectURL = renderResponse.createRenderURL();

selectURL.setParameter("mvcPath", "/display_links.jsp");
selectURL.setWindowState(LiferayWindowState.POP_UP);
%>

<div><aui:button-row>
		<aui:button cssClass="btn-lg" name="showLinks" type="submit" value="hide-show-urls" />
	</aui:button-row>
</div>

<aui:script use="aui-io-request,aui-base">
var showlinks = A.one('#<portlet:namespace />showLinks');
showlinks.on(
			'click',
			function() {
				Liferay.Util.openWindow(
				{
					dialog: {
						constrain: true,
						modal: true,
						width: 1000
					},
					title:'<liferay-ui:message key="hide-show-urls" />',
					uri: '<%= selectURL.toString() %>'
				}
			);
		}
	);
</aui:script>