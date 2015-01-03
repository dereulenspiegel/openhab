/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.arduinojunkers;

import java.util.List;

import org.openhab.binding.arduinojunkers.internal.ArduinoJunkersBindingConfig;
import org.openhab.core.binding.BindingProvider;

/**
 * @author Till Klocke
 * @since 1.7.0
 */
public interface ArduinoJunkersBindingProvider extends BindingProvider {
	
	public ArduinoJunkersBindingConfig getConfigForItem(String itemName);
	
	public ArduinoJunkersBindingConfig getTempItemForDevice(ArduinoJunkersBindingConfig config);
	
	public List<ArduinoJunkersBindingConfig> getAllConfigs();
	
	public List<ArduinoJunkersBindingConfig> getAllTempConfigs();

}
