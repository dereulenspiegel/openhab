/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fs20.internal;

import java.util.Dictionary;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.fs20.FS20BindingConfig;
import org.openhab.binding.fs20.FS20BindingProvider;
import org.openhab.core.types.Command;
import org.openhab.io.transport.cul.AbstractCULBinding;
import org.openhab.io.transport.cul.CULCommunicationException;
import org.openhab.io.transport.cul.CULMode;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the communcation between openHAB and FS20 devices. Via
 * RF received updates are received directly, there is no polling.
 * 
 * @author Till Klocke
 * @since 1.4.0
 */
public class FS20Binding extends AbstractCULBinding<FS20BindingProvider> {

	private static final Logger logger = LoggerFactory.getLogger(FS20Binding.class);

	/**
	 * the refresh interval which is used to poll values from the FS20 server
	 * (optional, defaults to 60000ms)
	 */
	private long refreshInterval = 60000;

	public FS20Binding() {
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
		return "FS20 Refresh Service";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void execute() {
		// Nothing to do here
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected void internalReceiveCommand(String itemName, Command command) {
		FS20BindingConfig bindingConfig = null;
		for (FS20BindingProvider provider : super.providers) {
			bindingConfig = provider.getConfigForItemName(itemName);
			if (bindingConfig != null) {
				break;
			}
		}
		if (bindingConfig != null) {
			logger.debug("Received command " + command.toString() + " for item " + itemName);
			try {
				FS20Command fs20Command = FS20CommandHelper.convertHABCommandToFS20Command(command);
				if (fs20Command == null) {
					logger.error("Couldn't convert openHAB command " + command.format("%s") + " to FS20 command");
					return;
				}
				cul.send("F" + bindingConfig.getAddress() + fs20Command.getHexValue());
			} catch (CULCommunicationException e) {
				logger.error("An exception occured while sending a command", e);
			}
		}
	}

	private void handleReceivedMessage(String message) {
		String houseCode = (message.substring(1, 5));
		String address = (message.substring(5, 7));
		String command = message.substring(7, 9);
		String fullAddress = houseCode + address;
		FS20BindingConfig config = null;
		for (FS20BindingProvider provider : providers) {
			config = provider.getConfigForAddress(fullAddress);
			if (config != null) {
				break;
			}
		}
		if (config != null) {
			FS20Command fs20Command = FS20Command.getFromHexValue(command);
			logger.debug("Received command " + fs20Command.toString() + " for device " + config.getAddress());
			eventPublisher.postUpdate(config.getItem().getName(),
					FS20CommandHelper.getStateFromFS20Command(fs20Command));
		} else {
			logger.debug("Received message for unknown device " + fullAddress);
		}
	}

	@Override
	public void error(Exception e) {
		logger.error("Error while communicating with CUL", e);

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
		// Ignore, wo don't need to do anything

	}

	@Override
	protected void parseMessage(String data) {
		if (data.startsWith("F")) {
			logger.debug("Received FS20 message: " + data);
			handleReceivedMessage(data);
		}
	}

	@Override
	protected void parseConfig(Dictionary<String, ?> config) throws ConfigurationException {
		// to override the default refresh interval one has to add a
		// parameter to openhab.cfg like
		// <bindingName>:refresh=<intervalInMs>
		String refreshIntervalString = (String) config.get("refresh");
		if (StringUtils.isNotBlank(refreshIntervalString)) {
			refreshInterval = Long.parseLong(refreshIntervalString);
		}
	}

}
