package org.openhab.io.transport.cul.mocks;

import java.util.ArrayList;
import java.util.List;

import org.openhab.io.transport.cul.CULCommunicationException;
import org.openhab.io.transport.cul.CULHandler;
import org.openhab.io.transport.cul.CULListener;
import org.openhab.io.transport.cul.CULMode;

public class MockCULHandler implements CULHandler {

	private List<CULListener> listeners = new ArrayList<CULListener>();

	private List<String> sendCommands = new ArrayList<String>();

	@Override
	public void registerListener(CULListener listener) {
		listeners.add(listener);
	}

	@Override
	public void unregisterListener(CULListener listener) {
		listeners.remove(listener);

	}

	@Override
	public void send(String command) throws CULCommunicationException {
		sendCommands.add(command);

	}

	@Override
	public CULMode getCULMode() {
		return CULMode.SLOW_RF;
	}

	public void simulateReceivedCommand(String command) {
		for (CULListener listener : listeners) {
			listener.dataReceived(command);
		}
	}

	public List<String> getSendCommands() {
		return sendCommands;
	}

	public void clear() {
		sendCommands.clear();
	}

	public String getLastSendCommand() {
		if (sendCommands.size() > 0) {
			return sendCommands.get(0);
		}
		return null;
	}

}
