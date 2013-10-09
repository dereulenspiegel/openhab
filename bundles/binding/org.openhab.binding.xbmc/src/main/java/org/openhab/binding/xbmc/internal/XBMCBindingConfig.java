package org.openhab.binding.xbmc.internal;

import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.types.Command;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class XBMCBindingConfig implements BindingConfig {

	private String deviceId;
	private Item item;
	private Multimap<Command, BindingTypes> commandMap = HashMultimap.create();

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public Multimap<Command, BindingTypes> getCommandMap() {
		return commandMap;
	}

	public void setCommandMap(Multimap<Command, BindingTypes> commandMap) {
		this.commandMap = commandMap;
	}

	public void addCommandMapping(Command command, BindingTypes type) {
		commandMap.put(command, type);
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

}
