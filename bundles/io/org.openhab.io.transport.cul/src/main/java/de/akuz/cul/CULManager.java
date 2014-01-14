package de.akuz.cul;

public interface CULManager {

	public void close(CULHandler handler);

	public CULHandler getOpenCULHandler(String deviceName, CULMode mode) throws CULDeviceException;

}
