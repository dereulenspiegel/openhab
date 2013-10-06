package org.openhab.binding.xbmc.internal.client;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openhab.binding.xbmc.internal.client.messages.ReceivedMessage;
import org.openhab.binding.xbmc.internal.client.messages.XBMCParams;
import org.openhab.binding.xbmc.internal.client.messages.data.PlayPauseStopData;
import org.openhab.binding.xbmc.internal.client.messages.data.XBMCData;
import org.openhab.binding.xbmc.internal.tcp.XBMCSocket;
import org.openhab.binding.xbmc.internal.tcp.XBMCSocket.XBMCSocketListener;
import org.openhab.binding.xbmc.internal.tcp.XBMCSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XBMCClient implements XBMCSocketListener {

	private final static Logger logger = LoggerFactory.getLogger(XBMCClient.class);

	public static interface XBMCPlayListener {
		public void onPlay(PlayPauseStopData data);

		public void onPause(PlayPauseStopData data);

		public void onStop(PlayPauseStopData data);
	}

	private XBMCSocket socket;

	private ObjectMapper objectMapper = new ObjectMapper();

	private Set<XBMCPlayListener> playListeners = new HashSet<XBMCClient.XBMCPlayListener>();

	private boolean running = false;

	public XBMCClient() {

	}

	public void registerPlayListener(XBMCPlayListener listener) {
		if (listener != null) {
			playListeners.add(listener);
		}
	}

	public void unregisterPlayListener(XBMCPlayListener listener) {
		if (listener != null) {
			playListeners.remove(listener);
		}
	}

	public void open(String host, int port) throws XBMCSocketException {
		running = true;
		socket = new XBMCSocket(host, port);
		socket.registerListener(this);
		socket.open();
	}

	public void close() {
		running = false;
		socket.close();
	}

	@Override
	public void jsonReceived(String json) {
		try {
			ReceivedMessage message = objectMapper.readValue(json, ReceivedMessage.class);
			XBMCEventType event = XBMCEventType.getByMethodName(message.getMethod());
			switch (event) {
			case ON_PAUSE:
			case ON_PLAY:
			case ON_STOP:
				handlePlayPauseStop(event, message.getParams());
				break;
			}
		} catch (JsonParseException e) {
			logger.error("Can't parse received json message: " + json, e);
		} catch (JsonMappingException e) {
			logger.error("Can't parse received json message: " + json, e);
		} catch (IOException e) {
			logger.error("Can't parse received json message: " + json, e);
		}
	}

	private void handlePlayPauseStop(XBMCEventType eventType, XBMCParams params) {
		PlayPauseStopData data = (PlayPauseStopData) parseData(params, eventType.getDataClass(), eventType);
		for (XBMCPlayListener listener : playListeners) {
			switch (eventType) {
			case ON_PAUSE:
				listener.onPause(data);
				break;
			case ON_PLAY:
				listener.onPlay(data);
			case ON_STOP:
				listener.onStop(data);
			}
		}
	}

	private <D extends XBMCData> D parseData(XBMCParams params, Class<D> clazz, XBMCEventType eventType) {
		String jsonString = params.getData().toString();
		try {
			D data = objectMapper.readValue(jsonString, clazz);
			return data;
		} catch (JsonParseException e) {
			logger.error("Can't parse Data: " + jsonString, e);
		} catch (JsonMappingException e) {
			logger.error("Can't parse Data: " + jsonString, e);
		} catch (IOException e) {
			logger.error("Can't parse Data: " + jsonString, e);
		}
		return null;
	}

	@Override
	public void connected() {
		// Ignore

	}

	@Override
	public void disconnected() {
		if (running) {
			try {
				Thread.sleep(2000);
				socket.open();
			} catch (XBMCSocketException e) {
				logger.error("Exception while reconnecting", e);
			} catch (InterruptedException e) {
				logger.error("Exception while waiting for disconnect", e);
			}
		}

	}
}
