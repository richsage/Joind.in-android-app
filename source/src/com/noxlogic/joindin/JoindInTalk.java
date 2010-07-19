package com.noxlogic.joindin;

import org.json.JSONObject;


public class JoindInTalk {
    protected JSONObject talk;

	public JoindInTalk (int talk_id) {
        DataHelper dh = DataHelper.getInstance();
        this.talk = dh.getEvent (talk_id);
	}
	
	public boolean isValid () {
		return (this.talk.length() != 0);
	}
}
