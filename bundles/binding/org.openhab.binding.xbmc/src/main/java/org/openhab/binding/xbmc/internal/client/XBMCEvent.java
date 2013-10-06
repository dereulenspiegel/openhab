package org.openhab.binding.xbmc.internal.client;

import org.openhab.binding.xbmc.internal.client.messages.data.XBMCData;

public class XBMCEvent<D extends XBMCData> {

	private XBMCEventType eventType;
	private D data;

	public XBMCEvent(XBMCEventType eventType, D data) {
		this.eventType = eventType;
		this.data = data;
	}

	public XBMCEventType getEventType() {
		return eventType;
	}

	public void setEventType(XBMCEventType eventType) {
		this.eventType = eventType;
	}

	public D getData() {
		return data;
	}

	public void setData(D data) {
		this.data = data;
	}

}
