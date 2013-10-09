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
import org.openhab.binding.xbmc.internal.client.messages.data.XBMCData;
import org.openhab.binding.xbmc.internal.tcp.XBMCSocket;
import org.openhab.binding.xbmc.internal.tcp.XBMCSocket.XBMCSocketListener;
import org.openhab.binding.xbmc.internal.tcp.XBMCSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XBMCClient implements XBMCSocketListener {

	private final static Logger logger = LoggerFactory.getLogger(XBMCClient.class);

	public static interface XBMCClientListener {
		public void messageReceived(XBMCMessage message);
	}

	private XBMCSocket socket;

	private ObjectMapper objectMapper = new ObjectMapper();

	private Set<XBMCClientListener> listeners = new HashSet<XBMCClient.XBMCClientListener>();

	private boolean running = false;

	private String host;
	private int port;

	public XBMCClient() {

	}

	public void registerListener(XBMCClientListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void unregisterListener(XBMCClientListener listener) {
		if (listener != null) {
			listeners.remove(listener);
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

			for (XBMCClientListener listener : listeners) {
				listener.messageReceived(message);
			}
		} catch (JsonParseException e) {
			logger.error("Can't parse received json message: " + json, e);
		} catch (JsonMappingException e) {
			logger.error("Can't parse received json message: " + json, e);
		} catch (IOException e) {
			logger.error("Can't parse received json message: " + json, e);
		}
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
