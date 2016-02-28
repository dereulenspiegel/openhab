/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify.internal;

import java.awt.Color;
import java.io.IOException;
import java.util.Dictionary;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.lightify.LightifyBindingConfig;
import org.openhab.binding.lightify.LightifyBindingConfig.Type;
import org.openhab.binding.lightify.LightifyGenericBindingProvider;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.akuz.lightify.Gateway;
import de.akuz.lightify.Group;
import de.akuz.lightify.Light;
import de.akuz.lightify.Luminary;
import de.akuz.lightify.Luminary.ChangeListener;

/**
 *
 * @author Till Klocke
 * @since 1.9.0
 */
public class LightifyBinding extends AbstractActiveBinding<LightifyGenericBindingProvider>
        implements ManagedService, ChangeListener {

    private static final Logger logger = LoggerFactory.getLogger(LightifyBinding.class);

    private Gateway gateway;

    private int defaultTransitionTime = 10;
    private final static String KEY_HOST = "host";
    private final static String KEY_TRANSITION_TIME = "transition";

    /**
     * the refresh interval which is used to poll values from the lightify gateway
     * (optional, defaults to 60000ms)
     */
    private long refreshInterval = 60000;

    public LightifyBinding() {
    }

    @Override
    public void activate() {
        logger.debug("Activating Lightify binding");
    }

    @Override
    public void deactivate() {
        logger.debug("Deactivating lightify binding");
        try {
            gateway.disconnect();
        } catch (IOException e) {
            logger.error("Error while disconnecting from gateway", e);
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
        return "Lightify Refresh Service";
    }

    /**
     * @{inheritDoc
     */
    @Override
    protected void execute() {
        updateLuminaries();
    }

    /**
     * @{inheritDoc
     */
    @Override
    protected void internalReceiveCommand(String itemName, Command command) {
        LightifyBindingConfig config = getBindingConfigForItem(itemName);
        if (config != null) {
            short time = (short) defaultTransitionTime;
            if (config.getTime() != -1) {
                time = (short) config.getTime();
                Luminary lum = gateway.getLuminary(config.getAddress());
                if (lum != null) {
                    try {
                        switch (config.getType()) {
                            case SWITCH:
                                OnOffType onOffCommand = (OnOffType) command;
                                lum.setOn(onOffCommand == OnOffType.ON);
                                break;
                            case TEMPERATURE:
                                PercentType percentCommand = (PercentType) command;
                                lum.setTemperature(percentCommand.shortValue(), time);
                                break;
                            case LUMINANCE:
                                PercentType percentLuminanceCommand = (PercentType) command;
                                lum.setLuminance(percentLuminanceCommand.byteValue(), time);
                                break;
                            case COLOR:
                                HSBType colorType = (HSBType) command;
                                byte red = colorType.getRed().byteValue();
                                byte green = colorType.getGreen().byteValue();
                                byte blue = colorType.getBlue().byteValue();
                                lum.setColor(red, green, blue, time);
                                break;

                        }
                    } catch (IOException e) {
                        logger.error("Exception while sending command to Lightify gateway", e);
                    }
                }

            } else {
                logger.warn("Received command for unknown luminary. Item {}", itemName);
            }
        }

    }

    protected void addBindingProvider(LightifyGenericBindingProvider bindingProvider) {
        super.addBindingProvider(bindingProvider);
    }

    protected void removeBindingProvider(LightifyGenericBindingProvider bindingProvider) {
        super.removeBindingProvider(bindingProvider);
    }

    private LightifyBindingConfig getBindingConfigForItem(String itemName) {
        for (BindingProvider provider : this.providers) {
            LightifyBindingConfig config = ((LightifyGenericBindingProvider) provider).getConfigForItem(itemName);
            if (config != null) {
                return config;
            }
        }
        return null;
    }

    private LightifyBindingConfig getBindingConfigAddressAndType(byte[] address, Type type) {
        for (BindingProvider provider : this.providers) {
            LightifyBindingConfig config = ((LightifyGenericBindingProvider) provider)
                    .getConfigForAddressAndType(address, type);
            if (config != null) {
                return config;
            }
        }
        return null;
    }

    private void updateLuminaries() {
        try {
            List<Light> lights = gateway.refreshAllLights();
            List<Group> groups = gateway.refreshGroups();

            for (Light l : lights) {
                l.registerListener(this);
            }
            for (Group g : groups) {
                g.registerListener(this);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("Error while updating luminaries", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updated(Dictionary<String, ?> config) throws ConfigurationException {
        logger.debug("Received new config");
        if (config != null) {

            Boolean configChanged = false;

            String host = (String) config.get(KEY_HOST);
            if (host == null || host.isEmpty()) {
                throw new ConfigurationException(KEY_HOST, "Host must not be empty");
            }

            if (gateway != null) {
                try {
                    gateway.disconnect();
                } catch (IOException e) {
                    logger.warn("Error while disconnecting from gateway before connecting to new host", e);
                }
            }
            gateway = new Gateway(host);
            try {
                gateway.connect();
                updateLuminaries();
            } catch (IOException e) {
                throw new ConfigurationException(KEY_HOST, "Can't connect to configured host", e);
            }
            defaultTransitionTime = Integer.parseInt((String) config.get(KEY_TRANSITION_TIME));
            // to override the default refresh interval one has to add a
            // parameter to openhab.cfg like
            // <bindingName>:refresh=<intervalInMs>
            String refreshIntervalString = (String) config.get("refresh");
            if (StringUtils.isNotBlank(refreshIntervalString)) {
                refreshInterval = Long.parseLong(refreshIntervalString);
            }

            setProperlyConfigured(true);
            // read further config parameters here ...

        }
    }

    @Override
    public void luminaryColorUpdated(Luminary lum, byte red, byte green, byte blue) {
        LightifyBindingConfig config = getBindingConfigAddressAndType(lum.getAddressBytes(), Type.COLOR);
        if (config != null) {
            // TODO is 100 for alpha correct?
            HSBType colorType = new HSBType(new Color(red, green, blue, 100));
            eventPublisher.postUpdate(config.getItem().getName(), colorType);
        }

    }

    @Override
    public void luminaryLuminanceUpdated(Luminary lum, short luminance) {
        LightifyBindingConfig config = getBindingConfigAddressAndType(lum.getAddressBytes(), Type.LUMINANCE);
        if (config != null) {
            PercentType luminanceUpdate = new PercentType(luminance);
            eventPublisher.postUpdate(config.getItem().getName(), luminanceUpdate);
        }

    }

    @Override
    public void luminarySwitchedOnUpdated(Luminary lum, boolean state) {
        LightifyBindingConfig config = getBindingConfigAddressAndType(lum.getAddressBytes(), Type.SWITCH);
        if (config != null) {
            OnOffType update = state ? OnOffType.ON : OnOffType.OFF;
            eventPublisher.postUpdate(config.getItem().getName(), update);
        }

    }

    @Override
    public void luminaryTemperatureUpdated(Luminary lum, short temp) {
        LightifyBindingConfig config = getBindingConfigAddressAndType(lum.getAddressBytes(), Type.TEMPERATURE);
        if (config != null) {
            PercentType update = new PercentType(temp);
            eventPublisher.postUpdate(config.getItem().getName(), update);
        }
    }

}
