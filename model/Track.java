package in.joind.model;

public class Track {
    public String track_name;
    public String track_uri;
    public String track_description;
    public int talks_count;
    public String uri;
    public String verbose_uri;
    public String event_uri;

    /**
     * Track model is used in two places, where return data is in a slightly different format
     * Either we get the 'uri' property (/tracks) or we get 'track_uri' (/talks)
     *
     * @return String
     */
    public String getUri() {
        if (uri != null && uri.length() > 0) {
            return uri;
        }

        return track_uri;
    }
}
