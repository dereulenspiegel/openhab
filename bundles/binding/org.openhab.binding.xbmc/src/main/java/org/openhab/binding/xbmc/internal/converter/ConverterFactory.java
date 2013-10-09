package org.openhab.binding.xbmc.internal.converter;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.xbmc.internal.BindingTypes;
import org.openhab.binding.xbmc.internal.client.messages.XBMCMessage;

public class ConverterFactory {

	private static Map<String, OpenHABXBMCConverter> messageConverters = new HashMap<String, OpenHABXBMCConverter>();
	private static Map<BindingTypes, OpenHABXBMCConverter> typeConverters = new HashMap<BindingTypes, OpenHABXBMCConverter>();

	public static void registerConverter(BindingTypes type, String methodName, OpenHABXBMCConverter converter) {
		messageConverters.put(methodName, converter);
		typeConverters.put(type, converter);
	}

	public static OpenHABXBMCConverter getConverter(XBMCMessage message) {
		return messageConverters.get(message.getMethod());
	}

	public static OpenHABXBMCConverter getConverter(BindingTypes type) {
		return typeConverters.get(type);
	}

}
