package org.openhab.binding.xbmc.internal.tcp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XBMCSocket {

	private final static Logger logger = LoggerFactory.getLogger(XBMCSocket.class);

	public static interface XBMCSocketListener {
		public void jsonReceived(String json);

		public void connected();

		public void disconnected();
	}

	private String xbmcHost;
	private int xbmcPort;

	private Socket xbmcSocket;

	private BufferedReader br;
	private BufferedWriter bw;

	private Set<XBMCSocketListener> listeners = new HashSet<XBMCSocketListener>();

	private ReadThread readThread;

	private boolean connected;

	public XBMCSocket(String host, int port) {
		this.xbmcHost = host;
		this.xbmcPort = port;
	}

	public void open() throws XBMCSocketException {
		try {
			xbmcSocket = new Socket();
			xbmcSocket.connect(new InetSocketAddress(xbmcHost, xbmcPort));
			InputStream is = xbmcSocket.getInputStream();
			OutputStream os = xbmcSocket.getOutputStream();
			bw = new BufferedWriter(new OutputStreamWriter(os));
			br = new BufferedReader(new InputStreamReader(is));
			connected = true;
			readThread = new ReadThread();
			readThread.start();
			notifyConnected();
		} catch (UnknownHostException e) {
			connected = false;
			notifyDisconnected();
			logger.error("Can't connect to XBMC host", e);
			throw new XBMCSocketException(e);
		} catch (IOException e) {
			connected = false;
			notifyDisconnected();
			logger.error("Can't connect to XBMC host", e);
			throw new XBMCSocketException(e);
		}

	}

	public void close() {
		connected = false;
		try {
			xbmcSocket.close();
		} catch (IOException e) {
			logger.warn("Error while closing socket", e);
		}
		xbmcSocket = null;
	}

	public void writeJsonString(String json) throws XBMCSocketException {
		try {
			bw.write(json);
			bw.flush();
		} catch (IOException e) {
			connected = false;
			notifyDisconnected();
			throw new XBMCSocketException(e);
		}
	}

	public void registerListener(XBMCSocketListener listener) {
		if (listener != null) {
			listeners.add(listener);
		}
	}

	public void unregisterListener(XBMCSocketListener listener) {
		if (listener != null) {
			listeners.remove(listener);
		}
	}

	private void notifyConnected() {
		for (XBMCSocketListener listener : listeners) {
			listener.connected();
		}
	}

	private void notifyDisconnected() {
		for (XBMCSocketListener listener : listeners) {
			listener.disconnected();
		}
	}

	private class ReadThread extends Thread {

		private int braceCounter = 0;

		private StringBuffer readBuffer = new StringBuffer();

		@Override
		public void run() {
			while (connected) {
				try {
					int readValue = (char) br.read();
					if (readValue > 0) {
						char readChar = (char) readValue;
						if (readChar == '{') {
							braceCounter++;
						}
						if (readChar == '}') {
							braceCounter--;
						}
						readBuffer.append(readChar);
						if (braceCounter == 0) {
							String readLine = readBuffer.toString();
							readBuffer = new StringBuffer();
							for (XBMCSocketListener listener : listeners) {
								listener.jsonReceived(readLine);
							}
						}
					} else {
						logger.warn("Didn't read anything from socket");
					}
				} catch (IOException e) {
					logger.error("Can't read from socket", e);
					connected = false;
					notifyDisconnected();
				}
			}
		}
	}

}
