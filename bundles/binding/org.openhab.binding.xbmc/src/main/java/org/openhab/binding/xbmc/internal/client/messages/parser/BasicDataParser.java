package org.openhab.binding.xbmc.internal.client.messages.parser;

import org.codehaus.jackson.map.ObjectMapper;
import org.openhab.binding.xbmc.internal.client.messages.XBMCMessage;
import org.openhab.binding.xbmc.internal.client.messages.data.XBMCData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicDataParser<D extends XBMCData> implements DataParser<D> {

	private final static Logger logger = LoggerFactory.getLogger(BasicDataParser.class);

	private Class<D> dataClass;

	private ObjectMapper mapper = new ObjectMapper();

	public BasicDataParser(Class<D> dataClass) {
		this.dataClass = dataClass;
	}

	@Override
	public D parseXBMCData(XBMCMessage message) {
		String jsonString = message.getParams().getData().toString();
		try {
			return mapper.readValue(jsonString, dataClass);
		} catch (Exception e) {
			logger.error("Can't parse XBMC data: " + jsonString, e);
		}

		return null;
	}

}
