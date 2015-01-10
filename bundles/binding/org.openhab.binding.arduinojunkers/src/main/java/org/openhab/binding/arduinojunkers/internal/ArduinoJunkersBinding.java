/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.arduinojunkers.internal;

import java.util.Dictionary;
import java.util.List;

import org.openhab.binding.arduinojunkers.ArduinoJunkersBindingProvider;
import org.openhab.binding.arduinojunkers.internal.ArduinoJunkersBindingConfig.ConnectionType;
import org.openhab.binding.arduinojunkers.internal.ConnectionBackend.TempListener;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement this class if you are going create an actively polling service like
 * querying a Website/Device.
 * 
 * @author Till Klocke
 * @since 1.7.0
 */
public class ArduinoJunkersBinding extends
		AbstractActiveBinding<ArduinoJunkersBindingProvider> implements
		ManagedService {

	private static final Logger logger = LoggerFactory
			.getLogger(ArduinoJunkersBinding.class);

	/**
	 * the refresh interval which is used to poll values from the ArduinoJunkers
	 * server (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;

	private SerialConnectionBackend serialConnectionBackend;

	public ArduinoJunkersBinding() {
	}

	public void activate() {
		logger.debug("Activating Arduino Junkers binding");
		serialConnectionBackend = new SerialConnectionBackend();
	}

	public void deactivate() {
		logger.debug("Deactivating Arduino Junkers binding");
		serialConnectionBackend.shutdown();
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
		return "ArduinoJunkers Refresh Service";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void execute() {
		List<ArduinoJunkersBindingConfig> tempConfigs = providers.iterator()
				.next().getAllTempConfigs();
		for (final ArduinoJunkersBindingConfig config : tempConfigs) {
			ConnectionBackend backend = getBackend(config);
			if (backend != null) {
				backend.requestTemperature(config, new TempListener() {
					@Override
					public void tempReceived(float temp) {
						DecimalType update = new DecimalType(temp);
						eventPublisher.postUpdate(config.itemName, update);

					}
				});
			} else {
				logger.error("Couldn't find backend for item {}",
						config.itemName);
			}
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		ArduinoJunkersBindingConfig config = null;
		for (ArduinoJunkersBindingProvider provider : providers) {
			config = provider.getConfigForItem(itemName);
			if (config != null) {
				break;
			}
		}

		if (config == null) {
			logger.error("Didn't found config for item {}", itemName);
			return;
		}
		ConnectionBackend backend = getBackend(config);
		if (backend != null) {
			PercentType type = (PercentType) command;
			logger.debug("Setting value {} for item {}", type.intValue(),
					itemName);
			backend.setValue(config, type.intValue());
		} else {
			logger.error("Couldn't find matching backend for item {}", itemName);
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
		logger.debug("internalReceiveCommand() is called! Ignoring");
	}

	private ConnectionBackend getBackend(ArduinoJunkersBindingConfig config) {
		if (config.connectionType == ConnectionType.SERIAL) {
			return serialConnectionBackend;
		}
		return null;
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void updated(Dictionary<String, ?> config)
			throws ConfigurationException {
		if (config != null) {

			// to override the default refresh interval one has to add a
			// parameter to openhab.cfg like
			// <bindingName>:refresh=<intervalInMs>
			String refreshIntervalString = (String) config.get("refresh");
			if (StringUtils.isNotBlank(refreshIntervalString)) {
				refreshInterval = Long.parseLong(refreshIntervalString);
			}

			// read further config parameters here ...

			setProperlyConfigured(true);
		}
	}

}
