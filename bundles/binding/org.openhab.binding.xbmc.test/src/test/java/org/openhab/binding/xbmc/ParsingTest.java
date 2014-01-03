package org.openhab.binding.xbmc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;
import org.openhab.binding.xbmc.internal.XBMCBindingConfig;
import org.openhab.binding.xbmc.internal.XBMCGenericBindingProvider;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.OnOffType;

public class ParsingTest {

	private final static String CONFIG_PATTERN = "^(.*?)\\((.+)\\)$";

	@Test
	public void testExpression() {
		String testConfig = "mediacenter(ON:Player.OnPlay,OFF:Player.OnPause)";
		Pattern pattern = Pattern.compile(CONFIG_PATTERN);
		Matcher matcher = pattern.matcher(testConfig);

		Assert.assertTrue(matcher.matches());
		matcher.reset();
		matcher.find();

		Assert.assertEquals("mediacenter", matcher.group(1));
		Assert.assertEquals("ON:Player.OnPlay,OFF:Player.OnPause", matcher.group(2));

	}

	@Test
	public void testParseBindingConfig() throws Exception {
		XBMCGenericBindingProvider provider = new XBMCGenericBindingProvider();
		String config1 = "mediacenter(ON:PLAY,OFF:PAUSE)";
		provider.processBindingConfiguration("xbmc", new SwitchItem("testSwitch"), config1);
		XBMCBindingConfig bindingConfig = provider.findBindingConfigByItemName("testSwitch");
		Assert.assertNotNull(bindingConfig);
		Assert.assertEquals(XBMCBindingCommands.PLAY, bindingConfig.getMethodNameForCommand(OnOffType.ON));
	}

}
