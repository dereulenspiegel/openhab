package org.openhab.binding.arduinojunkers.internal;

import org.openhab.core.binding.BindingConfig;

public class ArduinoJunkersBindingConfig implements BindingConfig {
	public enum ConnectionType {
		SERIAL,UDP;
	}
	
	public ConnectionType connectionType;
	
	public String serialPort;
}