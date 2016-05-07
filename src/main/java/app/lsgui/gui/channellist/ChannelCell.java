package app.lsgui.gui.channellist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.lsgui.model.Channel;
import javafx.scene.control.ListCell;
import javafx.scene.paint.Color;

public class ChannelCell extends ListCell<Channel> {// NOSONAR

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelCell.class); // NOSONAR

    @Override
    protected void updateItem(Channel item, boolean isEmpty) {
        super.updateItem(item, isEmpty);
        if (isEmpty || item == null) {
            setText(null);
        } else {
            setText(item.getName().get());
            if (item.isOnline().get()) {
                setStyle("-fx-background-color: green");
                setTextFill(Color.BLACK);
            } else {
                setStyle("");
                setTextFill(Color.BLACK);
            }
        }
    }
}
