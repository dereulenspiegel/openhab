package org.openhab.binding.xbmc.internal.converter;

import org.openhab.binding.xbmc.internal.BindingTypes;
import org.openhab.binding.xbmc.internal.client.messages.XBMCMessage;

public interface OpenHABXBMCConverter {

	public BindingTypes convertMessage(XBMCMessage message);

	public XBMCMessage convertBindingType(BindingTypes type);

}
