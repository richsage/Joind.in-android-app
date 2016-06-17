package in.joind.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import in.joind.DateHelper;
import in.joind.ImageLoader;
import in.joind.R;
import in.joind.model.Event;

public class EventListAdapter extends ArrayAdapter<Event> {
    private final ArrayList<Event> all_items;
    private ArrayList<Event> filtered_items;
    private Context context;
    LayoutInflater inflater;
    public ImageLoader image_loader;
    private PTypeFilter filter;

    public int getCount() {
        return filtered_items.size();
    }

    public Event getItem(int position) {
        return filtered_items.get(position);
    }

    public EventListAdapter(Context context, int textViewResourceId, ArrayList<Event> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.all_items = items;
        this.filtered_items = items;
        this.inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.image_loader = new ImageLoader(context.getApplicationContext(), "events");
    }

    // This function will create a custom row with our event data.
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = this.inflater.inflate(R.layout.eventrow, parent, false);
        }

        // Get the data we need
        Event event = filtered_items.get(position);
        if (event == null) return convertView;

        // Display (or load in the background if needed) the event logo
        // We temporarily set the view to GONE to ensure that the row
        // gets recycled (and the image gets updated if required)
        ImageView el = (ImageView) convertView.findViewById(R.id.EventDetailLogo);
        el.setTag("");
        el.setVisibility(View.GONE);
        el.setImageDrawable(null);

        // Display (or load in the background if needed) the event logo
        if (event.icon != null) {
            String filename = event.icon;
            el.setTag(filename);
            image_loader.displayImage("http://joind.in/inc/img/event_icons/", filename, (Activity) context, el);
        } else {
            el.setImageResource(R.drawable.event_icon_none);
        }
        el.setVisibility(View.VISIBLE);

        // Set a darker color when the event is currently running.
        long event_start = 0;
        long event_end = 0;
        try {
            event_start = new SimpleDateFormat(context.getString(R.string.apiDateFormat), Locale.US).parse(event.start_date).getTime();
            event_end = new SimpleDateFormat(context.getString(R.string.apiDateFormat), Locale.US).parse(event.end_date).getTime();
        } catch (ParseException e) {
            // do nothing
        }
        long cts = System.currentTimeMillis() / 1000;
        if (event_start <= cts && cts <= event_end) {
            convertView.setBackgroundColor(Color.rgb(218, 218, 204));
        } else {
            // This isn't right. We shouldn't set a white color, but the default color
            convertView.setBackgroundColor(Color.rgb(255, 255, 255));
        }

        // Find our textviews we need to fill
        TextView tt = (TextView) convertView.findViewById(R.id.EventDetailCaption);
        TextView bt = (TextView) convertView.findViewById(R.id.EventDetailDate);
        TextView at = (TextView) convertView.findViewById(R.id.EventDetailAttending);

        // When the user is attending this event, we display our "attending" image.
        ImageView im = (ImageView) convertView.findViewById(R.id.EventDetailAttendingImg);
        if (!event.attending) {
            im.setVisibility(View.GONE);
        } else {
            im.setVisibility(View.VISIBLE);
        }

        // Set our texts
        if (at != null)
            at.setText(String.format(this.context.getString(R.string.activityMainAttending), event.attendee_count));
        if (tt != null) tt.setText(event.name);
        if (bt != null) {
            // Display start date. Only display end date when it differs (ie: it's multiple day event)
            // Android 2.2 and below don't support the "L" pattern character
            String fmt = Build.VERSION.SDK_INT <= 8 ? "d MMM yyyy" : "d LLL yyyy";
            String d1 = DateHelper.parseAndFormat(event.start_date, fmt);
            String d2 = DateHelper.parseAndFormat(event.end_date, fmt);
            bt.setText(d1.equals(d2) ? d1 : d1 + " - " + d2);
        }

        return convertView;
    }


    public Filter getFilter() {
        if (filter == null) {
            filter = new PTypeFilter();
        }
        return filter;
    }

    private class PTypeFilter extends Filter {
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence prefix, FilterResults results) {
            filtered_items = (ArrayList<Event>) results.values;
            notifyDataSetChanged();
        }

        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();
            ArrayList<Event> i = new ArrayList<>();

            if (prefix != null && prefix.toString().length() > 0) {

                for (int index = 0; index < all_items.size(); index++) {
                    Event event = all_items.get(index);
                    String title = event.name;
                    // Add to the filtered result list when our string is found in the event_name
                    if (title.toUpperCase().contains(prefix.toString().toUpperCase()))
                        i.add(event);
                }
                results.values = i;
                results.count = i.size();
            } else {
                // No more filtering, display all items
                synchronized (all_items) {
                    results.values = all_items;
                    results.count = all_items.size();
                }
            }

            return results;
        }
    }
}
