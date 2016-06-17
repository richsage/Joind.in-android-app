package in.joind;

/*
 * Displays event details (info, talk list)
 */

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import in.joind.model.Event;

public class EventDetail extends JIActivity implements OnClickListener {
    final public static String INTENT_KEY_EVENT_ROW_ID = "eventRowID";

    private Event event;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allow ActionBar 'up' navigation
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        // Set layout
        setContentView(R.layout.eventdetail);


        // Add handler to buttons
        Button button = (Button) findViewById(R.id.ButtonEventDetailsViewComments);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.ButtonEventDetailsViewTalks);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.ButtonEventDetailsViewTracks);
        button.setOnClickListener(this);

        // We et the onclick listener for the 'i attended' button AFTER loaded the details.
        // Otherwise we might end up clicking it when it's not in the correct state (disabled when you are
        // attending the event)
        CheckBox checkbox = (CheckBox) findViewById(R.id.CheckBoxEventDetailsAttending);
        checkbox.setOnClickListener(this);
    }

    public void onResume() {
        super.onResume();

        CheckBox checkbox = (CheckBox) findViewById(R.id.CheckBoxEventDetailsAttending);
        checkbox.setEnabled(isAuthenticated());

        int eventRowID = getIntent().getIntExtra(INTENT_KEY_EVENT_ROW_ID, 0);
        event = DataHelper.getInstance(this).getEvent(eventRowID);

        if (event == null) {
            Log.v(JIActivity.LOG_JOINDIN_APP, "No event JSON available to activity");
            Crashlytics.setString("eventDetail_eventJSON", getIntent().getStringExtra("eventJSON"));

            // Tell the user
            showToast(getString(R.string.activityEventDetailFailedJSON), Toast.LENGTH_LONG);
            finish();
            return;
        }

        displayDetails();
    }

    public void displayDetails() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setTitle(event.name);

        TextView t;
        t = (TextView) this.findViewById(R.id.EventDetailsEventLoc);
        t.setText(event.location);

        String d1;
        String d2;

        // Android 2.2 and below don't support the "L" pattern character
        String fmt = Build.VERSION.SDK_INT <= 8 ? "d MMM yyyy" : "d LLL yyyy";
        SimpleDateFormat dfOutput = new SimpleDateFormat(fmt, Locale.US),
                dfInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
        try {
            d1 = dfOutput.format(dfInput.parse(event.start_date));
            d2 = dfOutput.format(dfInput.parse(event.end_date));
            getSupportActionBar().setSubtitle(d1.equals(d2) ? d1 : d1 + " - " + d2);
        } catch (ParseException e) {
            e.printStackTrace();
            getSupportActionBar().setSubtitle("");
        }

        String hashtag = event.hashtag;
        this.findViewById(R.id.EventDetailsHashtagsRow).setVisibility(hashtag != null && hashtag.length() > 0 ? View.VISIBLE : View.GONE);
        if (hashtag != null) {
            t = (TextView) this.findViewById(R.id.EventDetailsStub);
            t.setText(event.hashtag);
        }

        t = (TextView) this.findViewById(R.id.EventDetailsDescription);
        t.setText(event.description);
        Linkify.addLinks(t, Linkify.ALL);

        // Add number of talks to the correct button caption
        Button b = (Button) this.findViewById(R.id.ButtonEventDetailsViewTalks);
        int talkCount = event.talks_count;
        if (talkCount == 0) {
            b.setText(getString(R.string.generalViewTalkNoCount));
        } else if (talkCount == 1) {
            b.setText(String.format(getString(R.string.generalViewTalkSingular), talkCount));
        } else {
            b.setText(String.format(getString(R.string.generalViewTalkPlural), talkCount));
        }

        // Add number of comments to the correct button caption
        b = (Button) this.findViewById(R.id.ButtonEventDetailsViewComments);
        int commentCount = event.event_comments_count;
        if (commentCount == 1) {
            b.setText(String.format(getString(R.string.generalViewCommentSingular), commentCount));
        } else {
            b.setText(String.format(getString(R.string.generalViewCommentPlural), commentCount));
        }

        // See if this event has tracks
        b = (Button) this.findViewById(R.id.ButtonEventDetailsViewTracks);
        int trackCount = event.tracks_count;
        if (trackCount == 1) {
            b.setText(String.format(getString(R.string.generalViewTrackSingular), trackCount));
        } else {
            b.setText(String.format(getString(R.string.generalViewTrackPlural), trackCount));
        }

        // Tick the checkbox, depending on if we are attending or not
        CheckBox c = (CheckBox) findViewById(R.id.CheckBoxEventDetailsAttending);
        c.setChecked(event.attending);
    }

    public void onClick(View v) {
        if (v == findViewById(R.id.ButtonEventDetailsViewComments)) {
            // Display event comments activity
            Intent myIntent = new Intent();
            myIntent.setClass(getApplicationContext(), EventComments.class);
            myIntent.putExtra("eventJSON", getIntent().getStringExtra("eventJSON"));
            startActivity(myIntent);
        }
        if (v == findViewById(R.id.ButtonEventDetailsViewTalks)) {
            // Display talks activity
            Intent myIntent = new Intent();
            myIntent.setClass(getApplicationContext(), EventTalks.class);
            myIntent.putExtra("eventJSON", getIntent().getStringExtra("eventJSON"));
            startActivity(myIntent);
        }
        if (v == findViewById(R.id.ButtonEventDetailsViewTracks)) {
            // Display talks activity
            Intent myIntent = new Intent();
            myIntent.setClass(getApplicationContext(), EventTracks.class);
            myIntent.putExtra("eventJSON", getIntent().getStringExtra("eventJSON"));
            startActivity(myIntent);
        }
        if (v == findViewById(R.id.CheckBoxEventDetailsAttending)) {
            // Check box clicked. This will toggle if we are attending the event or not.
            new Thread() {
                // We run in a background thread
                public void run() {
                    // Display progress bar (@TODO: Check if this works since it's not a UI thread)
                    displayProgressBarCircular(true);

                    // Fetch state of checkbox (on or off)
                    CheckBox cb = (CheckBox) findViewById(R.id.CheckBoxEventDetailsAttending);
                    // Tell joind.in API that we attend (or unattended) this event
                    final String result = attendEvent(cb.isChecked());

                    // Display result, must be done in UI thread
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // Display result from attendEvent
                            Toast toast = Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });

                    // Stop displaying progress bar
                    displayProgressBarCircular(false);
                }
            }.start();
        }
    }


    // This function will send to joind.in if we attend (or unattended) specified event.
    private String attendEvent(boolean initialState) {
        // Send data to the joind.in API
        JIRest rest = new JIRest(EventDetail.this);
        int error = rest.requestToFullURI(this.eventJSON.optString("attending_uri"), null,
                initialState ? JIRest.METHOD_POST : JIRest.METHOD_DELETE);

        if (error != JIRest.OK) {
            // Incorrect result, return error
            return String.format(getString(R.string.generatelAttendingError), rest.getError());
        } else {
            // Everything went as expected
            // We update the event, since the even has been changed (attendee count)
            try {
                loadDetails(eventRowID, eventJSON.getString("verbose_uri"));
            } catch (JSONException e) {
                Log.e(JIActivity.LOG_JOINDIN_APP, "No verbose URI available");
            }

            if (initialState) {
                return getString(R.string.generalSuccessFullyAttended);
            } else {
                return getString(R.string.generalSuccessFullyUnAttended);
            }
        }
    }
}
