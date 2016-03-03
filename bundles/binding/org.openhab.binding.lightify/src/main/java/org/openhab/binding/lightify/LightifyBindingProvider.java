/**
 * Copyright (c) 2010-2016, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lightify;

import org.openhab.binding.lightify.LightifyBindingConfig.Type;
import org.openhab.core.binding.BindingProvider;

/**
 * @author Till Klocke
 * @since 1.9.0
 */
public interface LightifyBindingProvider extends BindingProvider {

    public LightifyBindingConfig getConfigForItem(String itemName);

    public LightifyBindingConfig getConfigForAddressAndType(byte[] address, Type type);

    public LightifyBindingConfig getConfigForName(String name, Type type);

}
