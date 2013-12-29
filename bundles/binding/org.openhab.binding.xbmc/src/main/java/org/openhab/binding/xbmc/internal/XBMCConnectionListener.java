package org.openhab.binding.xbmc.internal;

import java.util.List;

import org.openhab.binding.xbmc.XBMCBindingCommands;
import org.openhab.binding.xbmc.XBMCBindingProvider;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.Application.GetProperties;
import org.xbmc.android.jsonrpc.api.model.ApplicationModel.PropertyValue;
import org.xbmc.android.jsonrpc.api.model.ApplicationModel.PropertyValue.Version;
import org.xbmc.android.jsonrpc.io.ApiCallback;
import org.xbmc.android.jsonrpc.io.ConnectionListener;
import org.xbmc.android.jsonrpc.io.JavaConnectionManager;
import org.xbmc.android.jsonrpc.notification.AbstractEvent;

public class XBMCConnectionListener implements ConnectionListener {

	private Logger logger = LoggerFactory.getLogger(XBMCConnectionListener.class);

	private EventPublisher eventPublisher;
	private XBMCBindingProvider bindingProvider;

	private String deviceId;

	private JavaConnectionManager conManager;

	private Integer currentVolume;
	private Boolean isMute;
	private String name;
	private Version version;

	public XBMCConnectionListener(String deviceId, EventPublisher publisher, XBMCBindingProvider provider,
			JavaConnectionManager conManager) {
		this.deviceId = deviceId;
		this.eventPublisher = publisher;
		this.bindingProvider = provider;
		this.conManager = conManager;
	}

	public JavaConnectionManager getConnectionManager() {
		return conManager;
	}

	public void updateProperties() {
		GetProperties call = new GetProperties("volume", "muted", "name", "version");
		conManager.call(call, new ApiCallback<PropertyValue>() {

			@Override
			public void onError(int arg0, String arg1, String arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onResponse(AbstractCall<PropertyValue> result) {
				currentVolume = result.getResult().volume;
				isMute = result.getResult().muted;
				name = result.getResult().name;
				version = result.getResult().version;

			}
		});
	}

	@Override
	public void connected() {
		logger.debug(deviceId + ": Connected");
	}

	@Override
	public void disconnected(Exception error) {
		logger.debug(deviceId + ": Disconnected, Trying to reconnect");
		if (error != null) {
			logger.debug(deviceId + ": Disconnected due to to Exception", error);
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			logger.debug("Exception while waiting for reconnect", e);
		}
		conManager.reconnect();
	}

	@Override
	public void notificationReceived(AbstractEvent event) {
		String methodName = event.getMethod();
		logger.debug("Received event: " + methodName);
		List<XBMCBindingConfig> configs = bindingProvider.findBindingConfigs(deviceId, methodName);
		for (XBMCBindingConfig config : configs) {
			State state = config.getStateForEvent(methodName);
			if (state != null) {
				if (eventPublisher != null) {
					logger.debug(deviceId + ": Posting update for item " + config.getItem().getName() + ": "
							+ state.format("%s"));
					eventPublisher.postUpdate(config.getItem().getName(), state);
				} else {
					logger.error("EventPublisher was NULL during creation of this listener...");
				}
			}
		}
	}

}
