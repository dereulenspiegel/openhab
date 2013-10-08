package org.openhab.binding.xbmc;

import org.openhab.binding.xbmc.internal.client.XBMCClient;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Connecting to XBMC");
		XBMCClient client = new XBMCClient();
	}
}
