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
package app.lsgui.gui.settings;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.lsgui.gui.main.LsGuiWindow;
import app.lsgui.utils.LsGuiUtils;
import app.lsgui.utils.Settings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

/**
 *
 * @author Niklas 11.06.2016
 *
 */
public final class SettingsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsController.class);

    @FXML
    private CheckBox sortCheckBox;

    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField oauthTextField;

    @FXML
    private ChoiceBox<String> styleChoiceBox;

    @FXML
    private ChoiceBox<Integer> gamesToLoadChoiceBox;

    @FXML
    private ChoiceBox<Integer> channelsToLoadChoiceBox;

    @FXML
    private Button exeBrowseButton;

    @FXML
    private Hyperlink updateLink;

    @FXML
    private Button openSettingsFolder;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    public SettingsController() {
        // Empty Constructor
    }

    @FXML
    public void initialize() {
        LOGGER.info("SettingsController init");
        this.setupStyleChoiceBox();
        this.setupLoadChoiceBoxes();
        final Settings settings = Settings.getInstance();

        this.sortCheckBox.setSelected(settings.sortTwitchProperty().get());
        this.oauthTextField.setText(settings.twitchOAuthProperty().get());
        this.usernameTextField.setText(settings.twitchUserProperty().get());
        this.gamesToLoadChoiceBox.getSelectionModel().select(Integer.valueOf(settings.maxGamesProperty().get()));
        this.channelsToLoadChoiceBox.getSelectionModel().select(Integer.valueOf(settings.maxChannelsProperty().get()));

        this.sortCheckBox.setOnAction(event -> settings.sortTwitchProperty().setValue(this.sortCheckBox.isSelected()));
        this.usernameTextField.textProperty()
                .addListener((observable, oldValue, newValue) -> settings.twitchUserProperty().setValue(newValue));
        this.oauthTextField.textProperty()
                .addListener((observable, oldValue, newValue) -> settings.twitchOAuthProperty().setValue(newValue));
        this.styleChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            settings.windowStyleProperty().setValue(newValue);
            final String style = SettingsController.class.getResource("/styles/" + newValue + ".css").toExternalForm();

            LsGuiUtils.clearStyleSheetsFromStage(LsGuiWindow.getRootStage());
            LsGuiUtils.clearStyleSheetsFromStage(SettingsWindow.getSettingsStage());

            LsGuiUtils.addStyleSheetToStage(LsGuiWindow.getRootStage(), style);
            LsGuiUtils.addStyleSheetToStage(SettingsWindow.getSettingsStage(), style);
        });

        this.gamesToLoadChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> settings.maxGamesProperty().setValue(newValue));

        this.channelsToLoadChoiceBox.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> settings.maxChannelsProperty().setValue(newValue));

        this.exeBrowseButton.setOnAction(event -> {
            final FileChooser exeFileChooser = new FileChooser();
            exeFileChooser.setTitle("Choose Livestreamer.exe file");
            exeFileChooser.getExtensionFilters().add(new ExtensionFilter("EXE", "*.exe"));
            final File exeFile = exeFileChooser.showOpenDialog(LsGuiWindow.getRootStage());
            if (exeFile != null) {
                Settings.getInstance().livestreamerPathProperty().set(exeFile.getAbsolutePath());
            }
        });
        final String updateLinkString = settings.updateLinkProperty().get();
        if (updateLinkString != null && !"".equals(updateLinkString)) {
            this.updateLink.setText("New Version available!");
            this.updateLink.setOnAction(event -> LsGuiUtils.openURLInBrowser(settings.updateLinkProperty().get()));
        } else {
            this.updateLink.setDisable(true);
        }

        this.saveButton.setOnAction(event -> saveSettingsAction());
        this.cancelButton.setOnAction(event -> cancelSettingsAction());

        this.openSettingsFolder.setOnAction(event -> {
            try {
                Desktop.getDesktop().open(Settings.getInstance().getFilePath().toFile());
            } catch (IOException e) {
                LOGGER.error("Could not open settings folder", e);
            }
        });
    }

    private void setupLoadChoiceBoxes() {
        final int min = 10;
        final int max = 110;
        final int step = 10;
        for (int i = min; i < max; i += step) {
            this.gamesToLoadChoiceBox.getItems().add(i);
            this.channelsToLoadChoiceBox.getItems().add(i);
        }
        final Settings settings = Settings.getInstance();
        final int maxGamesToLoad = settings.maxGamesProperty().get();
        final int maxChannelsToLoad = settings.maxChannelsProperty().get();
        this.gamesToLoadChoiceBox.getSelectionModel().select(maxGamesToLoad);
        this.channelsToLoadChoiceBox.getSelectionModel().select(maxChannelsToLoad);
    }

    private void setupStyleChoiceBox() {
        this.styleChoiceBox.getItems().add("DarkStyle");
        this.styleChoiceBox.getItems().add("LightStyle");
        this.styleChoiceBox.getSelectionModel().select(Settings.getInstance().windowStyleProperty().get());
    }

    private static void cancelSettingsAction() {
        SettingsWindow.getSettingsStage().hide();
        SettingsWindow.getSettingsStage().close();
    }

    private static void saveSettingsAction() {
        Settings.getInstance().saveSettings();
        SettingsWindow.getSettingsStage().hide();
        SettingsWindow.getSettingsStage().close();
    }
}
