package org.openhab.binding.em.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.em.internal.EMBindingConfig.Datapoint;
import org.openhab.binding.em.internal.EMBindingConfig.EMType;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.io.transport.cul.mocks.MockCULHandler;
import org.openhab.io.transport.cul.mocks.MockCULManager;
import org.openhab.io.transport.cul.mocks.MockEventPublisher;
import org.openhab.io.transport.cul.mocks.MockEventPublisher.Tupel;

public class BindingTest {

	private EMBinding binding;
	private MockCULHandler culHandler;
	private MockCULManager culManager;
	private MockEventPublisher eventPublisher;
	private EMGenericBindingProvider provider;

	@Before
	public void setup() throws Exception {
		binding = new EMBinding();
		eventPublisher = new MockEventPublisher();
		culManager = new MockCULManager();
		provider = new EMGenericBindingProvider();

		binding.setCULManager(culManager);
		binding.setEventPublisher(eventPublisher);
		binding.addBindingProvider(provider);

		Dictionary<String, Object> config = new Hashtable<String, Object>();
		config.put("device", "serial:COM1");
		binding.updated(config);

		culHandler = culManager.getMockCULHandler();
		setupItems();
	}

	private void setupItems() throws Exception {
		String blankConfig = "type=%s;address=%s;datapoint=%s;correctionFactor=400";
		int addressValue = 16;
		String address = Integer.toHexString(addressValue);
		for (EMType type : EMType.values()) {
			addressValue += 1;
			address = Integer.toHexString(addressValue);
			for (Datapoint datapoint : Datapoint.values()) {
				String itemName = type.toString() + datapoint.toString();
				Item emItem = new NumberItem(itemName);
				String itemConfig = String.format(blankConfig, type.getTypeValue(), address, datapoint.toString());
				provider.processBindingConfiguration("", emItem, itemConfig);
			}
		}

	}

	@Test
	public void testReceiveValues() throws Exception {
		int addressValue = 16;
		String address = Integer.toHexString(addressValue);
		String cumulatedValue = "E803";
		String currentValue = "E903";
		String peakValue = "EA03";
		int packetCounter = 16;
		for (EMType type : EMType.values()) {
			eventPublisher.clear();
			addressValue += 1;
			address = Integer.toHexString(addressValue);
			for (Datapoint datapoint : Datapoint.values()) {
				packetCounter += 1;
				String counter = Integer.toHexString(packetCounter);
				String data = "E" + type.getTypeValue() + address + counter + cumulatedValue + currentValue + peakValue;
				culHandler.simulateReceivedCommand(data);
				EMBindingConfig bindingConfig = provider
						.getConfigByTypeAndAddressAndDatapoint(type, address, datapoint);
				Assert.assertNotNull(bindingConfig);
				Tupel tupel = eventPublisher.getLastPostedUpdate();
				Assert.assertNotNull(tupel);
				Assert.assertEquals(bindingConfig.getItem().getName(), tupel.itemName);
			}
		}
	}

}
