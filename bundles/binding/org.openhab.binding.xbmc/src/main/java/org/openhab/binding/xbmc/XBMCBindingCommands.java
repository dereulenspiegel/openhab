package org.openhab.binding.xbmc;

import java.util.HashMap;
import java.util.Map;

import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.Application;
import org.xbmc.android.jsonrpc.api.call.Player;
import org.xbmc.android.jsonrpc.api.call.System;
import org.xbmc.android.jsonrpc.notification.PlayerEvent;

public enum XBMCBindingCommands {

	PLAY(Player.PlayPause.class, PlayerEvent.Play.METHOD), //
	PLAYING_MOVIE(null, null), //
	PLAYING_TVSHOW(null, null), //
	PLAYING_TITLE(null,null), //
	PAUSE(Player.PlayPause.class, PlayerEvent.Pause.METHOD), //
	STOP(Player.Stop.class, PlayerEvent.Stop.METHOD), //
	SHUTDOWN(System.Shutdown.class, null), //
	MUTE(Application.SetMute.class, null), //
	UNMUTE(Application.SetMute.class, null), //
	// Dirty fix to have an ebent method name when we query the volume of XBMC
	VOLUME(Application.SetVolume.class, Application.SetVolume.API_TYPE),
	VERSION(null,null),
	NAME(null,null);

	private final static Map<String, XBMCBindingCommands> methodMap = new HashMap<String, XBMCBindingCommands>();
	static {
		for (XBMCBindingCommands command : XBMCBindingCommands.values()) {
			if (command.getMethodName() != null) {
				methodMap.put(command.getMethodName(), command);
			}
		}
	}

	private Class<? extends AbstractCall<?>> clazz;
	private String eventMethodName;

	private XBMCBindingCommands(Class<? extends AbstractCall<?>> clazz, String eventMethodName) {
		this.clazz = clazz;
		this.eventMethodName = eventMethodName;
	}

	public String getMethodName() {
		return eventMethodName;
	}

	public Class<? extends AbstractCall<?>> getCallClazz() {
		return clazz;
	}

	public static XBMCBindingCommands getBindingCommandByMethodName(String methodName) {
		return methodMap.get(methodName);
	}

}
