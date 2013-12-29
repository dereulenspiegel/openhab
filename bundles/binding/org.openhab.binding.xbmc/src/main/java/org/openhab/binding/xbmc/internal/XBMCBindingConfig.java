package org.openhab.binding.xbmc.internal;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.xbmc.XBMCBindingCommands;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class XBMCBindingConfig implements BindingConfig {

	private String deviceId;
	private Item item;
	private Map<Command, XBMCBindingCommands> commandMap = new HashMap<Command, XBMCBindingCommands>();
	private Map<XBMCBindingCommands, State> eventMap = new HashMap<XBMCBindingCommands, State>();

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

	public void addCommandAndCall(Command command, XBMCBindingCommands bindingCommand) {
		commandMap.put(command, bindingCommand);
	}

	public void addStateAndEvent(State state, XBMCBindingCommands bindingCommand) {
		eventMap.put(bindingCommand, state);
	}

	public State getStateForEvent(String event) {
		return eventMap.get(event);
	}

	public XBMCBindingCommands getMethodNameForCommand(Command command) {
		return commandMap.get(command);
	}

	public boolean hasBindingCommand(XBMCBindingCommands command) {
		if (commandMap.containsValue(command)) {
			return true;
		}
		if (eventMap.containsKey(command)) {
			return true;
		}
		return false;
	}

}
