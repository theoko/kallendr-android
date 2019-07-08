package com.kallendr.android.ui.calendar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import com.kallendr.android.R;
import com.kallendr.android.data.adapters.EventAdapter;
import com.kallendr.android.data.model.Event;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.Navigation;

import java.util.ArrayList;
import java.util.Date;

public class CalendarActivity extends AppCompatActivity {

    /**
     * Main app functionality
     */
    private EventAdapter eventAdapterForDay;
    private ArrayList<Event> listViewItems;
    private CalendarView mainCalendarView;
    private ListView listViewForDay;

    private ListView initialLocalEventsList;
    private EventAdapter initialEventAdapter;
    private ArrayList<Event> initialLocalEventItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initialLocalEventsList = findViewById(R.id.eventList);
        initialLocalEventItems = new ArrayList<>();

        setContentView(R.layout.activity_calendar_main);
        setTitle(Constants.APP_NAME);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                CalendarActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                Navigation.selectedItem(CalendarActivity.this, drawer, menuItem);
                return true;
            }
        });

        mainCalendarView = findViewById(R.id.calendarView);
        listViewForDay = findViewById(R.id.eventList);
        listViewItems = new ArrayList<>();

        // Set text for currently authenticated user
        View headerView = navigationView.getHeaderView(0);
        TextView fullNameTextView = headerView.findViewById(R.id.userFullName);
        TextView emailTextView = headerView.findViewById(R.id.userEmail);
        Navigation.populateNav(fullNameTextView, emailTextView);

        /*
         * Display events
         */
        mainCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // List events for selected day
                /*Date currDate = new Date(view.getDate());
                            Event sample = new Event();
                            sample.setTimeOfEvent(currDate);
                            sample.setDescription("Test description 1234");
                            listViewItems.add(sample);
                            eventAdapterForDay.notifyDataSetChanged();*/
            }
        });

        // List events for current day
        long date = mainCalendarView.getDate();
        Date currDate = new Date(date);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
