/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2010-2013, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */
package org.openhab.binding.xbmc.internal;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.xbmc.XBMCBindingProvider;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.config.HostConfig;
import org.xbmc.android.jsonrpc.io.ApiCallback;
import org.xbmc.android.jsonrpc.io.JavaConnectionManager;

/**
 * @author Till Klocke
 * @since 1.4.0
 */
public class XBMCBinding extends AbstractActiveBinding<XBMCBindingProvider> implements ManagedService {

	private static final Logger logger = LoggerFactory.getLogger(XBMCBinding.class);

	private Map<String, JavaConnectionManager> clientMap = new HashMap<String, JavaConnectionManager>();

	private static final Pattern EXTRACT_CONFIG_PATTERN = Pattern.compile("^(.*?)\\.(host|port)$");

	/**
	 * the refresh interval which is used to poll values from the XBMC server
	 * (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;

	public XBMCBinding() {
	}

	public void activate() {
	}

	public void deactivate() {
		for (JavaConnectionManager client : clientMap.values()) {
			client.disconnect();
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected String getName() {
		return "XBMC Refresh Service";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void execute() {
		// the frequently executed code (polling) goes here ...
		logger.debug("execute() method is called!");
	}

	/**
	 * @{inheritDoc
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		// the code being executed when a command was sent on the openHAB
		// event bus goes here. This method is only called if one of the
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveCommand() is called!");
		XBMCBindingConfig config = findConfigByItemName(itemName);
		if (config != null) {
			String methodName = config.getMethodNameForCommand(command);
			AbstractCall call = CallAndEventParser.getCallFromString(methodName);
			JavaConnectionManager conManager = clientMap.get(config.getDeviceId());
			if (conManager != null && call != null) {
				conManager.call(call, new ApiCallback() {

					@Override
					public void onError(int arg0, String arg1, String arg2) {
						// TODO Auto-generated method stub

					}

					@Override
					public void onResponse(AbstractCall arg0) {
						// TODO Auto-generated method stub

					}
				});
			} else {
				logger.error("Either ApiCall for command could not be found or device connection can't be found");
			}
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void internalReceiveUpdate(String itemName, State newState) {
		// the code being executed when a state was sent on the openHAB
		// event bus goes here. This method is only called if one of the
		// BindingProviders provide a binding for the given 'itemName'.
		logger.debug("internalReceiveCommand() is called!");
	}

	private XBMCBindingConfig findConfigByItemName(String itemName) {
		for (XBMCBindingProvider provider : providers) {
			XBMCBindingConfig config = provider.findBindingConfigByItemName(itemName);
			if (config != null) {
				return config;
			}
		}
		return null;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void updated(Dictionary<String, ?> config) throws ConfigurationException {
		if (config != null) {

			// to override the default refresh interval one has to add a
			// parameter to openhab.cfg like
			// <bindingName>:refresh=<intervalInMs>
			String refreshIntervalString = (String) config.get("refresh");
			if (StringUtils.isNotBlank(refreshIntervalString)) {
				refreshInterval = Long.parseLong(refreshIntervalString);
			}
			Enumeration<String> keys = config.keys();
			Map<String, HostConfig> hostConfigs = new HashMap<String, HostConfig>();

			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();

				// the config-key enumeration contains additional keys that we
				// don't want to process here ...
				if ("service.pid".equals(key)) {
					continue;
				}

				Matcher matcher = EXTRACT_CONFIG_PATTERN.matcher(key);

				if (!matcher.matches()) {
					logger.debug("given config key '" + key
							+ "' does not follow the expected pattern '<id>.<host|port>'");
					continue;
				}

				matcher.reset();
				matcher.find();

				String deviceId = matcher.group(1);

				HostConfig hostConfig = hostConfigs.get(deviceId);
				if (hostConfig == null) {
					hostConfig = new HostConfig(null);
					hostConfigs.put(deviceId, hostConfig);
				}

				String configKey = matcher.group(2);
				String value = (String) config.get(key);

				if ("host".equals(configKey)) {
					hostConfig.mAddress = value;
				} else if ("port".equals(configKey)) {
					int port = Integer.valueOf(value);
					hostConfig.mTcpPort = port;
				} else {
					throw new ConfigurationException(configKey, "the given configKey '" + configKey + "' is unknown");
				}
			}
			List<String> failedClientKeys = new ArrayList<String>();
			for (Entry<String, HostConfig> clientEntry : hostConfigs.entrySet()) {
				try {
					JavaConnectionManager conManager = new JavaConnectionManager();
					// FIXME probably give the listener a list of
					// BindingProvider instead of just the first one
					conManager.registerConnectionListener(new XBMCConnectionListener(clientEntry.getKey(),
							eventPublisher, providers.iterator().next()));
					conManager.connect(clientEntry.getValue());
					clientMap.put(clientEntry.getKey(), conManager);
				} catch (Exception e) {
					failedClientKeys.add(clientEntry.getKey());
					logger.error("Can't connect with XBMC instance " + clientEntry.getKey(), e);
				}
			}
			setProperlyConfigured(true);
		}
	}
}
