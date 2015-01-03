package org.openhab.binding.arduinojunkers.internal;

import org.openhab.core.binding.BindingConfig;

public class ArduinoJunkersBindingConfig implements BindingConfig {
	public enum ConnectionType {
		SERIAL,UDP;
	}
	
	public enum ItemType {
		SET_PERCENT,TEMP;
	}
	public ItemType itemType;
	
	public ConnectionType connectionType;
	
	public String serialPort;
}