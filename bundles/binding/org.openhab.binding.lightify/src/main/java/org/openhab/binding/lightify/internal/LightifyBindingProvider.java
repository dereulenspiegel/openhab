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
import org.openhab.binding.lightify.LightifyGenericBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;

/**
 * This class is responsible for parsing the binding configuration.
 *
 * @author Till Klocke
 * @since 1.9.0
 */
public class LightifyBindingProvider extends AbstractGenericBindingProvider implements LightifyGenericBindingProvider {

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
     * Binding config is in the style of {lightify="address=FF:bb:...,type=LUMINANCE,time=60"} {@inheritDoc}
     */
    @Override
    public void processBindingConfiguration(String context, Item item, String bindingConfig)
            throws BindingConfigParseException {
        super.processBindingConfiguration(context, item, bindingConfig);

        String[] parts = bindingConfig.split(",");
        if (parts.length < 2 || parts.length > 3) {
            throw new BindingConfigParseException("Can't parse binding config");
        }
        LightifyBindingConfig.Type type = LightifyBindingConfig.Type.valueOf(parts[1]);
        if (type == null) {
            throw new BindingConfigParseException("Unknown binding type");
        }
        int time = -1;
        if (parts.length == 3) {
            time = Integer.parseInt(parts[2]);
        }

        byte[] address;
        if (parts[0].contains(":")) {
            String[] addressParts = parts[0].split(":");
            if (addressParts.length != 8) {
                throw new BindingConfigParseException("Illegal address format");
            }
            address = new byte[8];
            for (int i = 0; i < 8; i++) {
                address[i] = Byte.parseByte(addressParts[i], 16);
            }
        } else {
            address = new byte[] { Byte.parseByte(parts[0], 16), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
        }

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
