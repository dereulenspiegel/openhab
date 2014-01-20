package org.openhab.io.transport.cul.mocks;

import org.openhab.io.transport.cul.CULDeviceException;
import org.openhab.io.transport.cul.CULHandler;
import org.openhab.io.transport.cul.CULManager;
import org.openhab.io.transport.cul.CULMode;

public class MockCULManager implements CULManager {

	private MockCULHandler culHandler;

	@Override
	public void close(CULHandler handler) {
		// Ignore

	}

	@Override
	public CULHandler getOpenCULHandler(String deviceName, CULMode mode) throws CULDeviceException {
		if (culHandler == null) {
			culHandler = new MockCULHandler();
		}
		return culHandler;
	}

	public MockCULHandler getMockCULHandler() {
		return culHandler;
	}

}
