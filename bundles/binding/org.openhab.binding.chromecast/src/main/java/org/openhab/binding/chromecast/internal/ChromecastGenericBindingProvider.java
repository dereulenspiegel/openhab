/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chromecast.internal;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.chromecast.ChromecastBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Till Klocke
 * @since 1.6.0
 */
public class ChromecastGenericBindingProvider extends
		AbstractGenericBindingProvider implements ChromecastBindingProvider {

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "chromecast";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		if (!(item instanceof SwitchItem || item instanceof NumberItem || item instanceof StringItem)) {
			throw new BindingConfigParseException(
					"item '"
							+ item.getName()
							+ "' is of type '"
							+ item.getClass().getSimpleName()
							+ "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Example config {chromecast="name=wohnzimmer:action=PLAY"}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		ChromecastBindingConfig config = new ChromecastBindingConfig();

		if (bindingConfig.indexOf(':') < 0 || bindingConfig.indexOf('=') < 0) {
			throw new BindingConfigParseException(
					"Binding configuration is not properly formatted");
		}
		String[] tupels = bindingConfig.split(":");
		for (String pair : tupels) {
			String[] parts = pair.split("=");
			if ("name".equals(parts[0])) {
				config.deviceName = parts[1];
			} else if ("action".equals(parts[0])) {
				config.property = ChromeCastProperties.valueOf(parts[1]);
			}
		}

		config.item = item;

		addBindingConfig(item, config);
	}

	public static class ChromecastBindingConfig implements BindingConfig {
		public String deviceName;
		public ChromeCastProperties property;
		public Item item;
	}

	@Override
	public ChromecastBindingConfig getBindingConfigFor(String deviceName,
			ChromeCastProperties property) {

		for (BindingConfig config : bindingConfigs.values()) {
			ChromecastBindingConfig chromecastConfig = (ChromecastBindingConfig) config;
			if (deviceName.equals(chromecastConfig.deviceName)
					&& chromecastConfig.property == property) {
				return chromecastConfig;
			}
		}
		return null;
	}

	@Override
	public ChromecastBindingConfig getBindingConfigFor(Item item) {
		return getBindingConfigFor(item.getName());
	}

	@Override
	public List<ChromecastBindingConfig> getBindingConfigsFor(String deviceName) {
		List<ChromecastBindingConfig> result = new ArrayList<ChromecastBindingConfig>();
		for (BindingConfig config : bindingConfigs.values()) {
			if (((ChromecastBindingConfig) config).deviceName
					.equals(deviceName)) {
				result.add((ChromecastBindingConfig) config);
			}
		}
		return result;
	}

	@Override
	public ChromecastBindingConfig getBindingConfigFor(String itemName) {
		return (ChromecastBindingConfig) this.bindingConfigs.get(itemName);
	}

}
