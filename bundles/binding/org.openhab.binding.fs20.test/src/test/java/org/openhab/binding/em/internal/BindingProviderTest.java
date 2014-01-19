package org.openhab.binding.em.internal;

import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.fs20.FS20BindingConfig;
import org.openhab.binding.fs20.internal.FS20GenericBindingProvider;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.BindingConfigParseException;

public class BindingProviderTest {

	@Test
	public void testParsingSuccess() throws Exception {
		FS20GenericBindingProvider provider = new FS20GenericBindingProvider();
		String switchItemName = "fs20Switch";
		String switchItemConfig = "CCAA00";
		Item switchItem = new SwitchItem(switchItemName);
		provider.processBindingConfiguration("contxt", switchItem, switchItemConfig);

		FS20BindingConfig config = provider.getConfigForAddress(switchItemConfig);
		Assert.assertNotNull(config);
		Assert.assertEquals(switchItem, config.getItem());
		Assert.assertEquals(switchItemConfig, config.getAddress());
	}

	@Test(expected = BindingConfigParseException.class)
	public void testParseTooLongAddress() throws Exception {
		parseAddress("CCAA0012");
		
	}
	
	@Test(expected=BindingConfigParseException.class)
	public void testParseEmptyAddress() throws Exception {
		parseAddress("");
	}
	
	@Test(expected=BindingConfigParseException.class)
	public void testParseNullAddress() throws Exception {
		parseAddress(null);
	}
	
	@Test(expected=BindingConfigParseException.class)
	public void testParseTooShortAddress() throws Exception {
		parseAddress("CCAA0");
	}
	
	private void parseAddress(String address) throws Exception {
		FS20GenericBindingProvider provider = new FS20GenericBindingProvider();
		String switchItemName = "fs20Switch";
		Item switchItem = new SwitchItem(switchItemName);
		provider.processBindingConfiguration("contxt", switchItem, address);
	}

}
