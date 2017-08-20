package it.smc.calendar.sync.application.list;

import com.liferay.application.list.BasePanelApp;
import com.liferay.application.list.PanelApp;
import com.liferay.application.list.constants.PanelCategoryKeys;
import com.liferay.portal.kernel.model.Portlet;

import it.smc.calendar.sync.util.PortletKeys;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author juangon
 */
@Component(immediate = true, property = { "panel.app.order:Integer=100",
		"panel.category.key=" + PanelCategoryKeys.USER_MY_ACCOUNT }, 
		service = PanelApp.class)
public class CaldavPanelApp extends BasePanelApp {

	@Override
	public String getPortletId() {
		return PortletKeys.CALDAV;
	}

	@Override
	@Reference(target = "(javax.portlet.name=" + PortletKeys.CALDAV + ")", unbind = "-")
	public void setPortlet(Portlet portlet) {
		super.setPortlet(portlet);
	}

}