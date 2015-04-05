package org.openhab.binding.coap.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.openhab.binding.coap.CoAPBindingProvider;
import org.openhab.binding.coap.internal.CoAPGenericBindingProvider.CoAPBindingConfig;
import org.openhab.core.binding.BindingChangeListener;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenHABCoapServer extends CoapServer implements
		BindingChangeListener {

	private final static Logger logger = LoggerFactory
			.getLogger(OpenHABCoapServer.class);

	private EventPublisher eventPublisher;

	private Collection<CoAPBindingProvider> bindingProviders;

	private Map<String, CoapResource> resourceMap = new HashMap<String, CoapResource>();

	public OpenHABCoapServer(EventPublisher eventPublisher,
			Collection<CoAPBindingProvider> bindingProviders) {
		this.eventPublisher = eventPublisher;
		this.bindingProviders = bindingProviders;

		for (CoAPBindingProvider provider : bindingProviders) {
			for (String itemName : provider.getItemNames()) {
				addItemResource(itemName);
			}
		}
	}

	public void notifyResourceChanged(String itemName) {
		CoapResource resource = resourceMap.get(itemName);
		if (resource != null) {
			resource.changed();
		}
	}

	private void addItemResource(String itemName) {
		logger.debug("Adding item resource for item {}", itemName);
		CoAPBindingConfig config = getConfigForItem(itemName);
		if (resourceMap.containsKey(itemName)) {
			removeResource(itemName);
		}
		if (config != null) {
			CoapResource resource = createCoapResourceForItemConfig(config);
			resourceMap.put(itemName, resource);
			this.add(resource);
		} else {
			logger.warn("Didn't find binding config for item {}", itemName);
		}
	}

	private void removeResource(String itemName) {
		logger.debug("Removing item resource for item {}", itemName);
		CoapResource resource = resourceMap.get(itemName);
		if (resource != null) {
			this.remove(resource);
			resourceMap.remove(itemName);
		} else {
			logger.warn("Can't remove resource for item {}", itemName);
		}
	}

	private CoAPBindingConfig getConfigForItem(String itemName) {
		for (CoAPBindingProvider provider : bindingProviders) {
			CoAPBindingConfig config = provider
					.getBindingConfigForItem(itemName);
			if (config != null) {
				return config;
			}
		}
		return null;
	}

	private CoapResource createCoapResourceForItemConfig(
			CoAPBindingConfig config) {
		CoapResource resource = new OpenHABCoAPResource(config, eventPublisher);

		return resource;
	}

	@Override
	public void bindingChanged(BindingProvider provider, String itemName) {
		CoAPBindingConfig config = getConfigForItem(itemName);
		if (config != null) {
			addItemResource(itemName);
		} else {
			removeResource(itemName);
		}

	}

	@Override
	public void allBindingsChanged(BindingProvider provider) {
		for (String itemName : resourceMap.keySet()) {
			removeResource(itemName);
		}
		Collection<String> itemNames = provider.getItemNames();
		for (String itemName : itemNames) {
			addItemResource(itemName);
		}
	}

}
