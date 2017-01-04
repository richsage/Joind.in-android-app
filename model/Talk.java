package in.joind.model;

import java.util.ArrayList;

public class Talk {
    final public static String TYPE_TALK = "Talk";
    final public static String TYPE_SOCIAL_EVENT = "Social Event";
    final public static String TYPE_WORKSHOP = "Workshop";
    final public static String TYPE_KEYNOTE = "Keynote";

    public String talk_title;
    public String url_friendly_talk_title;
    public String talk_description;
    public String type;
    public String slides_link;
    public String language;
    public String start_date;
    public int duration;
    public String stub;
    public int average_rating;
    public int comments_enabled;
    public int comment_count;
    public boolean starred;
    public int starred_count;
    public ArrayList<Speaker> speakers;
    public ArrayList<Track> tracks;
    public String uri;
    public String verbose_uri;
    public String website_uri;
    public String comments_uri;
    public String verbose_comments_uri;
    public String event_uri;
    public String starred_uri;

    // Internal
    public int _rowID;
}
