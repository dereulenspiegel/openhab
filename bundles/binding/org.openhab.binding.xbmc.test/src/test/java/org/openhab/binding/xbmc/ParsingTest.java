package org.openhab.binding.xbmc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

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

}
