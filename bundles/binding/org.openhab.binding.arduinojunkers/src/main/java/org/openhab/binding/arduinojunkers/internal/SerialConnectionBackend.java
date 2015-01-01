package org.openhab.binding.arduinojunkers.internal;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialConnectionBackend implements ConnectionBackend {

	private final static Logger logger = LoggerFactory
			.getLogger(SerialConnectionBackend.class);

	Map<String, SerialPort> openPorts = new HashMap<String, SerialPort>();

	@Override
	public void setValue(ArduinoJunkersBindingConfig config, int percent) {
		try {
			SerialPort port = getPort(config);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					port.getOutputStream()));
			writer.write(percent);
			writer.flush();
			writer.close();
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

	private SerialPort getPort(ArduinoJunkersBindingConfig config)
			throws NoSuchPortException, PortInUseException,
			UnsupportedCommOperationException {
		if (openPorts.containsKey(config.serialPort)) {
			return openPorts.get(config.serialPort);
		}
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(config.serialPort);
		CommPort port = portIdentifier.open(this.getClass().getName(), 2000);
		final SerialPort serialPort = (SerialPort) port;
		serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
		try {
			serialPort.addEventListener(new SerialPortEventListener() {

				private BufferedReader reader = new BufferedReader(
						new InputStreamReader(serialPort.getInputStream()));

				@Override
				public void serialEvent(SerialPortEvent event) {
					if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
						try {
							String response = reader.readLine();
							logger.debug("Response from Arduino: {}", response);
						} catch (IOException e) {
							logger.warn("Can't read from arduino", e);
						}
					}

				}
			});
			serialPort.notifyOnDataAvailable(true);
		} catch (TooManyListenersException e) {
			logger.warn("Can't register debug listener", e);
		} catch (IOException e) {
			logger.warn("Can't create BufferedReader for reading responses");
		}
		openPorts.put(config.serialPort, serialPort);
		return serialPort;
	}

	@Override
	public void shutdown() {
		for (SerialPort port : openPorts.values()) {
			port.close();
		}

	}

}
