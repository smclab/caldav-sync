package it.smc.calendar.caldav.sync.application.list;

import it.smc.calendar.caldav.sync.constants.CaldavSyncPortletKeys;

import com.liferay.application.list.BasePanelApp;
import com.liferay.application.list.PanelApp;
import com.liferay.application.list.constants.PanelCategoryKeys;
import com.liferay.portal.kernel.model.Portlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * @author bta
 */
@Component(
	immediate = true,
	property = {
		"panel.app.order:Integer=600",
		"panel.category.key=" + PanelCategoryKeys.USER_MY_ACCOUNT
	},
	service = PanelApp.class
)
public class CaldavSyncPanelApp extends BasePanelApp {

	@Override
	public String getPortletId() {
		return CaldavSyncPortletKeys.CaldavSync;
	}

	@Override
	@Reference(
		target = "(javax.portlet.name=" + CaldavSyncPortletKeys.CaldavSync + ")",
		unbind = "-"
	)
	public void setPortlet(Portlet portlet) {
		super.setPortlet(portlet);
	}

}