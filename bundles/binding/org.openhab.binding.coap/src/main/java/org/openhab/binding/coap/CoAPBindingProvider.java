/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coap;

import org.openhab.binding.coap.internal.CoAPGenericBindingProvider.CoAPBindingConfig;
import org.openhab.core.binding.BindingProvider;

/**
 * @author Till Klocke
 * @since 1.7.0
 */
public interface CoAPBindingProvider extends BindingProvider {
	
	public CoAPBindingConfig getBindingConfigForItem(String itemName);

}
