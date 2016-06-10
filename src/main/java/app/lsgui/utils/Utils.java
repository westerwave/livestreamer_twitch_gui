package app.lsgui.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import app.lsgui.gui.MainWindow;
import app.lsgui.gui.chat.ChatWindow;
import app.lsgui.model.channel.IChannel;
import app.lsgui.model.channel.twitch.TwitchChannel;
import app.lsgui.model.service.GenericService;
import app.lsgui.model.service.IService;
import app.lsgui.model.service.TwitchService;
import app.lsgui.rest.twitch.TwitchAPIClient;
import app.lsgui.settings.Settings;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class Utils {

    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private Utils() {
    }

    public static void openURLInBrowser(final String url) {
        LOGGER.info("Open Browser URL {}", url);
        try {
            URI uri = new URI(url);
            Desktop.getDesktop().browse(uri);
        } catch (IOException | URISyntaxException e) {
            LOGGER.error("ERROR while opening URL in Browser", e);
        }
    }

    public static List<String> getAvailableQuality(final String url) {
        final List<String> qualities = new ArrayList<>();
        final JsonObject qualitiesJson = LivestreamerUtils.getQualityJsonFromLivestreamer(url);
        if (!qualitiesJson.toString().contains("error")) {
            final JsonObject jsonQualitiyList = qualitiesJson.get("streams").getAsJsonObject();
            jsonQualitiyList.entrySet().forEach(entry -> qualities.add(entry.getKey()));
            return sortQualities(qualities);
        }
        return qualities;
    }

    private static List<String> sortQualities(final List<String> qualities) {
        final List<String> sortedQualities = new ArrayList<>();
        qualities.forEach(s -> s = s.toLowerCase());
        if (qualities.contains("audio")) {
            sortedQualities.add("Audio");
        }
        if (qualities.contains("mobile")) {
            sortedQualities.add("Mobile");
        }
        if (qualities.contains("low")) {
            sortedQualities.add("Low");
        }
        if (qualities.contains("medium")) {
            sortedQualities.add("Medium");
        }
        if (qualities.contains("high")) {
            sortedQualities.add("High");
        }
        if (qualities.contains("source")) {
            sortedQualities.add("Source");
        }
        if (sortedQualities.isEmpty()) {
            sortedQualities.add("Worst");
            sortedQualities.add("Best");
        }
        return sortedQualities;
    }

    public static String getColorFromString(final String input) {
        int r = 0;
        int g = 0;
        int b = 0;
        if (!"".equals(input)) {
            int hash = input.hashCode();
            r = (hash & 0xFF0000) >> 16;
            if (r > 200) {
                r = 200;
            }
            g = (hash & 0x00FF00) >> 8;
            if (g > 200) {
                g = 200;
            }
            b = hash & 0x0000FF;
            if (b > 200) {
                b = 200;
            }
        }
        return "rgb(" + r + "," + g + "," + b + ")";
    }

    public static void addStyleSheetToStage(final Stage stage, final String style) {
        if (stage != null && !stage.getScene().getStylesheets().contains(style) && !"".equals(style)) {
            stage.getScene().getStylesheets().add(style);
        }
    }

    public static void clearStyleSheetsFromStage(final Stage stage) {
        if (stage != null) {
            stage.getScene().getStylesheets().clear();
        }
    }

    public static boolean isTwitchChannel(final IChannel channel) {
        return channel.getClass().equals(TwitchChannel.class);
    }

    public static boolean isTwitchService(final IService service) {
        return service.getClass().equals(TwitchService.class);
    }

    public static void addChannelToService(final String channel, final IService service) {
        if (isTwitchService(service) && !"".equals(channel)) {
            if (TwitchAPIClient.instance().channelExists(channel)) {
                service.addChannel(channel);
            }
        } else {
            service.addChannel(channel);
        }
    }

    public static void addFollowedChannelsToService(final String username, final TwitchService service) {
        if (!"".equals(username)) {
            service.addFollowedChannels(username);
        }
    }

    public static void removeChannelFromService(final IChannel channel, final IService service) {
        service.removeChannel(channel);
    }

    public static void addService(final String serviceName, final String serviceUrl) {
        LOGGER.debug("Add new Service {} with URL {}", serviceName, serviceUrl);
        if (!"".equals(serviceName) && !"".equals(serviceUrl)) {
            String correctedUrl = correctUrl(serviceUrl);
            Settings.instance().getStreamServices().add(new GenericService(serviceName, correctedUrl));
        }
    }

    private static String correctUrl(final String url) {
        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }

    public static String buildUrl(final String serviceUrl, final String channelUrl) {
        return serviceUrl + channelUrl;
    }

    public static boolean isChannelOnline(final IChannel channel) {
        if (channel != null) {
            if (Utils.isTwitchChannel(channel)) {
                return channel.isOnline().get();
            } else {
                return true;
            }
        }
        return false;
    }

    public static void recordStream(final IService service, final IChannel channel) {
        if (Utils.isChannelOnline(channel)) {
            final String url = buildUrl(service.getUrl().get(), channel.getName().get());
            final String quality = Settings.instance().getQuality();

            final FileChooser recordFileChooser = new FileChooser();
            recordFileChooser.setTitle("Choose Target file");
            recordFileChooser.getExtensionFilters().add(new ExtensionFilter("MPEG4", ".mpeg4"));
            final File recordFile = recordFileChooser.showSaveDialog(MainWindow.getRootStage());
            if (recordFile != null) {
                LivestreamerUtils.recordLivestreamer(url, quality, recordFile);
            }
        }
    }

    public static void openTwitchChat(final IChannel channel) {
        if (Utils.isChannelOnline(channel) && Utils.isTwitchChannel(channel)) {
            final String channelName = channel.getName().get();
            ChatWindow cw = new ChatWindow(channelName);
            cw.connect();
        }
    }

    public static void removeService(final IService service) {
        LOGGER.debug("Removing Service {}", service.getName().get());
        Settings.instance().getStreamServices().remove(service);
    }
}
