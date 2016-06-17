package in.joind;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.markupartist.android.widget.PullToRefreshListView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import in.joind.adapter.EventListAdapter;
import in.joind.api.APIService;
import in.joind.fragment.FragmentLifecycle;
import in.joind.fragment.LogInDialogFragment;
import in.joind.model.Event;
import in.joind.model.EventCollectionResponse;
import in.joind.model.Metadata;
import in.joind.user.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The list fragment that is shown in our tabbed view.
 * Lists events depending on event type (in our case, the fragment's Tag value)
 */
public class EventListFragment extends ListFragment implements EventListFragmentInterface, FragmentLifecycle {

    final static public String ARG_LIST_TYPE_KEY = "listType";
    final static public String LIST_TYPE_HOT = "hot";
    final static public String LIST_TYPE_UPCOMING = "upcoming";
    final static public String LIST_TYPE_MY_EVENTS = "my_events";
    final static public String LIST_TYPE_PAST = "past";

    private APIService service;
    private APIEventsCallback callback;
    private Call<EventCollectionResponse<ArrayList<Event>>> currentCall;
    private EventListAdapter m_eventAdapter;
    int eventSortOrder = DataHelper.ORDER_DATE_ASC;

    Main parentActivity;
    ListView listView;
    View emptyView;
    LinearLayout notSignedInView;
    Button signInButton;
    String eventType;

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        android.util.Log.d("JOINDIN", "onAttach called");
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        eventType = args.getString(ARG_LIST_TYPE_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.event_list_fragment, container, false);
        if (viewGroup != null) {
            // Populate our list adapter
            ArrayList<Event> m_events = new ArrayList<>();
            m_eventAdapter = new EventListAdapter(getActivity(), R.layout.eventrow, m_events);
            setListAdapter(m_eventAdapter);

            signInButton = (Button) viewGroup.findViewById(R.id.myEventsSignInButton);
        }

        return viewGroup;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = getListView();
        emptyView = listView.getEmptyView();
        notSignedInView = (LinearLayout) view.findViewById(R.id.notSignedInList);

        setViewVisibility(false, false);
        setupEventHandlers();
    }

    @Override
    public void onActivityCreated(android.os.Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parentActivity = (Main) getActivity();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseLoading();
    }

    public void pauseLoading() {
        listView = null;
        if (parentActivity != null) {
//            parentActivity.displayHorizontalProgress(false);
        }
        if (callback != null) {
            callback.cancel();
        }
    }

    public void onResume() {
        super.onResume();
        eventType = getArguments().getString(ARG_LIST_TYPE_KEY);
        android.util.Log.d("JOINDIN", "onResume called for " + eventType);
        parentActivity = (Main) getActivity();
        service = new APIService(parentActivity);
        callback = new APIEventsCallback();
        listView = getListView();

        if (getUserVisibleHint()) {
            performEventListUpdate();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        android.util.Log.d("JOINDIN", "setUserVisibleHint for " + eventType + ": " + (isVisibleToUser ? "true" : "false"));
        if (isVisibleToUser && parentActivity != null) {
            android.util.Log.d("JOINDIN", "performing event list update for " + eventType);
            performEventListUpdate();
        }
    }

    public void onPauseFragment() {
        pauseLoading();
    }

    public void onResumeFragment() {
    }

    private void setViewVisibility(boolean showList, boolean showNotSignedIn) {
        // List and empty view are opposites
        if (listView != null) {
            listView.setVisibility(showList ? View.VISIBLE : View.GONE);
        }
        if (emptyView != null) {
            emptyView.setVisibility(showList ? View.GONE : View.VISIBLE);
        }

        if (notSignedInView != null) {
            notSignedInView.setVisibility(showNotSignedIn ? View.VISIBLE : View.GONE);
            if (showNotSignedIn && emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        }
    }

    public void performEventListUpdate() {
        // My Events - check our signed-in status
        // We explicitly request a refresh of the authenticated status
        parentActivity = (Main) getActivity();
        boolean isAuthenticated = parentActivity.isAuthenticated(true);
        if (eventType.equals(LIST_TYPE_MY_EVENTS)) {
            if (!isAuthenticated) {
                parentActivity.displayHorizontalProgress(false);
                setViewVisibility(false, true);

                // Not signed in, no need to carry on
                return;
            }

            setViewVisibility(true, false);
        }

        // If we don't have any events in the adapter, then try and load some
        if (m_eventAdapter != null && m_eventAdapter.getCount() == 0) {
            if (listView != null) {
                setViewVisibility(false, false);
            }

            fetchEvents();
        }
    }

    private void fetchEvents() {
        parentActivity.displayHorizontalProgress(true);

        callback.reset();
        HashMap<String, String> params = new HashMap<>();
        params.put("filter", eventType);
        currentCall = service.getAPI().events(params);
        currentCall.enqueue(callback);
    }

    private void fetchEvents(String uri) {
        currentCall = service.getAPI().eventsByUri(uri);
        currentCall.enqueue(callback);
    }

    protected void setupEventHandlers() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Intent myIntent = new Intent();
                myIntent.setClass(getActivity().getApplicationContext(), EventDetail.class);

                // pass the JSON data for specified event to the next activity
                myIntent.putExtra("eventJSON", parent.getAdapter().getItem(pos).toString());
                startActivity(myIntent);
            }
        });
        ((PullToRefreshListView) listView).setOnRefreshListener(new PullToRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchEvents();
            }
        });
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogInDialogFragment dlg = new LogInDialogFragment();
                dlg.show(getChildFragmentManager(), "login");
            }
        });
    }

    /**
     *  Display events by populating the m_eventAdapter (custom list) with items loaded from DB
     */
    public int displayEvents(String eventType) {
        listView = getListView();
        if (listView != null) {
            setViewVisibility(true, false);
        }

        // Clear all events
        m_eventAdapter.clear();

        // add events and return count
        DataHelper dh = DataHelper.getInstance(parentActivity);
        int count = dh.populateEvents(eventType, m_eventAdapter, eventSortOrder);

        // Tell the adapter that our data set has changed so it can update it
        m_eventAdapter.notifyDataSetChanged();
        ((PullToRefreshListView) listView).onRefreshComplete();

        parentActivity.displayHorizontalProgress(false);

        return count;
    }

    public void setEventSortOrder(int sortOrder) {
        eventSortOrder = sortOrder;
        displayEvents(eventType);
    }

    public int getEventSortOrder() {
        return eventSortOrder;
    }

    public void filterByString(CharSequence s) {
        m_eventAdapter.getFilter().filter(s);
    }

    /**
     * Retrofit callback: handle event loading
     */
    private class APIEventsCallback implements Callback<EventCollectionResponse<ArrayList<Event>>> {
        private boolean isFirstLoad = true;
        private boolean isCancelled = false;
        private DataHelper dataHelper;

        public APIEventsCallback() {
            dataHelper = DataHelper.getInstance();
        }

        public void reset() {
            isFirstLoad = true;
            isCancelled = false;
        }

        public void cancel() {
            isCancelled = true;
        }

        @Override
        public void onResponse(Call<EventCollectionResponse<ArrayList<Event>>> call, Response<EventCollectionResponse<ArrayList<Event>>> response) {
            if (isFirstLoad) {
                dataHelper.deleteAllEventsFromType(eventType);
                checkForUserData(response.body().meta);
                isFirstLoad = false;
            }
            String nextPage = response.body().meta.next_page;

            ArrayList<Event> events = response.body().events;
            for (Event event : events) {
                m_eventAdapter.add(event);
            }
            dataHelper.insertEvents(eventType, events);

            if (nextPage == null) {
                displayEvents(eventType);
                return;
            }

            // Don't continue after one round of Hot events
            if (eventType.equals(LIST_TYPE_HOT)) {
                displayEvents(eventType);
                return;
            }

            if (!isCancelled) {
                m_eventAdapter.notifyDataSetChanged();
                fetchEvents(nextPage);
            }
        }

        @Override
        public void onFailure(Call<EventCollectionResponse<ArrayList<Event>>> call, Throwable t) {
            parentActivity.displayHorizontalProgress(false);
            displayEvents(eventType);
            Toast toast = Toast.makeText(parentActivity, getString(R.string.activityMainCouldntLoadEvents), Toast.LENGTH_LONG);
            toast.show();
        }

        private void checkForUserData(Metadata metadata) {
            String userURI = metadata.user_uri;
            if (userURI == null || userURI.length() == 0) {
                return;
            }


            UserManager userManager = new UserManager(getActivity());
            if (!userManager.accountRequiresFurtherDetails()) {
                return;
            }
            userManager.updateSavedUserDetails(userURI);
        }
    }
}
