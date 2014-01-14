package de.akuz.cul;

import java.util.Dictionary;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.binding.BindingProvider;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

public abstract class AbstractCULBinding<T extends BindingProvider> extends AbstractActiveBinding<T> implements
		CULListener, ManagedService {

	private final static String KEY_DEVICE = "device";

	protected CULManager culManager;
	protected CULHandler cul;

	protected String deviceAddress;

	public void setCULManager(CULManager manager) {
		this.culManager = manager;
	}

	public void unsetCULManager(CULManager manager) {
		this.culManager = null;
	}

	protected void openCUL(String address, CULMode mode) throws CULDeviceException {
		if (cul != null) {
			culManager.close(cul);
		}
		cul = culManager.getOpenCULHandler(address, mode);
	}

	protected void closeCUL() {
		culManager.close(cul);
	}

	protected abstract void parseMessage(String data);

	protected abstract void parseConfig(Dictionary<String, ?> config) throws ConfigurationException;
	
	@Override
	public void dataReceived(String data) {
		parseMessage(data);

	}

	@Override
	public void updated(Dictionary<String, ?> config) throws ConfigurationException {
		if (config != null) {
			deviceAddress = parseMandatoryValue(KEY_DEVICE, config);
			parseConfig(config);
		}

	}
	

	protected String parseMandatoryValue(String key, Dictionary<String, ?> config) throws ConfigurationException {
		String value = (String) config.get(key);
		if (StringUtils.isEmpty(value)) {
			setProperlyConfigured(false);
			throw new ConfigurationException(key, "Configuration option " + key + " is mandatory");
		}
		return value;
	}

}
