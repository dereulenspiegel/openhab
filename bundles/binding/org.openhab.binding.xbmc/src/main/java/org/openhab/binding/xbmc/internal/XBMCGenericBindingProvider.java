/**
 * openHAB, the open Home Automation Bus.
 * Copyright (C) 2010-2013, openHAB.org <admin@openhab.org>
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or
 * combining it with Eclipse (or a modified version of that library),
 * containing parts covered by the terms of the Eclipse Public License
 * (EPL), the licensors of this Program grant you additional permission
 * to convey the resulting work.
 */
package org.openhab.binding.xbmc.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.xbmc.XBMCBindingProvider;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author Till Klocke
 * @since 1.4.0
 */
public class XBMCGenericBindingProvider extends AbstractGenericBindingProvider implements XBMCBindingProvider {

	private final static Pattern CONFIG_PATTERN = Pattern.compile("^(.*?)\\((.+)\\)$");

	private Multimap<String, XBMCBindingConfig> bindingConfigs = HashMultimap.create();

	private final static List<Class<? extends Command>> allowedCommands = new ArrayList<Class<? extends Command>>();
	private final static List<Class<? extends State>> allowedStates = new ArrayList<Class<? extends State>>();

	static {
		allowedCommands.add(OnOffType.class);
		allowedCommands.add(DecimalType.class);

		allowedStates.add(OnOffType.class);
		allowedStates.add(DecimalType.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "xbmc";
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		// TODO To which items can we bind? Probably more than switches...
		if (!(item instanceof SwitchItem)) {
			throw new BindingConfigParseException("item '" + item.getName() + "' is of type '"
					+ item.getClass().getSimpleName()
					+ "', only SwitchItems are allowed - please check your *.items configuration");
		}
	}

	/**
	 * xbmc="[deviceId]([COMMAND/STATE]:[Method])"; {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig)
			throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		XBMCBindingConfig config = new XBMCBindingConfig();

		// parse bindingconfig here ...
		Matcher matcher = CONFIG_PATTERN.matcher(bindingConfig);
		if (!matcher.matches()) {
			throw new BindingConfigParseException("Config does not conform to the expected pattern");
		}
		matcher.reset();
		matcher.find();

		String deviceId = matcher.group(1);
		String commandsAndStates = matcher.group(2);
		Map<String, String> configMap = createStringMap(commandsAndStates);
		config.setDeviceId(deviceId);
		config.setItem(item);
		for (Entry<String, String> entry : configMap.entrySet()) {
			Command command = TypeParser.parseCommand(allowedCommands, entry.getKey());
			State state = TypeParser.parseState(allowedStates, entry.getKey());

			if (command != null) {
				// TODO Parse Call from MethodName
				config.addCommandAndCall(command, null);
			}
			if (state != null) {
				config.addStateAndEvent(state, entry.getValue());
			}
		}
		bindingConfigs.put(deviceId, config);
		addBindingConfig(item, config);
	}

	private Map<String, String> createStringMap(String commandsAndStates) {
		String[] parts = null;
		if (commandsAndStates.indexOf(',') > 0) {
			parts = commandsAndStates.split(",");
		} else {
			parts = new String[] { commandsAndStates };
		}
		Map<String, String> map = new HashMap<String, String>();
		for (String line : parts) {
			String[] lineParts = line.split(":");
			map.put(lineParts[0], lineParts[1]);
		}

		return map;
	}

	@Override
	public List<XBMCBindingConfig> findBindingConfigs(String deviceId, String methodName) {
		Collection<XBMCBindingConfig> configsForDevice = bindingConfigs.get(deviceId);
		List<XBMCBindingConfig> resultConfigs = new ArrayList<XBMCBindingConfig>();
		for (XBMCBindingConfig config : configsForDevice) {
			if (config.hasMethodName(methodName)) {
				resultConfigs.add(config);
			}
		}
		return resultConfigs;
	}

	@Override
	public XBMCBindingConfig findBindingConfigByItemName(String itemName) {
		for (XBMCBindingConfig config : bindingConfigs.values()) {
			if (config.getItem().getName().equals(itemName)) {
				return config;
			}
		}
		return null;
	}

}
