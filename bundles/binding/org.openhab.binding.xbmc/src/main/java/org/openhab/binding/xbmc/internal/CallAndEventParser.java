package org.openhab.binding.xbmc.internal;

import java.util.HashMap;
import java.util.Map;

import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.Application;
import org.xbmc.android.jsonrpc.api.call.Player;
import org.xbmc.android.jsonrpc.api.call.System;
import org.xbmc.android.jsonrpc.api.model.GlobalModel.Toggle;

public class CallAndEventParser {

	private final static Map<String, AbstractCall<?>> methodToCallMap = new HashMap<String, AbstractCall<?>>();
	static {
		methodToCallMap.put(Application.Quit.API_TYPE, new Application.Quit());
		// TODO: Add parameters to calls
		methodToCallMap.put(Application.SetMute.API_TYPE, new Application.SetMute(new Toggle(true)));
		methodToCallMap.put(Player.PlayPause.API_TYPE, new Player.PlayPause(0));
		methodToCallMap.put(Player.Stop.API_TYPE, new Player.Stop(0));
		methodToCallMap.put(System.Shutdown.API_TYPE, new System.Shutdown());
	}

	public static AbstractCall<?> getCallFromString(String in) {
		return methodToCallMap.get(in);
	}

}
