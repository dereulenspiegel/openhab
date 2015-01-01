package org.openhab.binding.arduinojunkers.internal;

public interface ConnectionBackend {
	
	public void setValue(ArduinoJunkersBindingConfig config, int percent);
	
	public void shutdown();

}
