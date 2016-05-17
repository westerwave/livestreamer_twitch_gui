package app.lsgui.gui.channelinfopanel;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.lsgui.gui.MainWindow;
import app.lsgui.gui.chat.ChatWindow;
import app.lsgui.model.IChannel;
import app.lsgui.model.twitch.TwitchChannel;
import app.lsgui.service.IService;
import app.lsgui.utils.LivestreamerUtils;
import app.lsgui.utils.Utils;
import app.lsgui.utils.WrappedImageView;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

public class ChannelInfoPanel extends BorderPane { // NOSONAR

    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelInfoPanel.class);
    private static final String CHANNELINFOPANELFXML = "fxml/ChannelInfoPanel.fxml";
    private static FXMLLoader loader;

    private ComboBox<IService> serviceComboBox;
    private ComboBox<String> qualityComboBox;

    private ObjectProperty<IChannel> channelProperty;

    private WrappedImageView previewImageView;

    private Label channelDescription;
    private Label channelUptime;
    private Label channelViewers;
    private Label channelGame;

    @FXML
    private BorderPane rootBorderPane;

    @FXML
    private CheckBox notifyCheckBox;

    @FXML
    private GridPane descriptionGrid;

    @FXML
    private ToolBar buttonBox;

    public ChannelInfoPanel(ComboBox<IService> serviceComboBox, ComboBox<String> qualityComboBox) {
        channelProperty = new SimpleObjectProperty<>();

        this.serviceComboBox = serviceComboBox;
        this.qualityComboBox = qualityComboBox;
        loader = new FXMLLoader();
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load(getClass().getClassLoader().getResourceAsStream(CHANNELINFOPANELFXML));
        } catch (IOException e) {
            LOGGER.error("ERROR while loading ChannelInfoPanel FXML", e);
        }
        setupChannelInfoPanel();
        setupChannelListener();

    }

    private void setupChannelListener() {
        channelProperty.addListener((observable, oldValue, newValue) -> {
            final IChannel selectedChannel = newValue;
            if (selectedChannel != null) {
                if (Utils.isTwitchChannel(selectedChannel)) {
                    final TwitchChannel twitchChannel = (TwitchChannel) selectedChannel;
                    previewImageView.imageProperty().bind((twitchChannel).getPreviewImage());
                    channelDescription.textProperty().bind((twitchChannel).getDescription());
                    channelUptime.textProperty().bind((twitchChannel).getUptimeString());
                    channelUptime.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.CLOCK_ALT));
                    channelViewers.textProperty().bind((twitchChannel).getViewersString());
                    channelViewers.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.USER));
                    channelGame.textProperty().bind((twitchChannel).getGame());
                    channelGame.setGraphic(GlyphsDude.createIcon(FontAwesomeIcon.GAMEPAD));
                } else {
                    channelDescription.textProperty().bind(channelProperty.get().getName());
                }
            }
        });
    }

    private void setupChannelInfoPanel() {
        previewImageView = new WrappedImageView(null);
        rootBorderPane.setCenter(previewImageView);

        channelDescription = new Label();
        channelDescription.setWrapText(true);
        channelUptime = new Label();
        channelViewers = new Label();
        channelGame = new Label();

        descriptionGrid.add(channelGame, 0, 0, 1, 1);
        descriptionGrid.add(channelViewers, 0, 1, 1, 1);
        descriptionGrid.add(channelUptime, 0, 2, 1, 1);
        descriptionGrid.add(channelDescription, 0, 3, 1, 1);

        final Button startStreamButton = GlyphsDude.createIconButton(FontAwesomeIcon.PLAY);
        startStreamButton.setOnAction(event -> startStream());

        final Button recordStreamButton = GlyphsDude.createIconButton(FontAwesomeIcon.DOWNLOAD);
        recordStreamButton.setOnAction(event -> recordStream());

        final Button openChatButton = GlyphsDude.createIconButton(FontAwesomeIcon.COMMENT);
        openChatButton.setOnAction(event -> openChat());

        final Button openBrowserButton = GlyphsDude.createIconButton(FontAwesomeIcon.EDGE);
        openBrowserButton.setOnAction(event -> openBrowser());

        buttonBox.getItems().add(startStreamButton);
        buttonBox.getItems().add(recordStreamButton);
        buttonBox.getItems().add(openChatButton);
        buttonBox.getItems().add(openBrowserButton);
    }

    public void setStream(final IChannel channel) {
        channelProperty.setValue(channel);
    }

    private void startStream() {
        if (channelProperty.get() != null && channelProperty.get().isOnline().get()) {
            final String url = buildURL();
            final String quality = getQuality();
            LivestreamerUtils.startLivestreamer(url, quality);
        }
    }

    private void recordStream() {
        if (channelProperty.get() != null && !"".equals(channelProperty.get().getName().get())
                && channelProperty.get().isOnline().get()) {
            final String url = buildURL();
            final String quality = getQuality();

            final FileChooser recordFileChooser = new FileChooser();
            recordFileChooser.setTitle("Choose Target file");
            recordFileChooser.getExtensionFilters().add(new ExtensionFilter("MPEG4", ".mpeg4"));
            final File recordFile = recordFileChooser.showSaveDialog(MainWindow.getRootStage());
            if (recordFile != null) {
                LivestreamerUtils.recordLivestreamer(url, quality, recordFile);
            }
        }
    }

    private void openChat() {
        if (channelProperty.get() != null && !"".equals(channelProperty.get().getName().get())
                && Utils.isTwitchChannel(channelProperty.get())) {
            final String channel = channelProperty.get().getName().get();
            ChatWindow cw = new ChatWindow(channel);
            cw.connect();
        }
    }

    private void openBrowser() {
        if (channelProperty.get() != null && !"".equals(channelProperty.get().getName().get())) {
            final String channel = channelProperty.get().getName().get();
            Utils.openURLInBrowser(serviceComboBox.getSelectionModel().getSelectedItem().getUrl().get() + channel);
        }
    }

    public ObjectProperty<IChannel> getChannelProperty() {
        return channelProperty;
    }

    public void setChannelProperty(ObjectProperty<IChannel> channelProperty) {
        this.channelProperty = channelProperty;
    }

    private String buildURL() {
        return serviceComboBox.getSelectionModel().getSelectedItem().getUrl().get()
                + channelProperty.get().getName().get();
    }

    private String getQuality() {
        return qualityComboBox.getSelectionModel().getSelectedItem();
    }
}
