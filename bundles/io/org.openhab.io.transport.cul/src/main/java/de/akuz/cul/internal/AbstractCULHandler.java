package de.akuz.cul.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.akuz.cul.CULCommunicationException;
import de.akuz.cul.CULDeviceException;
import de.akuz.cul.CULHandler;
import de.akuz.cul.CULListener;
import de.akuz.cul.CULMode;

/**
 * Abstract base class for all CULHandler which brings some convenience
 * regarding registering listeners and detecting forbidden messages.
 * 
 * @author Till Klocke
 * @since 1.4.0
 */
public abstract class AbstractCULHandler implements CULHandler, CULHandlerInternal {

	private class SendThread extends Thread {
		@Override
		public void run() {
			while (!isInterrupted()) {
				String command = sendQueue.poll();
				if (command != null) {
					if (!command.endsWith("\r\n")) {
						command = command + "\r\n";
					}
					try {
						writeMessage(command);
					} catch (CULCommunicationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static class NotifyDataReceivedRunner implements Runnable {

		private String message;
		private CULListener listener;

		public NotifyDataReceivedRunner(CULListener listener, String message) {
			this.message = message;
			this.listener = listener;
		}

		@Override
		public void run() {
			listener.dataReceived(message);
		}

	}

	protected Executor receiveExecutor = Executors.newCachedThreadPool();
	protected SendThread sendThread = new SendThread();

	protected String deviceName;
	protected CULMode mode;

	protected List<CULListener> listeners = new ArrayList<CULListener>();

	protected Queue<String> sendQueue = new ConcurrentLinkedQueue<String>();

	protected AbstractCULHandler(String deviceName, CULMode mode) {
		this.mode = mode;
		this.deviceName = deviceName;
	}

	@Override
	public CULMode getCULMode() {
		return mode;
	}

	@Override
	public void registerListener(CULListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	@Override
	public void unregisterListener(CULListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	@Override
	public boolean hasListeners() {
		return listeners.size() > 0;
	}

	@Override
	public void open() throws CULDeviceException {
		openHardware();
		sendThread.start();
	}

	@Override
	public void close() {
		sendThread.interrupt();
		closeHardware();
	}

	protected abstract void openHardware()  throws CULDeviceException;

	protected abstract void closeHardware();

	@Override
	public void send(String command) {
		if (isMessageAllowed(command)) {
			sendQueue.add(command);
		}
	}

	@Override
	public void sendWithoutCheck(String message) throws CULCommunicationException {
		sendQueue.add(message);
	}

	protected abstract void writeMessage(String message) throws CULCommunicationException;

	/**
	 * Checks if the message would alter the RF mode of this device.
	 * 
	 * @param message
	 *            The message to check
	 * @return true if the message doesn't alter the RF mode, false if it does.
	 */
	protected boolean isMessageAllowed(String message) {
		if (message.startsWith("X") || message.startsWith("x")) {
			return false;
		}
		if (message.startsWith("Ar")) {
			return false;
		}
		return true;
	}

	protected void notifyDataReceived(String data) {
		for (final CULListener listener : listeners) {
			receiveExecutor.execute(new NotifyDataReceivedRunner(listener, data));
		}
	}

	protected void notifyError(Exception e) {
		for (CULListener listener : listeners) {
			listener.error(e);
		}
	}
}
