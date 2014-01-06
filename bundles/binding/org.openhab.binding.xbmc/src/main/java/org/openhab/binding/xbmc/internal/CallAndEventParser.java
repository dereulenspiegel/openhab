package org.openhab.binding.xbmc.internal;

import java.lang.reflect.Constructor;

import org.openhab.binding.xbmc.XBMCBindingCommands;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xbmc.android.jsonrpc.api.AbstractCall;
import org.xbmc.android.jsonrpc.api.model.GlobalModel.Toggle;

/**
 * This class is responsible for creating Calls which can be send to XBMC. Calls
 * are created based on a given binding command and a received openhab command.
 * The openhab command can be used as a parameter for the XBMC call.
 * 
 * @author Till Klocke
 * @since 1.4.0
 * 
 */
public class CallAndEventParser {

	private final static Logger logger = LoggerFactory.getLogger(CallAndEventParser.class);

	/**
	 * The id of the player we control with our calls. XBMC can have mutliple
	 * player at once, but so far player does always the trick.
	 */
	private final static Integer DEFAULT_PLAYER_ID = 1;

	/**
	 * Depending on the binding command we try to create a call which we can
	 * send to XBMC. The call can be parametrized with a received command. For
	 * example we can specify a DecimalType for the volume command to set the
	 * volume to the value specified by the DecimalType
	 * 
	 * @param bindingCommand
	 *            the command which was configured in the item
	 * @param command
	 *            optional openHAB command to parametrize the call. May be null
	 * @return a call object or null.
	 */
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
		case VOLUME:
			Integer volume = extractIntegerValueFromCommand(command);
			call = instantiateInstance(clazz, volume);
			break;
		default:
			call = null;
			break;
		}
		return call;
	}

	/**
	 * Since all numeric parameter in XBMC calls are Integer we try to get the
	 * integer value of the received command.
	 * 
	 * @param command
	 * @return
	 */
	private static Integer extractIntegerValueFromCommand(Command command) {
		Integer result = null;
		if (command instanceof DecimalType) {
			result = Integer.valueOf(((DecimalType) command).intValue());
		} else if (command instanceof PercentType) {
			result = Integer.valueOf(((PercentType) command).intValue());
		}
		return result;
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
