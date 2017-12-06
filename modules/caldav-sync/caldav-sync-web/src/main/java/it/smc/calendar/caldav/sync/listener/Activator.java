package it.smc.calendar.caldav.sync.listener;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import com.liferay.portal.kernel.util.BasePortalLifecycle;
import com.liferay.portal.kernel.webdav.WebDAVStorage;
import com.liferay.portal.kernel.webdav.WebDAVUtil;
import com.liferay.portal.kernel.webdav.methods.MethodFactory;
import com.liferay.portal.kernel.webdav.methods.MethodFactoryRegistryUtil;

import it.smc.calendar.caldav.sync.CalDAVMethodFactory;
import it.smc.calendar.caldav.sync.LiferayCalDAVStorageImpl;
import it.smc.calendar.caldav.sync.util.WebKeys;

/**
 * @author bta
 */
public class Activator extends BasePortalLifecycle 
						implements BundleActivator,BundleListener{
		
		@Override
	public void start(BundleContext context) throws Exception {
		context.addBundleListener(this);
	}
		
		@Override
	public void stop(BundleContext context) throws Exception {
		portalDestroy();
		context.removeBundleListener(this);
		
	}
		@Override
	public void bundleChanged(BundleEvent event) {
		registerPortalLifecycle();
	}
		
		@Override
	protected void doPortalDestroy() throws Exception {
		if (_storage != null) {
			WebDAVUtil.deleteStorage(_storage);
		}
		
		if (_methodFactory != null) {
					MethodFactoryRegistryUtil
					.unregisterMethodFactory(_methodFactory);
				}
			}
		
		@Override
	protected void doPortalInit() throws Exception {
			
		_storage = new LiferayCalDAVStorageImpl();
		_storage.setToken(WebKeys.CALDAV_TOKEN);
		
		WebDAVUtil.addStorage(_storage);
		
		_methodFactory = new CalDAVMethodFactory();
				  
		 MethodFactoryRegistryUtil.registerMethodFactory(_methodFactory);
	}
		
		private MethodFactory _methodFactory;
		private WebDAVStorage _storage;
				
}
