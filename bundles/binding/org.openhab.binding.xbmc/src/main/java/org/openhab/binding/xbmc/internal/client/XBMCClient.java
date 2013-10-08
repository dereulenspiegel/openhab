package org.openhab.binding.xbmc.internal.client;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openhab.binding.xbmc.internal.client.messages.XBMCMessage;
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
		public void onPlay(XBMCEventType type, PlayPauseStopData data);

		public void onPause(XBMCEventType type, PlayPauseStopData data);

		public void onStop(XBMCEventType type, PlayPauseStopData data);
	}

	private XBMCSocket socket;

	private ObjectMapper objectMapper = new ObjectMapper();

	private Set<XBMCPlayListener> playListeners = new HashSet<XBMCClient.XBMCPlayListener>();

	private boolean running = false;

	private String host;
	private int port;

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

	public void open() throws XBMCSocketException {
		open(host, port);
	}

	public void close() {
		running = false;
		socket.close();
	}

	public void write(XBMCMessage message) {
		String jsonString;
		try {
			jsonString = objectMapper.writeValueAsString(message);
			socket.writeJsonString(jsonString);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XBMCSocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void jsonReceived(String json) {
		try {
			XBMCMessage message = objectMapper.readValue(json, XBMCMessage.class);
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
				listener.onPause(eventType, data);
				break;
			case ON_PLAY:
				listener.onPlay(eventType, data);
			case ON_STOP:
				listener.onStop(eventType, data);
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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
}
