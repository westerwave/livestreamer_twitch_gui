package app.lsgui.serviceapi.twitch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;

public class TwitchManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TwitchManager.class);
	private static TwitchManager instance = null;
	private static final JsonParser JSONPARSER = new JsonParser();

	private TwitchManager() {
		LOGGER.debug("TwitchManager constructed");
	}

	public static TwitchManager instance() {
		if (instance == null) {
			instance = new TwitchManager();
		}
		return instance;
	}

	public TwitchStreamData getStreamData(final String streamName) {
		// TODO
		return null;
	}

	public TwitchGameData getGameData() {
		// TODO
		return null;
	}

	public TwitchChannelsData getChannelData(final String gameName) {
		// TODO
		return null;
	}
}