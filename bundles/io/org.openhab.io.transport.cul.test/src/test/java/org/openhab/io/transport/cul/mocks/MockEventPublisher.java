package org.openhab.io.transport.cul.mocks;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.spec.PSource;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;

public class MockEventPublisher implements EventPublisher {

	public static class Tupel {
		public Type type;
		public String itemName;

		public Tupel(String itemName, Type type) {
			this.itemName = itemName;
			this.type = type;
		}
	}

	private List<Tupel> sendCommands = new ArrayList<MockEventPublisher.Tupel>();
	private List<Tupel> postedCommands = new ArrayList<MockEventPublisher.Tupel>();
	private List<Tupel> postedUpdates = new ArrayList<MockEventPublisher.Tupel>();

	@Override
	public void sendCommand(String itemName, Command command) {
		sendCommands.add(new Tupel(itemName, command));

	}

	@Override
	public void postCommand(String itemName, Command command) {
		postedCommands.add(new Tupel(itemName, command));

	}

	@Override
	public void postUpdate(String itemName, State newState) {
		postedUpdates.add(new Tupel(itemName, newState));
	}

	public void clear() {
		sendCommands.clear();
		postedCommands.clear();
		postedUpdates.clear();
	}

	public List<Tupel> getSendCommands() {
		return sendCommands;
	}

	public Tupel getLastSendCommand() {
		if (sendCommands.size() > 0) {
			return sendCommands.get(sendCommands.size() - 1);
		}
		return null;
	}

	public List<Tupel> getPostedCommands() {
		return postedCommands;
	}

	public Tupel getLastPostedCommand() {
		if (postedCommands.size() > 0) {
			return postedCommands.get(postedCommands.size() - 1);
		}
		return null;
	}

	public List<Tupel> getPostedUpdates() {
		return postedUpdates;
	}

	public Tupel getLastPostedUpdate() {
		if (postedUpdates.size() > 0) {
			return postedUpdates.get(postedUpdates.size() - 1);
		}
		return null;
	}

}
