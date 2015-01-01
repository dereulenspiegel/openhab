/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.arduinojunkers.internal;

import org.openhab.binding.arduinojunkers.ArduinoJunkersBindingProvider;
import org.openhab.binding.arduinojunkers.internal.ArduinoJunkersBindingConfig.ConnectionType;
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
public class ArduinoJunkersGenericBindingProvider extends AbstractGenericBindingProvider implements ArduinoJunkersBindingProvider {

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "arduinojunkers";
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		if (!(item instanceof DimmerItem)) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only DimmerItems are allowed - please check your *.items configuration");
		}
	}
	
	/**
	 * {@inheritDoc}
	 * Config in style of arduinojunkers="serial://"
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		ArduinoJunkersBindingConfig config = new ArduinoJunkersBindingConfig();
		if(bindingConfig.startsWith("serial")){
			config.connectionType = ConnectionType.SERIAL;
			String serialPort = bindingConfig.substring(9);
			config.serialPort = serialPort;
		}
				
		addBindingConfig(item, config);		
	}

	@Override
	public ArduinoJunkersBindingConfig getConfigForItem(String itemName) {
		return (ArduinoJunkersBindingConfig) bindingConfigs.get(itemName);
	}
	
	
}
