/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.em.internal;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.em.EMBindingProvider;
import org.openhab.binding.em.internal.EMBindingConfig.Datapoint;
import org.openhab.binding.em.internal.EMBindingConfig.EMType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.io.transport.cul.AbstractCULBinding;
import org.openhab.io.transport.cul.CULMode;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement this class if you are going create an actively polling service like
 * querying a Website/Device.
 * 
 * @author Till Klocke
 * @since 1.4.0
 */
public class EMBinding extends AbstractCULBinding<EMBindingProvider> {

	private static final Logger logger = LoggerFactory.getLogger(EMBinding.class);

	private long refreshInterval = 60000;

	private Map<String, Integer> counterMap = new HashMap<String, Integer>();

	public EMBinding() {
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
		return "EM Refresh Service";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void execute() {
		// Nothing to do
	}

	/**
	 * Parse the received line of data and create updates for configured items
	 * 
	 * @param data
	 */
	private void parseDataLine(String data) {
		String address = ParsingUtils.parseAddress(data);
		if (!checkNewMessage(address, ParsingUtils.parseCounter(data))) {
			logger.warn("Received message from " + address + " more than once");
			return;
		}
		EMType type = ParsingUtils.parseType(data);
		EMBindingConfig emConfig = findConfig(type, address, Datapoint.CUMULATED_VALUE);
		if (emConfig != null) {
			updateItem(emConfig, ParsingUtils.parseCumulatedValue(data));
		}
		if (data.length() > 10) {
			emConfig = findConfig(type, address, Datapoint.LAST_VALUE);
			if (emConfig != null) {
				updateItem(emConfig, ParsingUtils.parseCurrentValue(data));
			}
			emConfig = findConfig(type, address, Datapoint.TOP_VALUE);
			if (emConfig != null) {
				updateItem(emConfig, ParsingUtils.parsePeakValue(data));
			}
		}
	}

	/**
	 * Update an item given in the configuration with the given value multiplied
	 * by the correction factor
	 * 
	 * @param config
	 * @param value
	 */
	private void updateItem(EMBindingConfig config, int value) {
		DecimalType status = new DecimalType(value * config.getCorrectionFactor());
		eventPublisher.postUpdate(config.getItem().getName(), status);
	}

	/**
	 * Check if we have received a new message to not consume repeated messages
	 * 
	 * @param address
	 * @param counter
	 * @return
	 */
	private boolean checkNewMessage(String address, int counter) {
		Integer lastCounter = counterMap.get(address);
		if (lastCounter == null) {
			lastCounter = -1;
		}
		if (counter > lastCounter) {
			return true;
		}
		return false;
	}

	private EMBindingConfig findConfig(EMType type, String address, Datapoint datapoint) {
		EMBindingConfig emConfig = null;
		for (EMBindingProvider provider : this.providers) {
			emConfig = provider.getConfigByTypeAndAddressAndDatapoint(type, address, Datapoint.CUMULATED_VALUE);
			if (emConfig != null) {
				return emConfig;
			}
		}
		return null;
	}

	@Override
	public void error(Exception e) {
		logger.error("Exception instead of new data from CUL", e);

	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected CULMode getCULMode() {
		return CULMode.SLOW_RF;
	}

	@Override
	protected void culOpen() {
		// Ignore, we don't need to do anything after the CUL device is open

	}

	@Override
	protected void parseMessage(String data) {
		if (!StringUtils.isEmpty(data) && data.startsWith("E")) {
			parseDataLine(data);
		}

	}

	@Override
	protected void parseConfig(Dictionary<String, ?> config) throws ConfigurationException {
		String refreshIntervalString = (String) config.get("refresh");
		if (StringUtils.isNotBlank(refreshIntervalString)) {
			refreshInterval = Long.parseLong(refreshIntervalString);
		}

	}

}
