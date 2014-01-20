package org.openhab.binding.fs20.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.fs20.internal.FS20Binding;
import org.openhab.binding.fs20.internal.FS20Command;
import org.openhab.binding.fs20.internal.FS20GenericBindingProvider;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.io.transport.cul.mocks.MockCULHandler;
import org.openhab.io.transport.cul.mocks.MockCULManager;
import org.openhab.io.transport.cul.mocks.MockEventPublisher;
import org.openhab.io.transport.cul.mocks.MockEventPublisher.Tupel;

public class BindingTest {

	private MockEventPublisher eventPublisher;
	private MockCULManager culManager;

	private FS20Binding binding;

	@Before
	public void before() throws Exception {
		eventPublisher = new MockEventPublisher();
		culManager = new MockCULManager();
		binding = new FS20Binding();
		binding.addBindingProvider(setupBindingConfigProviderAndItems());
		binding.setEventPublisher(eventPublisher);
		binding.setCULManager(culManager);
		Dictionary<String, Object> dictionary = new Hashtable<String, Object>();
		dictionary.put("device", "serial:/dev/tty0");
		binding.updated(dictionary);
	}

	private FS20GenericBindingProvider setupBindingConfigProviderAndItems() throws Exception {
		FS20GenericBindingProvider provider = new FS20GenericBindingProvider();
		String switchAddress = "AABB01";
		String dimmerAddres = "CCDD02";

		Item switchItem = new SwitchItem("FS20Switch");
		Item dimmerItem = new DimmerItem("FS20Dimmer");

		provider.processBindingConfiguration("context", switchItem, switchAddress);
		provider.processBindingConfiguration("context", dimmerItem, dimmerAddres);
		return provider;
	}

	@Test
	public void testSwitchItem() throws Exception {
		binding.receiveCommand("FS20Switch", OnOffType.ON);
		MockCULHandler handler = culManager.getMockCULHandler();
		String command = handler.getSendCommands().get(0);
		Assert.assertEquals("FAABB01" + FS20Command.ON.getHexValue(), command);

		handler.getSendCommands().clear();

		binding.receiveCommand("FS20Switch", OnOffType.OFF);
		command = handler.getSendCommands().get(0);
		Assert.assertEquals("FAABB01" + FS20Command.OFF.getHexValue(), command);

		handler.simulateReceivedCommand("FAABB01" + FS20Command.ON.getHexValue());
		Tupel tupel = eventPublisher.getPostedUpdates().get(0);
		Assert.assertEquals("FS20Switch", tupel.itemName);
		Assert.assertEquals(OnOffType.ON, tupel.type);
	}

	@Test
	public void testUnsupportedCommand() throws Exception {
		binding.receiveCommand("FS20Switch", new DecimalType(42));
		MockCULHandler culHandler = culManager.getMockCULHandler();
		Assert.assertEquals(0, culHandler.getSendCommands().size());
	}

	@Test
	public void testDimmerItem() throws Exception {
		MockCULHandler culHandler = culManager.getMockCULHandler();
		binding.receiveCommand("FS20Dimmer", OnOffType.OFF);
		String sendCommand = culHandler.getSendCommands().get(0);
		Assert.assertEquals("FCCDD02" + FS20Command.OFF.getHexValue(), sendCommand);
		culHandler.getSendCommands().clear();

		binding.receiveCommand("FS20Dimmer", new PercentType(12));
		sendCommand = culHandler.getSendCommands().get(0);
		Assert.assertEquals("FCCDD02" + FS20Command.DIM_1.getHexValue(), sendCommand);

		culHandler.simulateReceivedCommand("FCCDD02" + FS20Command.DIM_10.getHexValue());
		Tupel tupel = eventPublisher.getPostedUpdates().get(0);
		Assert.assertEquals("FS20Dimmer", tupel.itemName);
		Assert.assertEquals(new PercentType("62.5"), tupel.type);
	}

}
