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
package app.lsgui.gui.main.toolbar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.lsgui.utils.Settings;
import javafx.scene.control.ComboBox;

public final class QualityComboBox extends ComboBox<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(QualityComboBox.class);
    private static final String OFFLINEQUALITY = "Channel is offline";

    public QualityComboBox() {
        // Empty Constructor
    }

    public void initialize() {
        getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !OFFLINEQUALITY.equals(newValue)) {
                LOGGER.debug("Set selected Quality to {}", newValue);
                Settings.getInstance().qualityProperty().set(newValue);
            }
        });
    }

}
