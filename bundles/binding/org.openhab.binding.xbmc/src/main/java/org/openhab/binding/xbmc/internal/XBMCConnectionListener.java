package org.openhab.binding.xbmc.internal;

import java.util.List;

import org.openhab.binding.xbmc.XBMCBindingProvider;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbmc.android.jsonrpc.io.ConnectionListener;
import org.xbmc.android.jsonrpc.notification.AbstractEvent;

public class XBMCConnectionListener implements ConnectionListener {

	private Logger logger = LoggerFactory.getLogger(XBMCConnectionListener.class);

	private EventPublisher eventPublisher;
	private XBMCBindingProvider bindingProvider;

	private String deviceId;

	public XBMCConnectionListener(String deviceId, EventPublisher publisher, XBMCBindingProvider provider) {
		this.deviceId = deviceId;
		this.eventPublisher = publisher;
		this.bindingProvider = provider;
	}

	@Override
	public void connected() {
		logger.debug(deviceId + ": Connected");
		// TODO Auto-generated method stub

	}

	@Override
	public void disconnected() {
		// TODO Auto-generated method stub
		logger.debug(deviceId + ": Disconnected");
	}

	@Override
	public void notificationReceived(AbstractEvent event) {
		String methodName = event.getMethod();
		List<XBMCBindingConfig> configs = bindingProvider.findBindingConfigs(deviceId, methodName);
		for (XBMCBindingConfig config : configs) {
			State state = config.getStateForEvent(methodName);
			if (state != null) {
				eventPublisher.postUpdate(config.getItem().getName(), state);
			}
		}
	}

}
