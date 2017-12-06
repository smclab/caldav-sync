<%@ include file="init.jsp" %>

<%

String portletId = PortalUtil.getPortletId(request);

PortletURL selectURL = renderResponse.createRenderURL();

selectURL.setParameter("mvcPath", "/display_links.jsp");
selectURL.setWindowState(LiferayWindowState.POP_UP);

%>
<div><aui:button-row>
		<aui:button cssClass="btn-lg" type="submit" name="showLinks" value="hide-show-urls"/>
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