package org.openhab.binding.xbmc.internal.client;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.xbmc.internal.client.messages.data.PlayPauseStopData;
import org.openhab.binding.xbmc.internal.client.messages.data.XBMCData;

public enum XBMCEventType {
	ON_PLAY("Player.OnPlay", PlayPauseStopData.class), ON_PAUSE("Player.OnPause", PlayPauseStopData.class), ON_STOP(
			"Player.OnStop", PlayPauseStopData.class);

	private String methodName;
	private Class<? extends XBMCData> dataClass;

	private static Map<String, XBMCEventType> eventMap = new HashMap<String, XBMCEventType>();
	static {
		for (XBMCEventType event : XBMCEventType.values()) {
			eventMap.put(event.getMethodName(), event);
		}
	}

	private XBMCEventType(String methodName, Class<? extends XBMCData> dataClass) {
		this.methodName = methodName;
		this.dataClass = dataClass;
	}

	public String getMethodName() {
		return methodName;
	}

	public static XBMCEventType getByMethodName(String methodName) {
		return eventMap.get(methodName);
	}

	public Class<? extends XBMCData> getDataClass() {
		return dataClass;
	}

}
