package org.openhab.binding.xbmc.internal.converter;

import org.openhab.binding.xbmc.internal.BindingTypes;
import org.openhab.binding.xbmc.internal.client.messages.XBMCMessage;
import org.openhab.binding.xbmc.internal.client.messages.XBMCParams;

public class PlayPauseStopConverter implements OpenHABXBMCConverter {

	@Override
	public BindingTypes convertMessage(XBMCMessage message) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public XBMCMessage convertBindingType(BindingTypes type) {
		XBMCMessage message = new XBMCMessage();
		String methodName = null;
		XBMCParams params = new XBMCParams();
		switch (type) {
		case PAUSE:
			methodName = "Player.OnPause";
			break;
		case PLAY:
			methodName = "Player.OnPlay";
			break;
		case STOP:
			methodName = "Player.OnStop";
		}
		message.setMethod(methodName);
		message.setJsonrpc("");
		message.setParams(params);
		return message;
	}

}
