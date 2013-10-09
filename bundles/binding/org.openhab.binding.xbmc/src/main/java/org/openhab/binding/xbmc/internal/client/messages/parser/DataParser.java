package org.openhab.binding.xbmc.internal.client.messages.parser;

import org.openhab.binding.xbmc.internal.client.messages.XBMCMessage;
import org.openhab.binding.xbmc.internal.client.messages.data.XBMCData;

public interface DataParser<D extends XBMCData> {

	public D parseXBMCData(XBMCMessage message);

}
