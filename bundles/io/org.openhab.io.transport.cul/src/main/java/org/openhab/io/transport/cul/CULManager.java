/**
 * Copyright (c) 2010-2014, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.cul;

public interface CULManager {

	public void close(CULHandler handler);

	public CULHandler getOpenCULHandler(String deviceName, CULMode mode) throws CULDeviceException;

}
