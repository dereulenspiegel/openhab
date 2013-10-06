package org.openhab.binding.xbmc.internal.client.messages.data;

import org.openhab.binding.xbmc.internal.client.messages.Player;

public class PlayPauseStopData implements XBMCData {

	public static class Item {

		private String id;
		private String type;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
	}

	private Item item;
	private boolean end;
	private Player player;

	public Item getItem() {
		return item;
	}

	public void setItem(Item item) {
		this.item = item;
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

}
