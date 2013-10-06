package org.openhab.binding.xbmc;

import org.openhab.binding.xbmc.internal.client.XBMCClient;
import org.openhab.binding.xbmc.internal.client.XBMCClient.XBMCListener;
import org.openhab.binding.xbmc.internal.client.messages.data.PlayPauseStopData;
import org.openhab.binding.xbmc.internal.tcp.XBMCSocketException;

public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Connecting to XBMC");
		XBMCClient client = new XBMCClient();
		try {
			client.registerPlayListener(new XBMCListener() {

				@Override
				public void onStop(PlayPauseStopData playPauseStopData) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onPlay(PlayPauseStopData playPauseData) {
					System.out.println("XBMC is playing something");
					System.out.println("XBMC is playing a " + playPauseData.getItem().getType());

				}

				@Override
				public void onPause(PlayPauseStopData playPauseData) {
					// TODO Auto-generated method stub

				}
			});
			client.open("192.168.100.50", 9090);
			System.out.println("Successfully connected to XBMC");
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
