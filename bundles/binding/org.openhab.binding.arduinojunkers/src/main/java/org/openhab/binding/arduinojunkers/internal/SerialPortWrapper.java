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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.TooManyListenersException;

import org.openhab.binding.arduinojunkers.internal.ConnectionBackend.TempListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialPortWrapper implements SerialPortEventListener, Closeable {

	private final static Logger logger = LoggerFactory
			.getLogger(SerialPortWrapper.class);

	private final static int COMMAND_SET_PERCENT = 0x01;
	private final static int COMMAND_REQUEST_TEMP = 0x04;

	private SerialPort serialPort;

	private BufferedReader reader;
	private BufferedWriter writer;

	private TempListener tempListener;

	public SerialPortWrapper(ArduinoJunkersBindingConfig config)
			throws NoSuchPortException, PortInUseException,
			UnsupportedCommOperationException, IOException {
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(config.serialPort);
		CommPort port = portIdentifier.open(this.getClass().getName(), 2000);
		final SerialPort serialPort = (SerialPort) port;
		serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
				SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

		this.serialPort = serialPort;
		reader = new BufferedReader(new InputStreamReader(
				serialPort.getInputStream()));

		try {
			serialPort.addEventListener(this);
		} catch (TooManyListenersException e) {
			// This should never happen
			serialPort.removeEventListener();
			try {
				serialPort.addEventListener(this);
			} catch (TooManyListenersException e1) {
				logger.error(
						"Can't register listener to read responses, looks like a race condition",
						e1);
			}
		}
		serialPort.notifyOnDataAvailable(true);

		writer = new BufferedWriter(new OutputStreamWriter(
				serialPort.getOutputStream()));
	}

	public void setPercent(int percent) {
		try {
			writer.write(COMMAND_SET_PERCENT);
			writer.write(percent);
			writer.flush();
		} catch (IOException e) {
			logger.error("Error while setting percentage of the heater", e);
		}
	}

	public void requestTemperature(TempListener listener) {
		try {
			this.tempListener = listener;
			writer.write(COMMAND_REQUEST_TEMP);
			writer.write(0x00);
			writer.flush();
		} catch (IOException e) {
			logger.error("Error while requesting the temperature", e);
		}

	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				String readLine = reader.readLine();
				logger.debug("Received response from Arduino: " + readLine);
				if (readLine.startsWith("TEMP:")) {
					float temp = Float.parseFloat(readLine.substring(5));
					synchronized (tempListener) {
						if (tempListener != null) {
							tempListener.tempReceived(temp);
							tempListener = null;
						}
					}
				} else if (readLine.startsWith("WARNING:")) {
					logger.warn("Received warning from arduino: {}", readLine);
				}
			} catch (IOException e) {
				logger.error("Error while reading responses from arduino", e);
			}
		}

	}

	@Override
	public void close() {
		serialPort.removeEventListener();
		serialPort.close();
	}

}
