package settings;

import java.util.ArrayList;

import stream.StreamList;

/**
 * Class for managing global variables
 * 
 * @author Niklas 07.03.2015
 *
 */
public class Globals {

    public boolean sortTwitch = true;
    public ArrayList<StreamList> streamServicesList;
    public String currentStreamService = "twitch.tv";
    public String currentQuality = "High";
    public final Version VERSION = new Version(2, 0, 4, 1);
    public final long timeout = 5000L;
    public boolean _DEBUG = false;
    public SettingsManager settingsManager;
    public String currentStreamName = "";
    public boolean showPreview = true;
    public int checkTimer = 30;
    public int downloadedBytes = 0;
    public String twitchUser = "";
    public String twitchOAuth = "";
    public boolean autoUpdate = true;
    public String path = "";
    public boolean showLog = false;
    public String lookAndFeel = "System";
    public int maxGamesLoad = 20;
    public int maxChannelsLoad = 20;
    public Integer chatFontSize = 3;
    public String chatFont = "Tahoma";
}
