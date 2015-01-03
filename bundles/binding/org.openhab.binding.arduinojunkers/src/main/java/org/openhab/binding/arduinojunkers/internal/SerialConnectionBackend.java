package org.openhab.binding.arduinojunkers.internal;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.arduinojunkers.internal.ArduinoJunkersBindingConfig.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialConnectionBackend implements ConnectionBackend {

	private final static Logger logger = LoggerFactory
			.getLogger(SerialConnectionBackend.class);

	private Map<String, SerialPortWrapper> openWrappers = new HashMap<String, SerialPortWrapper>();

	public SerialConnectionBackend() {

	}

	@Override
	public void setValue(ArduinoJunkersBindingConfig config, int percent) {
		if (config.itemType != ItemType.SET_PERCENT) {
			return;
		}
		try {
			SerialPortWrapper wrapper = getWrapper(config);
			wrapper.setPercent(percent);
		} catch (NoSuchPortException e) {
			logger.error("The port {} does not exist", config.serialPort);
		} catch (PortInUseException e) {
			logger.error("The port {} is already in use", config.serialPort);
		} catch (UnsupportedCommOperationException e) {
			logger.error("Unsupported operation", e);
		} catch (IOException e) {
			logger.error("Error while communicating over {}",
					config.serialPort, e);
		}
	}

	private SerialPortWrapper getWrapper(ArduinoJunkersBindingConfig config)
			throws NoSuchPortException, PortInUseException,
			UnsupportedCommOperationException, IOException {
		if (openWrappers.containsKey(config.serialPort)) {
			return openWrappers.get(config.serialPort);
		}
		SerialPortWrapper wrapper = new SerialPortWrapper(config);
		openWrappers.put(config.serialPort, wrapper);
		return wrapper;
	}

	@Override
	public void shutdown() {
		for (SerialPortWrapper wrapper : openWrappers.values()) {
			wrapper.close();
		}

	}

	@Override
	public void requestTemperature(ArduinoJunkersBindingConfig config,
			TempListener listener) {
		if (config.itemType != ItemType.TEMP) {
			return;
		}

		try {
			SerialPortWrapper wrapper = getWrapper(config);
			wrapper.requestTemperature(listener);
		} catch (NoSuchPortException e) {
			logger.error("The port {} does not exist", config.serialPort);
		} catch (PortInUseException e) {
			logger.error("The port {} is already in use", config.serialPort);
		} catch (UnsupportedCommOperationException e) {
			logger.error("Unsupported operation", e);
		} catch (IOException e) {
			logger.error("Error while communicating over {}",
					config.serialPort, e);
		}

	}

}
