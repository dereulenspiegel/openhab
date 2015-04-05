package org.openhab.binding.coap.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.openhab.binding.coap.internal.CoAPGenericBindingProvider.CoAPBindingConfig;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.transform.TransformationException;
import org.openhab.core.transform.TransformationHelper;
import org.openhab.core.transform.TransformationService;
import org.openhab.core.types.Command;
import org.openhab.core.types.TypeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenHABCoAPResource extends CoapResource {

	private final static Logger logger = LoggerFactory
			.getLogger(OpenHABCoAPResource.class);

	private TransformationService transformationService;

	private CoAPBindingConfig bindingConfig;

	private EventPublisher eventPublisher;

	public OpenHABCoAPResource(CoAPBindingConfig config,
			EventPublisher eventPublisher) {
		super(config.item.getName());
		this.eventPublisher = eventPublisher;
		this.bindingConfig = config;
		if (!StringUtils.isEmpty(config.transformationFunction)) {
			transformationService = TransformationHelper
					.getTransformationService(CoAPBinding.bundleContext,
							config.transformationType);
		}
		setObservable(true);
		getAttributes().addResourceType("observe");
		getAttributes().setObservable();
		setObserveType(Type.CON);
	}

	@Override
	public void handleGET(CoapExchange exchange) {
		logger.debug("Handling GET for resource {}",
				bindingConfig.item.getName());
		String state = bindingConfig.item.getState().format("%s");
		String response = transform(state);
		exchange.respond(response);
	}

	@Override
	public void handlePOST(CoapExchange exchange) {
		logger.debug("Handling POST requets for resource {}",
				bindingConfig.item.getName());
		String payload = new String(exchange.getRequestPayload());
		String stateString = transform(payload);

		Command command = convertPayloadToType(stateString);

		if (command != null) {
			eventPublisher.postCommand(bindingConfig.item.getName(), command);
		} else {
			logger.warn("Can't transform {} to a command for item {}", payload,
					bindingConfig.item.getName());
		}

	}

	private Command convertPayloadToType(String payload) {
		return TypeParser.parseCommand(
				bindingConfig.item.getAcceptedCommandTypes(), payload);
	}

	private String transform(String in) {
		if (StringUtils.isEmpty(bindingConfig.transformationFunction)) {
			return in;
		}

		String out = "";
		try {
			out = transformationService.transform(
					bindingConfig.transformationFunction, in);
		} catch (TransformationException e) {
			logger.error(
					"Error transforming reponse for GET request for item {}",
					bindingConfig.item.getName(), e);
		}
		return out;
	}

}
