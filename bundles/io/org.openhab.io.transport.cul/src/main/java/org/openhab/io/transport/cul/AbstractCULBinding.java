package org.openhab.io.transport.cul;

import java.util.Dictionary;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.binding.BindingProvider;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;

/**
 * Base class for all CUL based bindings. This class handles some of the ground
 * work like managing the connection to the CUL device and parsing the device
 * address from the configuration dictionary.
 * 
 * @author Till Klocke
 * @since 1.5.0
 * 
 * @param <T>
 *            The BindingProvider used by the binding implementation
 */
public abstract class AbstractCULBinding<T extends BindingProvider> extends AbstractActiveBinding<T> implements
		CULListener, ManagedService {

	private final static String KEY_DEVICE = "device";

	/**
	 * The injected {@link CULManager} used to to open and close the CUL device.
	 */
	protected CULManager culManager;
	/**
	 * The {@link CULHandler}. Bindings must provide a valid {@link CULMode}.
	 * The device address is parsed automatically from the configuration
	 * dictionary.
	 */
	protected CULHandler cul;

	/**
	 * The parsed device address.
	 */
	protected String deviceAddress;

	@Override
	public void activate() {
		getLogger().info(getName() + " has been activated");
	}

	@Override
	public void deactivate() {
		closeCUL();
		getLogger().info(getName() + " has been deactivated");
	}

	public void setCULManager(CULManager manager) {
		getLogger().debug("Received CULManager");
		this.culManager = manager;
	}

	public void unsetCULManager(CULManager manager) {
		getLogger().debug("Lost CULManager");
		this.culManager = null;
	}

	/**
	 * Open the CUL device with the given address in the specified mode.
	 * 
	 * @param address
	 * @param mode
	 * @throws CULDeviceException
	 */
	private void openCUL(String address, CULMode mode) throws CULDeviceException {
		getLogger().debug("Opening CUL device with address " + address + " in mode " + mode.toString());
		cul = culManager.getOpenCULHandler(address, mode);
		cul.registerListener(this);
	}

	/**
	 * Sets a new device address. Closes and reopens the CUL if it was already
	 * open.
	 * 
	 * @param deviceAddress
	 */
	protected void setDeviceAddress(String deviceAddress) {
		closeCUL();
		this.deviceAddress = deviceAddress;
		try {
			openCUL(deviceAddress, getCULMode());
			culOpen();
		} catch (CULDeviceException e) {
			getLogger().error("Can't open CUL device with address " + deviceAddress, e);
		}
	}

	/**
	 * Return a non null, valid logger so this abstract base class can append
	 * log messages.
	 * 
	 * @return {@link Logger} a Logger must not be null.
	 */
	protected abstract Logger getLogger();

	/**
	 * Return the CULMode in which the device should be openend.
	 * 
	 * @return {@link CULMode} must not be null
	 */
	protected abstract CULMode getCULMode();

	/**
	 * Callback method which gets called after a CUL device has been openend.
	 */
	protected abstract void culOpen();

	protected void closeCUL() {
		if (cul != null) {
			cul.unregisterListener(this);
			culManager.close(cul);
		}
	}

	/**
	 * This method receives all received messages unmodified.
	 * 
	 * @param data
	 *            the received binary messages from the CUL device.
	 */
	protected abstract void parseMessage(String data);

	/**
	 * Parse additional configuration data. the CUL device address is already
	 * parsed in this base class.
	 * 
	 * @param config
	 * @throws ConfigurationException
	 */
	protected abstract void parseConfig(Dictionary<String, ?> config) throws ConfigurationException;

	@Override
	public void dataReceived(String data) {
		parseMessage(data);

	}

	@Override
	public void updated(Dictionary<String, ?> config) throws ConfigurationException {
		if (config != null) {
			String deviceAddress = parseMandatoryValue(KEY_DEVICE, config);
			parseConfig(config);
			setProperlyConfigured(true);
			setDeviceAddress(deviceAddress);
		}

	}

	/**
	 * Convenience method to parse mandatory values from the dictionary.
	 * 
	 * @param key
	 * @param config
	 * @return
	 * @throws ConfigurationException
	 */
	protected String parseMandatoryValue(String key, Dictionary<String, ?> config) throws ConfigurationException {
		String value = (String) config.get(key);
		if (StringUtils.isEmpty(value)) {
			setProperlyConfigured(false);
			throw new ConfigurationException(key, "Configuration option " + key + " is mandatory");
		}
		return value;
	}

}
