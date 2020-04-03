package it.smc.calendar.caldav.sync.menu;

import com.liferay.product.navigation.personal.menu.BasePersonalMenuEntry;
import com.liferay.product.navigation.personal.menu.PersonalMenuEntry;
import it.smc.calendar.caldav.sync.constants.CaldavSyncPortletKeys;
import org.osgi.service.component.annotations.Component;

@Component(
        immediate = true,
        property = {
                "product.navigation.personal.menu.entry.order:Integer=600",
                "product.navigation.personal.menu.group:Integer=300"
        },
        service = PersonalMenuEntry.class
)
public class CaldavSyncPersonalMenuEntry extends BasePersonalMenuEntry {
    @Override
    protected String getPortletId() {
        return CaldavSyncPortletKeys.CaldavSync;
    }
}
