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
			packetCounter += 1;
			String counter = Integer.toHexString(packetCounter);
			String data = "E" + type.getTypeValue() + address + counter + cumulatedValue + currentValue + peakValue;
			culHandler.simulateReceivedCommand(data);
			for (Datapoint datapoint : Datapoint.values()) {
				EMBindingConfig bindingConfig = provider
						.getConfigByTypeAndAddressAndDatapoint(type, address, datapoint);
				Assert.assertNotNull(bindingConfig);
				String expectedValue = null;
				switch (datapoint) {
				case CUMULATED_VALUE:
					expectedValue = "03E8";
					break;
				case LAST_VALUE:
					expectedValue = "03E9";
					break;
				case TOP_VALUE:
					expectedValue = "03EA";
					break;
				}
				doesEventPublisherContainsUpdate(bindingConfig.getItem().getName(), expectedValue);
			}
		}
	}

	private void doesEventPublisherContainsUpdate(String itemName, String expectedValue) {
		int intValue = Integer.parseInt(expectedValue, 16) * 400;
		for (Tupel t : eventPublisher.getPostedUpdates()) {
			if (t.itemName.equals(itemName)) {
				DecimalType state = (DecimalType) t.type;
				if (state.compareTo(new DecimalType(intValue)) == 0) {
					return;
				} else {
					Assert.fail("Received value doesn't match expected value, item: " + itemName + " expected value "
							+ intValue);
				}
			}
		}
		Assert.fail();
	}

}
