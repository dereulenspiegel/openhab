/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coap.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.coap.CoAPBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Till Klocke
 * @since 1.7.0
 */
public class CoAPGenericBindingProvider extends AbstractGenericBindingProvider
		implements CoAPBindingProvider {

	private final static Logger logger = LoggerFactory
			.getLogger(CoAPGenericBindingProvider.class);

	/** RegEx to extract a parse a function String <code>'(.*?)\((.*)\)'</code> */
	private static final Pattern EXTRACT_FUNCTION_PATTERN = Pattern
			.compile("(.*?)\\((.*)\\)");

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "coap";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		// if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
		// throw new BindingConfigParseException("item '" + item.getName()
		// + "' is of type '" + item.getClass().getSimpleName()
		// +
		// "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
		// }
	}

	/**
	 * {@inheritDoc} coap="transform: function.type"
	 */
	@Override
	public void processBindingConfiguration(String context, Item item,
			String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		logger.debug("Adding binding config for item {}: {}", item.getName(),
				bindingConfig);
		CoAPBindingConfig config = new CoAPBindingConfig();

		config.item = item;

		if (!StringUtils.isEmpty(bindingConfig)) {
			String[] parts = splitTransformationConfig(bindingConfig.split(":")[1]);

			config.transformationFunction = parts[0];
			config.transformationType = parts[1];
		}

		addBindingConfig(item, config);
	}

	protected String[] splitTransformationConfig(String transformation) {
		Matcher matcher = EXTRACT_FUNCTION_PATTERN.matcher(transformation);

		if (!matcher.matches()) {
			throw new IllegalArgumentException(
					"given transformation function '"
							+ transformation
							+ "' does not follow the expected pattern '<function>(<pattern>)'");
		}
		matcher.reset();

		matcher.find();
		String type = matcher.group(1);
		String pattern = matcher.group(2);

		return new String[] { type, pattern };
	}

	/**
	 * This is a helper class holding binding specific configuration details
	 * 
	 * @author Till Klocke
	 * @since 1.7.0
	 */
	public class CoAPBindingConfig implements BindingConfig {

		public String transformationType;
		public String transformationFunction;

		public Item item;
	}

	@Override
	public CoAPBindingConfig getBindingConfigForItem(String itemName) {
		if (this.bindingConfigs.containsKey(itemName)) {
			return (CoAPBindingConfig) this.bindingConfigs.get(itemName);
		}
		return null;
	}

}
