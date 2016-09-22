/**
 * MIT License
 *
 * Copyright (c) 2016 Jan-Niklas Keck
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package app.lsgui.remote.twitch;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import app.lsgui.model.twitch.TwitchChannel;
import app.lsgui.model.twitch.TwitchChannels;
import app.lsgui.model.twitch.TwitchGames;
import app.lsgui.utils.Settings;

/**
 *
 * @author Niklas 11.06.2016
 *
 */
public final class TwitchAPIClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitchAPIClient.class);
    private static final JsonParser JSONPARSER = new JsonParser();
    private static final String TWITCH_BASE_URL = "https://api.twitch.tv/kraken/";
    private static final String LSGUI_CLIENT_ID = "rfpepzumaxd1iija3ip3fixao6z13pj";
    private static final int CONNECTION_COUNT = 100;
    private static final CloseableHttpClient HTTP_CLIENT;

    private static TwitchAPIClient instance;

    static {
        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(CONNECTION_COUNT);
        cm.setDefaultMaxPerRoute(CONNECTION_COUNT);
        HTTP_CLIENT = HttpClients.createMinimal(cm);
    }

    private TwitchAPIClient() {
        LOGGER.debug("TwitchProcessor constructed");
    }

    public static synchronized TwitchAPIClient getInstance() {
        if (instance == null) {
            instance = new TwitchAPIClient();
        }
        return instance;
    }

    public TwitchChannel getStreamData(final String channelName, final boolean isBrowser) {
        TwitchChannel channel = new TwitchChannel(new JsonObject(), channelName, isBrowser);
        if (!"".equals(channelName)) {
            try {
                final URI uri = convertToURI(TWITCH_BASE_URL + "streams/" + channelName);
                final JsonObject jo = JSONPARSER.parse(getAPIResponse(uri)).getAsJsonObject();
                channel = new TwitchChannel(jo, channelName, isBrowser);
            } catch (JsonSyntaxException e) {
                LOGGER.error("ERROR while loading channel data. Return empty channel", e);
            }
        }
        return channel;
    }

    public TwitchChannels getGameData(final String game) {
        LOGGER.debug("Load game Data");
        final String gameName = game.replace(' ', '+');
        final int maxChannelsToLoad = Settings.getInstance().getMaxChannelsLoad();
        final URI uri = convertToURI(
                TWITCH_BASE_URL + "streams/?game=" + gameName + "&offset=0&limit=" + maxChannelsToLoad);
        final String response = getAPIResponse(uri);
        final JsonObject jo = JSONPARSER.parse(response).getAsJsonObject();
        return new TwitchChannels(jo);
    }

    public TwitchGames getGamesData() {
        LOGGER.debug("Load gamesData");
        final int maxGamesToLoad = Settings.getInstance().getMaxGamesLoad();
        final URI uri = convertToURI(TWITCH_BASE_URL + "games/top?offset=0&limit=" + maxGamesToLoad);
        final String response = getAPIResponse(uri);
        final JsonObject jo = JSONPARSER.parse(response).getAsJsonObject();
        return new TwitchGames(jo);
    }

    public Set<String> getListOfFollowedStreams(final String userName) {
        final Set<String> followedStreams = new TreeSet<>();
        if (!"".equals(userName) && this.channelExists(userName)) {
            final URI uri = convertToURI("https://api.twitch.tv/kraken/users/" + userName + "/follows/channels");
            JsonObject jo = JSONPARSER.parse(getAPIResponse(uri)).getAsJsonObject();

            final int total = jo.get("_total").getAsInt();
            JsonArray streams = jo.getAsJsonArray("follows");
            JsonObject links = jo.get("_links").getAsJsonObject();
            String self = links.get("self").getAsString();
            String next = links.get("next").getAsString();

            int offset = Integer.parseInt(self.split("&")[2].split("=")[1]);
            while (offset < total) {
                for (JsonElement je : streams) {
                    final JsonObject channel = je.getAsJsonObject().get("channel").getAsJsonObject();
                    final String name = channel.get("name").getAsString();
                    followedStreams.add(name);
                }
                jo = JSONPARSER.parse(getAPIResponse(convertToURI(next))).getAsJsonObject();
                streams = jo.getAsJsonArray("follows");
                links = jo.get("_links").getAsJsonObject();
                self = links.get("self").getAsString();
                next = links.get("next").getAsString();
                offset = Integer.parseInt(self.split("&")[2].split("=")[1]);
            }
        }
        return followedStreams;
    }

    public boolean channelExists(final String channel) {
        LOGGER.debug("Checking if {} is a twitch channel", channel);
        final URI uri = convertToURI(TWITCH_BASE_URL + "streams/" + channel);
        if ("{}".equals(getAPIResponse(uri)) || "".equals(channel)) {
            LOGGER.debug("{} is no twitch channel", channel);
            return false;
        }
        LOGGER.debug("{} is a twitch channel", channel);
        return true;
    }

    private static String getAPIResponse(final URI apiUrl) {
        LOGGER.trace("Send Request to API URL '{}'", apiUrl);
        final HttpGet request = new HttpGet(apiUrl);
        request.setHeader("Client-ID", LSGUI_CLIENT_ID);
        try (CloseableHttpResponse response = HTTP_CLIENT.execute(request)) {
            return new BasicResponseHandler().handleResponse(response);
        } catch (IOException e) {
            if (e.getClass().equals(UnknownHostException.class)) {
                LOGGER.error("Twitch is not reachable. Check your Internet Connection");
            } else if (e.getClass().equals(HttpResponseException.class)) {
                LOGGER.error("Http Error when fetching twitch api response. Status Code: {}",
                        ((HttpResponseException) e).getStatusCode());
            } else {
                LOGGER.error("Error when fetching twitch api response", e);
            }
        } finally {
            request.reset();
        }
        return "{}";
    }

    private static URI convertToURI(final String url) {
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            LOGGER.error("Could not convert String to URI", e);
        }
        return null;
    }

}