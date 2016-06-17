package in.joind.model;

import java.util.ArrayList;
import java.util.Map;

public class Event {
    public String name;
    public String url_friendly_name;
    public String start_date;
    public String end_date;
    public String description;
    public String stub;
    public String href;
    public float latitude;
    public float longitutde;
    public String tz_continent;
    public String tz_place;
    public String location;
    public String hashtag;
    public int attendee_count;
    public boolean attending;
    public int comments_enabled;
    public int event_comments_count;
    public int tracks_count;
    public int talks_count;
    public String cfp_start_date;
    public String cfp_end_date;
    public String cfp_url;
    public int talk_comments_count;
    public String icon;
    public Map<String, ImageDetail> images;
    public ArrayList<String> tags;
    public String uri;
    public String verbose_uri;
    public String comments_uri;
    public String talks_uri;
    public String tracks_uri;
    public String attending_uri;
    public String website_uri;
    public String humane_website_uri;
    public String attendees_uri;
    public String all_talk_comments_uri;
    public ArrayList<EventHost> hosts;
    public boolean can_edit;

    // Internal
    public int _rowID;
}
