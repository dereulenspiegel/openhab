package org.openhab.binding.xbmc;

import org.openhab.binding.xbmc.internal.tcp.XBMCSocket;
import org.openhab.binding.xbmc.internal.tcp.XBMCSocket.XBMCSocketListener;
import org.openhab.binding.xbmc.internal.tcp.XBMCSocketException;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Connectig to XBMX");
		XBMCSocket socket = new XBMCSocket("192.168.100.50", 9090);
		socket.registerListener(new XBMCSocketListener() {

			@Override
			public void jsonReceived(String json) {
				System.out.println("Received new JSON:");
				System.out.println(json);
				System.out
						.println("----------------------------------------------------------------------------------");

			}
		});
		try {
			socket.open();
			System.out.println("Connected to XBMC");
			while (true) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (XBMCSocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
