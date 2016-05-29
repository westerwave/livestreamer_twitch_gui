package app.lsgui.utils;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.lsgui.model.channel.IChannel;
import app.lsgui.model.service.GenericService;
import app.lsgui.model.service.IService;
import app.lsgui.model.service.TwitchService;
import app.lsgui.rest.twitch.TwitchAPIClient;
import app.lsgui.settings.Settings;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GuiUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(GuiUtils.class);
    private static final String SERVICE = "service";
    private static final String CHANNEL = "channel";

    public enum DialogType {
        ADD, REMOVE, IMPORT;
    }

    public static void addAction(final IService service) {
        final Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Add Channel/Service");
        final Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.setMinWidth(350);
        dialog.setWidth(350);
        final String style = GuiUtils.class.getResource("/styles/" + Settings.instance().getWindowStyle() + ".css")
                .toExternalForm();
        Utils.addStyleSheetToStage(dialogStage, style);
        dialogStage.getIcons().add(new Image(GuiUtils.class.getResourceAsStream("/icon.jpg")));
        configureAddDialog(dialog, service);
    }

    private static void configureAddDialog(final Dialog<String> dialog, final IService service) {
        final TextField nameTextField = new TextField();
        final Label nameLabel = new Label("Name");
        final TextField urlTextField = new TextField();
        final Label urlLabel = new Label("URL");
        final HBox nameBox = new HBox();
        nameBox.getChildren().addAll(nameLabel, nameTextField);
        final HBox urlBox = new HBox();
        urlBox.getChildren().addAll(urlLabel, urlTextField);
        final VBox serviceBox = new VBox();
        serviceBox.getChildren().addAll(nameBox, urlBox);

        final ButtonType okType = ButtonType.OK;
        final BorderPane borderPane = new BorderPane();
        final Button addChannel = new Button("Add Channel");
        addChannel.setOnAction(event -> {
            borderPane.setCenter(nameBox);
            dialog.getDialogPane().getButtonTypes().add(okType);
            final Node okButton = dialog.getDialogPane().lookupButton(okType);
            okButton.setDisable(true);

            nameTextField.textProperty()
                    .addListener((obs, oldValue, newValue) -> okButton.setDisable("".equals(newValue)));
        });
        final Button addService = new Button("Add Service");
        boolean validName = false;
        boolean validUrl = false;
        addService.setOnAction(event -> {
            borderPane.setCenter(serviceBox);
            dialog.getDialogPane().getButtonTypes().add(okType);
            final Node okButton = dialog.getDialogPane().lookupButton(okType);
            okButton.setDisable(true);
            nameTextField.textProperty()
                    .addListener((obs, oldValue, newValue) -> okButton.setDisable("".equals(newValue)));
            urlTextField.textProperty().addListener((obs, oldValue, newValue) -> {
                if ("".equals(newValue) || "".equals(nameTextField.getText())) {
                    okButton.setDisable(true);
                }
            });
        });
        final HBox buttonBox = new HBox();
        buttonBox.getChildren().add(addChannel);
        buttonBox.getChildren().add(addService);

        borderPane.setCenter(buttonBox);

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);
        dialog.getDialogPane().setContent(borderPane);

        dialog.setResultConverter(button -> {
            if (!button.equals(ButtonType.CANCEL)) {
                if (borderPane.getCenter().equals(serviceBox)) {
                    return SERVICE;
                } else if (borderPane.getCenter().equals(nameBox)) {
                    return CHANNEL;
                }
            }
            return "";
        });

        final Optional<String> result = dialog.showAndWait();
        final String resultString = result.get().trim();
        if (result.isPresent() && !"".equals(resultString)) {
            if (resultString.equals(CHANNEL) && !"".equals(nameTextField.getText().trim())) {
                final String channel = nameTextField.getText().trim();
                addChannelToService(channel, service);
            } else if (resultString.equals(SERVICE) && !"".equals(nameTextField.getText().trim())
                    && !"".equals(urlTextField.getText().trim())) {
                final String name = nameTextField.getText().trim();
                final String url = correctUrl(urlTextField.getText().trim());
                addService(name, url);
            }
        }
    }

    public static void removeAction(final IChannel channel, final IService service) {
        removeChannelFromService(channel, service);
    }

    public static void importFollowedChannels(final TwitchService service) {
        final Dialog<Boolean> dialog = new Dialog<>();
        final Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.setMinWidth(300);
        final String style = GuiUtils.class.getResource("/styles/" + Settings.instance().getWindowStyle() + ".css")
                .toExternalForm();
        Utils.addStyleSheetToStage(dialogStage, style);
        dialogStage.getIcons().add(new Image(GuiUtils.class.getResourceAsStream("/icon.jpg")));
        dialog.setTitle("Import Twitch.tv followed Channels");
        configureImportDialog(dialog, service);
    }

    private static void configureImportDialog(final Dialog<Boolean> dialog, final TwitchService service) {
        final ButtonType bt = new ButtonType("Import", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bt, ButtonType.CANCEL);

        final BorderPane ap = new BorderPane();
        final TextField tf = new TextField();
        final Label description = new Label("Please enter your Twitch.tv Username:");
        ap.setTop(description);
        ap.setCenter(tf);
        dialog.getDialogPane().setContent(ap);

        final Node submitButton = dialog.getDialogPane().lookupButton(bt);
        submitButton.setDisable(true);

        tf.textProperty()
                .addListener((observable, oldValue, newValue) -> submitButton.setDisable(newValue.trim().isEmpty()));

        dialog.setResultConverter(button -> {
            if ("".equals(tf.getText().trim()) || button.equals(ButtonType.CANCEL)) {
                return false;
            }
            if (TwitchAPIClient.instance().channelExists(tf.getText().trim())) {
                return true;
            }
            return false;
        });
        tf.requestFocus();
        final Optional<Boolean> result = dialog.showAndWait();
        if (result.isPresent() && result.get()) {
            addFollowedChannelsToService(tf.getText().trim(), service);
        }
    }

    private static void addChannelToService(final String channel, final IService service) {
        service.addChannel(channel);
    }

    private static void addFollowedChannelsToService(final String channel, final TwitchService service) {
        service.addFollowedChannels(channel);
    }

    private static void removeChannelFromService(final IChannel channel, final IService service) {
        service.removeChannel(channel);
    }

    private static void addService(final String serviceName, final String serviceUrl) {
        LOGGER.debug("Add new Service {} with URL {}", serviceName, serviceUrl);
        String correctedUrl = serviceUrl;
        if (!serviceUrl.endsWith("/")) {
            correctedUrl += "/";
        }
        Settings.instance().getStreamServices().add(new GenericService(serviceName, correctedUrl));
    }

    private static String correctUrl(final String url) {
        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }
}
