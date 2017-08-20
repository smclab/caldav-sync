package it.smc.calendar.sync.portlet;

import com.liferay.calendar.service.CalendarService;
import com.liferay.portal.kernel.portlet.bridges.mvc.MVCPortlet;
import com.liferay.portal.kernel.webdav.WebDAVStorage;
import com.liferay.portal.kernel.webdav.methods.MethodFactory;
import com.liferay.portal.kernel.webdav.methods.MethodFactoryRegistryUtil;

import it.smc.calendar.sync.internal.CalDAVMethodFactory;
import it.smc.calendar.sync.util.PortletKeys;

import java.util.Map;

import javax.portlet.Portlet;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

/**
 * @author juangon
 */
@Component(immediate = true, property = { "com.liferay.portlet.add-default-resource=true",
		"com.liferay.portlet.display-category=category.hidden", "com.liferay.portlet.layout-cacheable=true",
		"com.liferay.portlet.private-request-attributes=false", "com.liferay.portlet.private-session-attributes=false",
		"com.liferay.portlet.render-weight=50", "com.liferay.portlet.use-default-template=true",
		"javax.portlet.display-name=caldav Portlet", "javax.portlet.expiration-cache=0",
		"javax.portlet.init-param.template-path=/", "javax.portlet.init-param.view-template=/view.jsp",
		"javax.portlet.name=" + PortletKeys.CALDAV, "javax.portlet.resource-bundle=content.Language",
		"javax.portlet.security-role-ref=power-user,user",
		"javax.portlet.supports.mime-type=text/html" }, service = Portlet.class)
public class CaldavPortlet extends MVCPortlet {

	private CalendarService _calendarService;
	private MethodFactory _methodFactory;

	@Reference(unbind = "-")
	protected void setCalendarService(CalendarService calendarService) {
		_calendarService = calendarService;
	}

	@Activate
	@Modified
	protected void activate(Map<String, Object> properties) {
		_methodFactory = new CalDAVMethodFactory();

		MethodFactoryRegistryUtil.registerMethodFactory(_methodFactory);
	}

	@Deactivate
	@Modified
	protected void deactivate(Map<String, Object> properties) {
		MethodFactoryRegistryUtil.unregisterMethodFactory(_methodFactory);
	}
}