package org.openhab.binding.xbmc.internal.client.messages.parser;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.xbmc.internal.client.messages.XBMCMessage;
import org.openhab.binding.xbmc.internal.client.messages.data.XBMCData;

public class DataParserFactory {

	private static Map<String, DataParser<? extends XBMCData>> registeredParser = new HashMap<String, DataParser<? extends XBMCData>>();

	public static DataParser<? extends XBMCData> getParserForMessage(XBMCMessage message) {
		return registeredParser.get(message.getMethod());
	}

	public static void registerParser(String methodName, DataParser<? extends XBMCData> parser) {
		registeredParser.put(methodName, parser);
	}
}
