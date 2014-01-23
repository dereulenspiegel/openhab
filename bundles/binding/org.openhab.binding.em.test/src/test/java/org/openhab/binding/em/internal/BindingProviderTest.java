package org.openhab.binding.em.internal;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.em.internal.EMBindingConfig.Datapoint;
import org.openhab.binding.em.internal.EMBindingConfig.EMType;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.NumberItem;
import org.openhab.model.item.binding.BindingConfigParseException;

public class BindingProviderTest {

	private EMGenericBindingProvider provider;

	@Before
	public void before() {
		provider = new EMGenericBindingProvider();
	}

	@Test
	public void testSuccessfullParsing() throws Exception {
		String itemConfig = "type=01;address=BC;datapoint=CUMULATED_VALUE;correctionFactor=400";
		Item emItem = new NumberItem("emItem");
		provider.processBindingConfiguration("", emItem, itemConfig);
		EMBindingConfig config = provider.getConfigByTypeAndAddressAndDatapoint(EMType.EM1000S, "BC",
				Datapoint.CUMULATED_VALUE);
		Assert.assertNotNull(config);
	}

	@Test(expected = BindingConfigParseException.class)
	public void testUnknownEMType() throws Exception {
		String itemConfig = "type=04|address=BC|datapoint=LAST_VALUE;correctionFactor=400";
		processBindingConfig(itemConfig);
	}
	
	@Test(expected=BindingConfigParseException.class)
	public void testAddressTooLong() throws Exception {
		String itemConfig = "type=04|address=BCA|datapoint=LAST_VALUE;correctionFactor=400";
		processBindingConfig(itemConfig);
	}

	private void processBindingConfig(String itemConfig) throws Exception {
		Item emItem = new NumberItem("emItem");
		provider.processBindingConfiguration("", emItem, itemConfig);
	}

}
