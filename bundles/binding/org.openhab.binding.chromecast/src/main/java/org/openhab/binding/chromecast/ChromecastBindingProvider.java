/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.chromecast;

import java.util.List;

import org.openhab.binding.chromecast.internal.ChromeCastProperties;
import org.openhab.binding.chromecast.internal.ChromecastGenericBindingProvider.ChromecastBindingConfig;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.items.Item;

/**
 * @author Till Klocke
 * @since 1.6.0
 */
public interface ChromecastBindingProvider extends BindingProvider {
	
	public ChromecastBindingConfig getBindingConfigFor(String deviceName, ChromeCastProperties property);
	
	public ChromecastBindingConfig getBindingConfigFor(Item item);
	
	public List<ChromecastBindingConfig> getBindingConfigsFor(String deviceName);

}
