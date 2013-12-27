package org.openhab.binding.xbmc.internal;

import java.util.HashMap;
import java.util.Map;

import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.xbmc.android.jsonrpc.api.AbstractCall;

public class XBMCBindingConfig implements BindingConfig {

	private String deviceId;
	private Item item;
	private Map<Command, String> commandMap = new HashMap<Command, String>();
	private Map<String, State> eventMap = new HashMap<String, State>();

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public void addCommandAndCall(Command command, String methodName) {
		commandMap.put(command, methodName);
	}

	public void addStateAndEvent(State state, String event) {
		eventMap.put(event, state);
	}

	public State getStateForEvent(String event) {
		return eventMap.get(event);
	}

	public String getMethodNameForCommand(Command command) {
		return commandMap.get(command);
	}

	public boolean hasMethodName(String methodName) {
		if (commandMap.containsValue(methodName)) {
			return true;
		}
		if (eventMap.containsKey(methodName)) {
			return true;
		}
		return false;
	}

}
