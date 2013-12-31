package de.akuz.cul;

import java.util.Dictionary;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.binding.BindingProvider;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class to develop new CUL based bindings. Many things regarding handling
 * CUL devices are already implemented here. Simply implement the missing
 * methods and don't worry about opening or closing the CUL handler.
 * 
 * @author Till Klocke
 * 
 * @param <P>
 *            BindingProvider
 */
public abstract class AbstractCULBinding<P extends BindingProvider> extends AbstractActiveBinding<P> implements
		ManagedService, CULListener {

	private final static Logger logger = LoggerFactory.getLogger(AbstractCULBinding.class);

	protected final static String KEY_DEVICE_NAME = "device";

	protected String deviceName;

	protected CULHandler cul;

	protected void setNewDeviceName(String deviceName) {
		if (deviceName == null) {
			logger.error("Device name was null");
			return;
		}
		if (this.deviceName != null && this.deviceName.equals(deviceName)) {
			return;
		}
		closeCUL();
		this.deviceName = deviceName;
		openCUL();
	}

	protected void openCUL() {
		try {
			cul = CULManager.getOpenCULHandler(deviceName, CULMode.SLOW_RF);
			cul.registerListener(this);
		} catch (CULDeviceException e) {
			logger.error("Can't open CUL handler for device " + deviceName, e);
		}
	}

	protected void closeCUL() {
		if (cul != null) {
			cul.unregisterListener(this);
			CULManager.close(cul);
		}
	}

	@Override
	public final void updated(Dictionary<String, ?> config) throws ConfigurationException {
		logger.debug("Received new config");
		if (config != null) {
			String deviceName = (String) config.get(KEY_DEVICE_NAME);
			if (StringUtils.isEmpty(deviceName)) {
				logger.error("No device name configured");
				setProperlyConfigured(false);
				throw new ConfigurationException(KEY_DEVICE_NAME, "The device name can't be empty");
			} else {
				setNewDeviceName(deviceName);
			}

			try {
				dictionaryUpdated(config);
				setProperlyConfigured(true);
			} catch (ConfigurationException e) {
				setProperlyConfigured(false);
				throw e;
			}
		}
	}

	protected abstract void dictionaryUpdated(Dictionary<String, ?> config) throws ConfigurationException;

}
