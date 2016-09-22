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
package app.lsgui.rest.twitch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.lsgui.model.twitch.channel.TwitchChannel;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

/**
 *
 * @author Niklas 11.06.2016
 *
 */
public final class TwitchChannelUpdateService extends ScheduledService<TwitchChannel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitchChannelUpdateService.class);
    private static final ListProperty<TwitchChannel> ACTIVE_LIST = new SimpleListProperty<>(
            FXCollections.observableArrayList());
    private static final double UPDATE_PERIOD = 60;
    private TwitchChannel channel;

    public TwitchChannelUpdateService(final TwitchChannel channel) {
        LOGGER.debug("Create UpdateService for {}", channel.getName().get());
        this.channel = channel;
        this.setUpConstant();
    }

    public void setUpConstant() {
        setPeriod(Duration.seconds(UPDATE_PERIOD));
        setRestartOnFailure(true);
        setOnSucceeded(event -> {
            final TwitchChannel updatedModel = (TwitchChannel) event.getSource().getValue();
            if (updatedModel != null) {
                synchronized (this.channel) {
                    this.channel.updateData(updatedModel, true);
                }
            }
            synchronized (ACTIVE_LIST) {
                ObservableList<TwitchChannel> activeChannelServices = FXCollections
                        .observableArrayList(ACTIVE_LIST.get());
                activeChannelServices.remove(this.channel);
                ACTIVE_LIST.set(activeChannelServices);
            }
        });
        setOnFailed(event -> LOGGER.warn("UPDATE SERVICE FAILED"));
    }

    @Override
    protected Task<TwitchChannel> createTask() {
        return new Task<TwitchChannel>() {
            @Override
            protected TwitchChannel call() throws Exception {
                synchronized (ACTIVE_LIST) {
                    ACTIVE_LIST.set(addAndGetChannelToList(channel, ACTIVE_LIST));
                }
                return TwitchAPIClient.getInstance().getStreamData(channel.getName().get(), false);
            }
        };
    }

    private static ObservableList<TwitchChannel> addAndGetChannelToList(final TwitchChannel channel,
            final ObservableList<TwitchChannel> list) {
        final ObservableList<TwitchChannel> activeChannelServices = FXCollections.observableArrayList(list);
        activeChannelServices.add(channel);
        return activeChannelServices;
    }

    public static ListProperty<TwitchChannel> getActiveChannelServicesProperty() {
        return ACTIVE_LIST;
    }
}
