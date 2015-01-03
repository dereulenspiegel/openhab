package org.openhab.binding.arduinojunkers.internal;

public interface ConnectionBackend {

	public interface TempListener {
		public void tempReceived(float temp);
	}

	public void setValue(ArduinoJunkersBindingConfig config, int percent);

	public void requestTemperature(ArduinoJunkersBindingConfig config,
			TempListener listener);

	public void shutdown();

}
