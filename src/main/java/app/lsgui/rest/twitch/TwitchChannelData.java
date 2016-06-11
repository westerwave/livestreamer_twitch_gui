package app.lsgui.rest.twitch;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import app.lsgui.utils.JSONUtils;
import app.lsgui.utils.Utils;
import javafx.scene.image.Image;

/**
 *
 * @author Niklas 11.06.2016
 *
 */
public class TwitchChannelData {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitchChannelData.class);

    private static final ZoneOffset OFFSET = ZoneOffset.ofHours(0);
    private static final String PREFIX = "GMT"; // Greenwich Mean Time
    private static final ZoneId GMT = ZoneId.ofOffset(PREFIX, OFFSET);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'").withZone(GMT);
    private static final String STREAM = "stream";

    private boolean online = false;
    private boolean isPlaylist = false;
    private String name = "";
    private String title = "";
    private String createdAt = "";
    private String updatedAt = "";
    private String previewURL = "";
    private String logoURL = "";
    private String game = "";
    private long uptime = 0L;
    private int viewers = 0;
    private Image previewImage;
    private Image logoImage;
    private List<String> qualities;

    /**
     *
     * @param channelAPIResponse
     * @param name
     */
    public TwitchChannelData(final JsonObject channelAPIResponse, final String name) {
	if (channelAPIResponse.get(STREAM) != null && !channelAPIResponse.get(STREAM).isJsonNull()) {
	    JsonObject streamObject = channelAPIResponse.get(STREAM).getAsJsonObject();
	    if (streamObject != null && !streamObject.get("channel").isJsonNull() && !streamObject.isJsonNull()) {
		setData(streamObject, name);
	    }
	} else {
	    setData(null, name);
	}

    }

    private void setData(final JsonObject channelObject, final String name) {
	if (channelObject != null) {
	    setOnlineData(channelObject, name);
	} else {
	    setOfflineData(name);
	}
    }

    private void setOnlineData(final JsonObject channelObject, final String name) {
	JsonObject channel = channelObject.get("channel").getAsJsonObject();
	JsonObject preview = channelObject.get("preview").getAsJsonObject();
	setName(name);
	setTitle(JSONUtils.getStringIfNotNull("status", channel));
	setGame(JSONUtils.getStringIfNotNull("game", channelObject));
	setViewers(JSONUtils.getIntegerIfNotNull("viewers", channelObject));
	setPreviewURL(JSONUtils.getStringIfNotNull("large", preview));
	setCreatedAt(JSONUtils.getStringIfNotNull("created_at", channelObject));
	setUpdatedAt(JSONUtils.getStringIfNotNull("updated_at", channel));
	setLogoURL(JSONUtils.getStringIfNotNull("logo", channel));
	setOnline(true);
	setPlaylist(JSONUtils.getBooleanIfNotNull("is_playlist", channelObject));
	calculateAndSetUptime();
	setPreviewImage(new Image(getPreviewURL()));
	setLogoImage(null);
	setQualities(Utils.getAvailableQuality("http://twitch.tv/" + name));
    }

    private void setOfflineData(final String name) {
	setName(name);
	setTitle("");
	setGame("");
	setViewers(0);
	setPreviewURL("");
	setCreatedAt("");
	setUpdatedAt("");
	setLogoURL("");
	setOnline(false);
	setPlaylist(false);
	setQualities(new ArrayList<String>());
	setPreviewImage(null);
	setLogoImage(null);
    }

    private void calculateAndSetUptime() {
	try {
	    final ZonedDateTime nowDate = ZonedDateTime.now(GMT);
	    ZonedDateTime startDate = ZonedDateTime.parse(getCreatedAt(), DTF);
	    long time = startDate.until(nowDate, ChronoUnit.MILLIS);
	    setUptime(time);
	} catch (Exception e) {
	    LOGGER.error("ERROR while parsing date", e);
	    setUptime(0L);
	}
    }

    public boolean isOnline() {
	return online;
    }

    private void setOnline(boolean online) {
	this.online = online;
    }

    public String getName() {
	return name;
    }

    private void setName(String name) {
	this.name = name;
    }

    public String getTitle() {
	return title;
    }

    private void setTitle(String title) {
	this.title = title;
    }

    public int getViewers() {
	return viewers;
    }

    private void setViewers(int viewers) {
	this.viewers = viewers;
    }

    public String getCreatedAt() {
	return createdAt;
    }

    private void setCreatedAt(String createdAt) {
	this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
	return updatedAt;
    }

    private void setUpdatedAt(String updatedAt) {
	this.updatedAt = updatedAt;
    }

    public String getPreviewURL() {
	return previewURL;
    }

    private void setPreviewURL(String previewURL) {
	this.previewURL = previewURL;
    }

    public String getLogoURL() {
	return logoURL;
    }

    private void setLogoURL(String logoURL) {
	this.logoURL = logoURL;
    }

    public String getGame() {
	return game;
    }

    private void setGame(String game) {
	this.game = game;
    }

    public long getUptime() {
	return uptime;
    }

    private void setUptime(long uptime) {
	this.uptime = uptime;
    }

    public Image getLogoImage() {
	return logoImage;
    }

    private void setLogoImage(Image logoImage) {
	this.logoImage = logoImage;
    }

    public Image getPreviewImage() {
	return previewImage;
    }

    private void setPreviewImage(Image previewImage) {
	this.previewImage = previewImage;
    }

    public List<String> getQualities() {
	return qualities;
    }

    private void setQualities(List<String> qualities) {
	this.qualities = qualities;
    }

    /**
     * @return the isPlaylist
     */
    public boolean isPlaylist() {
	return isPlaylist;
    }

    /**
     * @param isPlaylist
     *            the isPlaylist to set
     */
    public void setPlaylist(boolean isPlaylist) {
	this.isPlaylist = isPlaylist;
    }

}