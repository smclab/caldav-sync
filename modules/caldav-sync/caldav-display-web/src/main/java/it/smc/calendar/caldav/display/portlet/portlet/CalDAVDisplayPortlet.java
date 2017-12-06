package it.smc.calendar.caldav.display.portlet.portlet;

import it.smc.calendar.caldav.display.portlet.constants.CalDAVDisplayPortletKeys;

import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;

import javax.portlet.Portlet;

import org.osgi.service.component.annotations.Component;

/**
 * @author rge
 */
@Component(
	immediate = true,
	property = {
		"com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.header-portlet-css=/css/main.css",
		"com.liferay.portlet.display-category=category.collaboration",
		"com.liferay.portlet.instanceable=true",
		"javax.portlet.display-name=CalDAV URL Display",
		"javax.portlet.init-param.template-path=/",
		"javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + CalDAVDisplayPortletKeys.CalDAVDisplay,
		"javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user"
	},
	service = Portlet.class
)
public class CalDAVDisplayPortlet extends MVCPortlet {
}