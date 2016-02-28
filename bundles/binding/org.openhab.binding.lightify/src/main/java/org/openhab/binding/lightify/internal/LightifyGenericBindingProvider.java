/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal;

import java.util.Arrays;

import org.openhab.binding.lightify.LightifyBindingConfig;
import org.openhab.binding.lightify.LightifyBindingConfig.Type;
import org.openhab.binding.lightify.LightifyBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for parsing the binding configuration.
 *
 * @author Till Klocke
 * @since 1.9.0
 */
public class LightifyGenericBindingProvider extends AbstractGenericBindingProvider implements LightifyBindingProvider {

    private final static Logger logger = LoggerFactory.getLogger(LightifyGenericBindingProvider.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBindingType() {
        return "lightify";
    }

    /**
     * @{inheritDoc
     */
    @Override
    public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
        if (!(item instanceof SwitchItem || item instanceof DimmerItem) || item instanceof ColorItem) {
            throw new BindingConfigParseException("item '" + item.getName() + "' is of type '"
                    + item.getClass().getSimpleName()
                    + "', only Switch-, Color- and DimmerItems are allowed - please check your *.items configuration");
        }
    }

    /**
     * Binding config is in the style of {lightify="FF:bb:...,LUMINANCE,60"}
     */
    @Override
    public void processBindingConfiguration(String context, Item item, String bindingConfig)
            throws BindingConfigParseException {
        logger.debug("Processing binding config {}", bindingConfig);
        super.processBindingConfiguration(context, item, bindingConfig);

        String[] parts = bindingConfig.split(",");
        if (parts.length < 2 || parts.length > 3) {
            logger.debug("Wrong number of arguments");
            throw new BindingConfigParseException("Can't parse binding config");
        }
        LightifyBindingConfig.Type type = LightifyBindingConfig.Type.valueOf(parts[1]);
        logger.debug("Binding config type is {}", type);
        if (type == null) {
            throw new BindingConfigParseException("Unknown binding type");
        }
        int time = -1;
        if (parts.length == 3) {
            logger.debug("Parsing time for transition");
            time = Integer.parseInt(parts[2]);
        }

        byte[] address;
        try {
            if (parts[0].contains(":")) {
                String[] addressParts = parts[0].split(":");
                if (addressParts.length != 8) {
                    throw new BindingConfigParseException("Illegal address format");
                }
                address = new byte[8];
                for (int i = 0; i < 8; i++) {
                    address[i] = (byte) Integer.parseInt(addressParts[i], 16);
                }
            } else {
                address = new byte[] { (byte) Integer.parseInt(parts[0], 16), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                        0x00 };
            }
        } catch (NumberFormatException e) {
            logger.error("Can't parse address {}", parts[0], e);
            throw new BindingConfigParseException("Can't parse address");
        }
        logger.debug("Creating binding config for {}, {}, {} (item {})", parts[0], type, time, item.getName());

        LightifyBindingConfig config = new LightifyBindingConfig(address, type, time, item);
        addBindingConfig(item, config);
    }

    @Override
    public LightifyBindingConfig getConfigForItem(String itemName) {
        return (LightifyBindingConfig) this.bindingConfigs.get(itemName);
    }

    @Override
    public LightifyBindingConfig getConfigForAddressAndType(byte[] address, Type type) {
        for (BindingConfig c : this.bindingConfigs.values()) {
            LightifyBindingConfig config = (LightifyBindingConfig) c;
            if (Arrays.equals(address, config.getAddress()) && type.equals(config.getType())) {
                return config;
            }
        }
        return null;
    }

}
