package org.openhab.binding.xbmc.internal;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.xbmc.XBMCBindingCommands;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.call.Application;
import org.xbmc.android.jsonrpc.api.call.Player;
import org.xbmc.android.jsonrpc.api.call.System;
import org.xbmc.android.jsonrpc.api.model.AddonModel.Detail.Extrainfo;
import org.xbmc.android.jsonrpc.api.model.GlobalModel.Toggle;

public class CallAndEventParser {

	private final static Logger logger = LoggerFactory.getLogger(CallAndEventParser.class);

	private final static Map<String, Class<? extends AbstractCall<?>>> methodToCallMap = new HashMap<String, Class<? extends AbstractCall<?>>>();
	static {
		methodToCallMap.put(Application.Quit.API_TYPE, Application.Quit.class);
		// TODO: Add parameters to calls
		methodToCallMap.put(Application.SetMute.API_TYPE, Application.SetMute.class);
		methodToCallMap.put(Player.PlayPause.API_TYPE, Player.PlayPause.class);
		methodToCallMap.put(Player.Stop.API_TYPE, Player.Stop.class);
		methodToCallMap.put(System.Shutdown.API_TYPE, System.Shutdown.class);
	}

	private final static Integer DEFAULT_PLAYER_ID = 0;

	public static AbstractCall<?> getCallForBindingCommandAndCommand(XBMCBindingCommands bindingCommand, Command command) {
		Class<? extends AbstractCall<?>> clazz = bindingCommand.getCallClazz();
		AbstractCall<?> call = null;
		switch (bindingCommand) {
		case PLAY:
			call = instantiateInstance(clazz, DEFAULT_PLAYER_ID, new Toggle(Boolean.TRUE));
			break;
		case PAUSE:
			call = instantiateInstance(clazz, DEFAULT_PLAYER_ID, new Toggle(Boolean.FALSE));
			break;
		case STOP:
			call = instantiateInstance(clazz, DEFAULT_PLAYER_ID);
			break;
		case SHUTDOWN:
			call = instantiateInstance(clazz, new Object[] {});
			break;
		case MUTE:
			call = instantiateInstance(clazz, new Toggle(Boolean.TRUE));
			break;
		case UNMUTE:
			call = instantiateInstance(clazz, new Toggle(Boolean.FALSE));
			break;
		case SETVOLUME:
			Integer volume = extractIntegerValueFromCommand(command);
			call = instantiateInstance(clazz, volume);
			break;
		}
		return call;
	}

	private static Integer extractIntegerValueFromCommand(Command command) {
		Integer result = null;
		if (command instanceof DecimalType) {
			result = Integer.valueOf(((DecimalType) command).intValue());
		} else if (command instanceof PercentType) {
			result = Integer.valueOf(((PercentType) command).intValue());
		}
		return result;
	}

	public static AbstractCall<?> getCallForMethodAndCommand(String methodName, Command command) {
		Object argument = null;
		if (command instanceof DecimalType) {
			argument = Integer.valueOf(((DecimalType) command).intValue());
		} else if (command instanceof PercentType) {
			argument = Integer.valueOf(((PercentType) command).intValue());
		}
		return getCallFromString(methodName, argument);
	}

	public static AbstractCall<?> getCallFromString(String in) {
		return getCallFromString(in, null);
	}

	public static AbstractCall<?> getCallFromString(String methodName, Object argument) {
		Class<? extends AbstractCall<?>> clazz = methodToCallMap.get(methodName);
		return instantiateInstance(clazz, argument);
	}

	private static AbstractCall<?> instantiateInstance(Class<? extends AbstractCall<?>> clazz, Object... arguments) {
		if (clazz == null) {
			logger.error("No class given as argument");
			return null;
		}
		// TODO allow support for multiple arguments
		Class<?>[] parameterTypes = {};
		Object[] initargs = {};
		if (arguments != null) {
			parameterTypes = new Class<?>[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				parameterTypes[i] = arguments[i].getClass();
			}
			initargs = arguments;
		}

		try {
			AbstractCall<?> call = instantiateWith(clazz, parameterTypes, initargs);
			if (call != null) {
				return call;
			}
		} catch (Exception e) {
			logger.error(
					"Can't instantiate Call of class " + clazz.getCanonicalName() + " with arguments " + arguments, e);
		}

		return null;
	}

	private static AbstractCall<?> instantiateWith(Class<? extends AbstractCall<?>> clazz, Class<?>[] parameterTypes,
			Object[] initargs) {
		Constructor<? extends AbstractCall<?>> constructor;
		try {
			constructor = clazz.getConstructor(parameterTypes);
			AbstractCall<?> call = constructor.newInstance(initargs);
			return call;
		} catch (Exception e) {
			logger.debug("Can't instantiate class " + clazz.getCanonicalName());
		}

		return null;
	}
}
