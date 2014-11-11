package org.openhab.binding.chromecast.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openhab.binding.chromecast.ChromecastBindingProvider;
import org.openhab.binding.chromecast.internal.ChromecastGenericBindingProvider.ChromecastBindingConfig;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import su.litvak.chromecast.api.v2.ChromeCast;
import su.litvak.chromecast.api.v2.ChromeCasts;
import su.litvak.chromecast.api.v2.MediaStatus;
import su.litvak.chromecast.api.v2.MediaStatus.PlayerState;
import su.litvak.chromecast.api.v2.Status;

public class ChromecastHandler {

	private final static Logger logger = LoggerFactory
			.getLogger(ChromecastHandler.class);

	private Map<String, ChromeCast> deviceMap = new ConcurrentHashMap<String, ChromeCast>();

	private EventPublisher eventPublisher;
	private ChromecastBindingProvider bindingProvider;

	public ChromecastHandler(EventPublisher eventPublisher,
			ChromecastBindingProvider bindingProvider) {
		this.eventPublisher = eventPublisher;
		this.bindingProvider = bindingProvider;
	}

	public void startDiscovery() {
		try {
			ChromeCasts.startDiscovery();
		} catch (IOException e) {
			logger.error("Can't start discovery of Chromecast devices", e);
			return;
		}
	}

	public void update() {

		Iterator<ChromeCast> deviceIterator = ChromeCasts.get().iterator();
		while (deviceIterator.hasNext()) {
			ChromeCast device = deviceIterator.next();
			if (!deviceMap.containsKey(device.getName())) {
				logger.debug("Discovered new Chromecast with the name "
						+ device.getName());
				deviceMap.put(device.getName(), device);
				updateStatusItems(device);
			} else {
				logger.debug("Updating status of already discovered device");
				updateStatusItems(device);
			}
		}

	}

	protected void updateStatusItems(ChromeCast device) {
		List<ChromecastBindingConfig> bindingConfigs = bindingProvider
				.getBindingConfigsFor(device.getName());
		if (bindingConfigs.size() == 0) {
			return;
		}
		try {
			MediaStatus mediaStatus = device.getMediaStatus();
			MediaStatus.PlayerState playerState = mediaStatus.playerState;
			Status deviceStatus = device.getStatus();

			for (ChromecastBindingConfig config : bindingConfigs) {
				State update = null;
				switch (config.property) {
				case PLAY:
					update = (playerState == PlayerState.PLAYING) ? OnOffType.ON
							: OnOffType.OFF;
					break;

				case PAUSE:
					update = (playerState == PlayerState.PAUSED) ? OnOffType.ON
							: OnOffType.OFF;
					break;

				case IDLE:
					update = (playerState == PlayerState.IDLE) ? OnOffType.ON
							: OnOffType.OFF;
					break;

				case BUFFERING:
					update = (playerState == PlayerState.BUFFERING) ? OnOffType.ON
							: OnOffType.OFF;
					break;

				case VOLUME:
					// TODO Determine how volume is formatted
					float volume = deviceStatus.volume.level;
					update = new DecimalType(volume * 100);
					break;

				case APP_ID:
					update = new StringType(deviceStatus.getRunningApp().id);
					break;

				case APP_NAME:
					update = new StringType(deviceStatus.getRunningApp().name);
					break;

				case DURATION:
					// Check if this is really curerent time in percent
					float percent = mediaStatus.currentTime;
					update = new PercentType(new BigDecimal(percent));
					break;

				case STREAM_TYPE:
					update = new StringType(mediaStatus.media.streamType);
					break;

				case CONTENT_TYPE:
					update = new StringType(mediaStatus.media.contentType);
					break;

				case URL:
					update = new StringType(mediaStatus.media.url);
					break;

				case IS_ACTIVE_INPUT:
					update = deviceStatus.activeInput ? OnOffType.ON
							: OnOffType.OFF;

				default:
					update = null;
					logger.warn("Can't find proper State for item "
							+ config.item.getName());

				}
				if (update != null) {
					eventPublisher.postUpdate(config.item.getName(), update);
				}
			}
		} catch (IOException e) {
			logger.error(
					"Error retrieving state from ChromeCast "
							+ device.getName(), e);
		}

	}

	public ChromeCast getDeviceByName(String name) {
		return deviceMap.get(name);
	}

}
