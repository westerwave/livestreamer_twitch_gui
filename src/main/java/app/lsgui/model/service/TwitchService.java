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
package app.lsgui.model.service;

import java.util.Comparator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import app.lsgui.model.channel.IChannel;
import app.lsgui.model.twitch.channel.TwitchChannel;
import app.lsgui.rest.twitch.TwitchAPIClient;
import app.lsgui.rest.twitch.TwitchChannelUpdateService;
import app.lsgui.settings.Settings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.transformation.SortedList;

/**
 *
 * @author Niklas 11.06.2016
 *
 */
public class TwitchService implements IService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitchService.class);
    public static final String TWITCH_ID = "twitch.tv";

    private StringProperty name;
    private StringProperty url;
    private ObjectProperty<SortedList<IChannel>> channelProperty;
    private ObservableList<IChannel> channelList = FXCollections.observableArrayList(TwitchChannel.extractor());
    private BooleanProperty sortChannels;

    private static final ObservableMap<IChannel, TwitchChannelUpdateService> UPDATESERVICES = FXCollections
            .observableHashMap();

    public TwitchService(final String name, final String url) {
        this.name = new SimpleStringProperty(name);
        this.url = new SimpleStringProperty(url);
        channelProperty = new SimpleObjectProperty<>(new SortedList<>(channelList));
        sortChannels = new SimpleBooleanProperty();
        sortChannels.bind(Settings.getInstance().getSortTwitch());
        sortChannels.addListener((observable, oldValue, newVale) -> changeComparator(newVale));
        channelProperty.get().addListener(new ListChangeListener<IChannel>() {
            @Override
            public void onChanged(Change<? extends IChannel> c) {
                c.next();
                changeComparator(sortChannels.get());
            }
        });
    }

    @Override
    public void addChannel(final String name) {
        LOGGER.debug("Add Channel {} to {} Service", name, this.getName().get());
        final TwitchChannel channelToAdd = new TwitchChannel(new JsonObject(), name, false);
        final TwitchChannelUpdateService tcus = new TwitchChannelUpdateService(channelToAdd);
        tcus.start();
        UPDATESERVICES.put(channelToAdd, tcus);
        channelList.add(channelToAdd);
    }

    @Override
    public void removeChannel(final IChannel channel) {
        if (channel instanceof TwitchChannel) {
            LOGGER.debug("Remove Channel {} from Service {}", channel.getName(), this.getName().get());
            final TwitchChannelUpdateService tcus = UPDATESERVICES.remove(channel);
            tcus.cancel();
            channelList.remove(channel);
        }
    }

    public void addFollowedChannels(final String username) {
        LOGGER.debug("Import followed Streams for user {} into Service {}", username, this.getName().get());
        final Set<String> set = TwitchAPIClient.getInstance().getListOfFollowedStreams(username);
        for (final String s : set) {
            addChannel(s);
        }
    }

    private void changeComparator(final boolean doSorting) {
        final Comparator<IChannel> comp;
        if (!doSorting) {
            comp = (ch1, ch2) -> ch1.getName().get().compareToIgnoreCase(ch2.getName().get());
        } else {
            comp = (ch1, ch2) -> {
                if (ch1.isOnline().get() && !ch2.isOnline().get()) {
                    return -1;
                } else if (!ch1.isOnline().get() && ch2.isOnline().get()) {
                    return 1;
                } else {
                    return ch1.getName().get().compareToIgnoreCase(ch2.getName().get());
                }
            };
        }
        getChannelProperty().get().setComparator(comp);
    }

    @Override
    public ObjectProperty<SortedList<IChannel>> getChannelProperty() {
        return channelProperty;
    }

    @Override
    public StringProperty getName() {
        return name;
    }

    @Override
    public StringProperty getUrl() {
        return url;
    }

    public ObservableMap<IChannel, TwitchChannelUpdateService> getUpdateServices() {
        return UPDATESERVICES;
    }

}
