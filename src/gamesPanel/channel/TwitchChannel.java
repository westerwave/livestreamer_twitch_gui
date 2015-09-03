package gamesPanel.channel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

import gamesPanel.TwitchDirectory;
import twitchAPI.Twitch_Game_Json;

public class TwitchChannel {
    private BufferedImage preview;
    private Twitch_Game_Json json;
    private String name;
    private TwitchDirectory parent;

    public TwitchChannel(BufferedImage preview, Twitch_Game_Json json, TwitchDirectory parent) {
	this.preview = preview;
	this.json = json;
	this.name = json.getName();
	this.parent = parent;
    }

    public TwitchChannel(Twitch_Game_Json json, TwitchDirectory parent) {
	this.preview = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	this.json = json;
	this.name = json.getName();
	this.parent = parent;
    }

    public JButton getButton() {
	JButton button = new JButton();
	String text = json.getName();
	String viewers = json.getViewers() + "";
	setPreview(preview);
	button.setIcon(new ImageIcon(preview));
	button.setHorizontalTextPosition(JLabel.CENTER);
	button.setVerticalTextPosition(JLabel.BOTTOM);
	button.setText("<html>" + text + "<br>Viewers: " + viewers + "</html>");
	button.setToolTipText(json.getChannel());
	button.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent e) {
		String name = ((JButton) e.getSource()).getToolTipText();
		parent.parent.openStream(name);
	    }
	});
	return button;
    }

    /**
     * @return the logo
     */
    public BufferedImage getPreview() {
	return preview;
    }

    /**
     * @param logo
     *            the logo to set
     */
    public void setPreview(BufferedImage preview) {
	this.preview = preview;
    }

    /**
     * @return the json
     */
    public Twitch_Game_Json getJson() {
	return json;
    }

    /**
     * @param json
     *            the json to set
     */
    public void setJson(Twitch_Game_Json json) {
	this.json = json;
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
	this.name = name;
    }
}
