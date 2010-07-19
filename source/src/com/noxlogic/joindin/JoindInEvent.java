package com.noxlogic.joindin;

import java.text.DateFormat;

import org.json.JSONArray;
import org.json.JSONObject;


public class JoindInEvent {
    protected JSONObject event;

	public JoindInEvent (int event_id) {
        DataHelper dh = DataHelper.getInstance();
        this.event = dh.getEvent (event_id);
	}
	
	public boolean isValid () {
		return (this.event.length() != 0);
	}
	
	public String getName () {
		return this.event.optString("event_name");
	}
	
	public String getLocation () {
		return this.event.optString("event_loc");
	}
	
	public String getStub () {
		return this.event.optString("event_stub");
	}
	
	public String getDescription () {
		return this.event.optString("event_desc");
	}
	
	public int getCommentCount () {
		return this.event.optInt("num_comments");
	}
	
	public int getTalkCount () {
		return this.event.optInt("num_talks");
	}
	
	public int getTrackCount () {
		JSONArray tracks = event.optJSONArray("tracks");
		if (tracks == null) return 0;
		return tracks.length();	
	}
	
	public boolean isUserAttending () {
		return this.event.optBoolean("user_attending");
	}
	
	public String getStartDate () {
        return DateFormat.getDateInstance().format(this.event.optLong("event_start")*1000);
	}
	
	public String getEndDate () {
        return DateFormat.getDateInstance().format(this.event.optLong("event_end")*1000);		
	}
}
