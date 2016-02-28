/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify;

import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;

/**
 * Binding config for Lightify devices.
 *
 * @author Till Klocke
 *
 */
public class LightifyBindingConfig implements BindingConfig {

    public enum Type {
        SWITCH,
        COLOR,
        TEMPERATURE,
        LUMINANCE;
    }

    private byte[] address;
    private Type type;
    private Item item;
    private int time;

    public LightifyBindingConfig(byte[] address, Type type, int time, Item item) {
        this.address = address;
        this.type = type;
        this.time = time;
        this.item = item;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

}
