/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.arduinojunkers.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openhab.binding.arduinojunkers.ArduinoJunkersBindingProvider;
import org.openhab.binding.arduinojunkers.internal.ArduinoJunkersBindingConfig.ConnectionType;
import org.openhab.binding.arduinojunkers.internal.ArduinoJunkersBindingConfig.ItemType;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Till Klocke
 * @since 1.7.0
 */
public class ArduinoJunkersGenericBindingProvider extends
		AbstractGenericBindingProvider implements ArduinoJunkersBindingProvider {

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "arduinojunkers";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		if (!(item instanceof DimmerItem)) {
			throw new BindingConfigParseException(
					"item '"
							+ item.getName()
							+ "' is of type '"
							+ item.getClass().getSimpleName()
							+ "', only DimmerItems are allowed - please check your *.items configuration");
		}
	}

	/**
	 * {@inheritDoc} Config in style of arduinojunkers="serial://[port][#temp]"
	 */
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		ArduinoJunkersBindingConfig config = new ArduinoJunkersBindingConfig();
		if (bindingConfig.startsWith("serial")) {
			config.connectionType = ConnectionType.SERIAL;
			String serialPort = null;
			ItemType type = ItemType.SET_PERCENT;
			if (bindingConfig.indexOf('#') > 0) {
				serialPort = bindingConfig.substring(9,
						bindingConfig.indexOf('#'));
				type = ItemType.TEMP;
			} else {
				serialPort = bindingConfig.substring(9);
			}
			config.serialPort = serialPort;
			config.itemType = type;
		}

		addBindingConfig(item, config);
	}

	@Override
	public ArduinoJunkersBindingConfig getConfigForItem(String itemName) {
		return (ArduinoJunkersBindingConfig) bindingConfigs.get(itemName);
	}

	@Override
	public ArduinoJunkersBindingConfig getTempItemForDevice(
			ArduinoJunkersBindingConfig config) {
		for (BindingConfig bindingConfig : bindingConfigs.values()) {
			ArduinoJunkersBindingConfig tempConfig = (ArduinoJunkersBindingConfig) bindingConfig;
			if (tempConfig.itemType == ItemType.TEMP
					&& tempConfig.connectionType == config.connectionType) {
				if (tempConfig.connectionType == ConnectionType.SERIAL
						&& tempConfig.serialPort.equals(config.serialPort)) {
					return tempConfig;
				}
			}
		}
		return null;
	}

	@Override
	public List<ArduinoJunkersBindingConfig> getAllConfigs() {
		List<ArduinoJunkersBindingConfig> configs = new ArrayList<ArduinoJunkersBindingConfig>(
				bindingConfigs.size());
		for (BindingConfig bindingConfig : bindingConfigs.values()) {
			configs.add((ArduinoJunkersBindingConfig) bindingConfig);
		}
		return Collections.unmodifiableList(configs);
	}

	@Override
	public List<ArduinoJunkersBindingConfig> getAllTempConfigs() {
		List<ArduinoJunkersBindingConfig> configs = new ArrayList<ArduinoJunkersBindingConfig>(
				bindingConfigs.size());
		for (BindingConfig bindingConfig : bindingConfigs.values()) {
			ArduinoJunkersBindingConfig config = (ArduinoJunkersBindingConfig) bindingConfig;
			if (config.itemType == ItemType.TEMP) {
				configs.add((ArduinoJunkersBindingConfig) bindingConfig);
			}
		}
		return Collections.unmodifiableList(configs);
	}

}
