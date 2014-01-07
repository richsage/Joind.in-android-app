package in.joind;

/*
 * Main activity. Displays all events and let the user select one.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.SearchManager;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

public class Main extends JIActivity implements SearchView.OnQueryTextListener {

    private String currentTab = "hot";                   // Current selected tab

    // Constants for dynamically added menu items
    private static final int MENU_SORT_DATE = 1;
    private static final int MENU_SORT_TITLE = 2;

    private FragmentTabHost tabHost;

    /**
     * Called when the activity is first created.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set 'main' layout
        setContentView(R.layout.main);

        // Create instance of the database singleton. This needs a context
        DataHelper.createInstance(this.getApplicationContext());

        initialiseTabs();
    }


    protected void initialiseTabs()
    {
        tabHost = (FragmentTabHost) findViewById(R.id.tabHost);
        tabHost.setup(this, getSupportFragmentManager(), R.id.tabContent);
        tabHost.addTab(tabHost.newTabSpec("hot").setIndicator("Hot"), EventListFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("upcoming").setIndicator("Upcoming"), EventListFragment.class, null);
        tabHost.addTab(tabHost.newTabSpec("past").setIndicator("Past"), EventListFragment.class, null);
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                SharedPreferences sp = getSharedPreferences(JIActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                sp.edit().putString("currentTab", tabId).commit();
                currentTab = tabId;
            }
        });
    }

    // Will reload events. Needed when we return to the screen.
    public void onResume() {
        super.onResume();

        SharedPreferences sp = getSharedPreferences(JIActivity.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if (sp.contains("currentTab")) {
            currentTab = sp.getString("currentTab", "hot");
            tabHost.setCurrentTabByTag(currentTab);
        } else {
            currentTab = sp.getString("defaultEventTab", "hot");
            tabHost.setCurrentTabByTag(currentTab);
        }
    }

    // Overriding the JIActivity add sort-items
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem menu_date = menu.add(0, MENU_SORT_DATE, 0, R.string.OptionMenuSortDate);
        menu_date.setIcon(android.R.drawable.ic_menu_month);
        MenuItem menu_title = menu.add(0, MENU_SORT_TITLE, 0, R.string.OptionMenuSortTitle);
        menu_title.setIcon(android.R.drawable.ic_menu_sort_alphabetically);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
        }

        return true;
    }


    // Overriding the JIActivity handler to handle the sorting
    public boolean onOptionsItemSelected(MenuItem item) {
        EventListFragment fragment = (EventListFragment) getSupportFragmentManager().findFragmentByTag(currentTab);
        int currentEventSortOrder;
        int newEventSortOrder;

        switch (item.getItemId()) {
            case MENU_SORT_DATE:
                currentEventSortOrder = fragment.getEventSortOrder();
                newEventSortOrder = (currentEventSortOrder == DataHelper.ORDER_DATE_ASC ? DataHelper.ORDER_DATE_DESC : DataHelper.ORDER_DATE_ASC);
                fragment.setEventSortOrder(newEventSortOrder);
                break;
            case MENU_SORT_TITLE:
                currentEventSortOrder = fragment.getEventSortOrder();
                newEventSortOrder = (currentEventSortOrder == DataHelper.ORDER_TITLE_ASC ? DataHelper.ORDER_TITLE_DESC : DataHelper.ORDER_TITLE_ASC);
                fragment.setEventSortOrder(newEventSortOrder);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    // Converts input stream to a string.
    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8192);
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            // ignored
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignored
            }
        }
        return sb.toString();
    }

    public void setEventsCountTitle(int eventCount) {
        String subTitle;
        if (eventCount == 1) {
            subTitle = String.format(getString(R.string.generalEventCountSingular), eventCount);
        } else {
            subTitle = String.format(getString(R.string.generalEventCountPlural), eventCount);
        }
        getSupportActionBar().setSubtitle(subTitle);
    }

    public void setEventsTitle(String title, int count) {
        // Set main title to event category plus the number of events found
        setTabTitle(title, count);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        EventListFragment fragment = (EventListFragment) getSupportFragmentManager().findFragmentByTag(currentTab);
        fragment.filterByString(s);

        return false;
    }

    protected void setTabTitle(String title, int eventCount) {
        getSupportActionBar().setTitle(title);
        setEventsCountTitle(eventCount);
    }
}
