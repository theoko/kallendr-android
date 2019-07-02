package com.kallendr.android.ui.calendar;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.kallendr.android.R;
import com.kallendr.android.data.Database;
import com.kallendr.android.data.adapters.EventAdapter;
import com.kallendr.android.data.model.Event;
import com.kallendr.android.data.model.LocalEvent;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.FirstLoginCallback;
import com.kallendr.android.helpers.Helpers;
import com.kallendr.android.helpers.LocalEventGetter;
import com.kallendr.android.helpers.UIHelpers;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarMainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Calendar setup
     */
    private TextView setup_header;
    private TextView setup_description;

    private LinearLayout buttons_calendar_link_layout;
    private Button btn_outlook_link;
    private Button btn_google_link;
    private Button btn_phone_calendar;

    /**
     * Main app functionality
     */
    private EventAdapter eventAdapter;
    private ArrayList<Event> listViewItems;
    private CalendarView calendarView;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loader);

        // First login
        checkIfFirstLogin();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkIfFirstLogin() {
        Database.getInstance().firstLogin(new FirstLoginCallback() {
            @Override
            public void onFirstLogin(boolean firstLogin) {
                Helpers.checkPermissionsAndRequestIfNeeded(CalendarMainActivity.this);
                if (firstLogin) {
                    setContentView(R.layout.link_calendar);

                    setup_header = findViewById(R.id.setup_header);
                    setup_description = findViewById(R.id.setup_description);

                    buttons_calendar_link_layout = findViewById(R.id.buttons_calendar_link_layout);
                    btn_outlook_link = findViewById(R.id.btn_outlook_link);
                    btn_google_link = findViewById(R.id.btn_google_link);
                    btn_phone_calendar = findViewById(R.id.btn_phone_calendar);

                    btn_outlook_link.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                    btn_google_link.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                    btn_phone_calendar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            readLocalCalendar();
                        }
                    });
                } else {
                    setContentView(R.layout.activity_calendar_main);
                    setTitle("Kallendr");
                    Toolbar toolbar = findViewById(R.id.toolbar);
                    setSupportActionBar(toolbar);
                    FloatingActionButton fab = findViewById(R.id.fab);
                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                    });
                    DrawerLayout drawer = findViewById(R.id.drawer_layout);
                    NavigationView navigationView = findViewById(R.id.nav_view);
                    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                            CalendarMainActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                    drawer.addDrawerListener(toggle);
                    toggle.syncState();
                    navigationView.setNavigationItemSelectedListener(CalendarMainActivity.this);

                    calendarView = findViewById(R.id.calendarView);
                    listView = findViewById(R.id.eventList);
                    listViewItems = new ArrayList<>();

                    /*
                     * Add example events
                     */
                    Event event = new Event();
                    event.setTimeOfEvent(new Date(System.currentTimeMillis()));
                    event.setDescription("Test description 123");
                    listViewItems.add(event);

                    event = new Event();
                    event.setTimeOfEvent(new Date(System.currentTimeMillis()));
                    event.setDescription("Test description 1234");
                    listViewItems.add(event);
                    eventAdapter = new EventAdapter(CalendarMainActivity.this, listViewItems);
                    listView.setAdapter(eventAdapter);

                    calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
                        @Override
                        public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                            // List events for selected day
                            Date currDate = new Date(view.getDate());
                            Event sample = new Event();
                            sample.setTimeOfEvent(currDate);
                            sample.setDescription("Test description 1234");
                            listViewItems.add(sample);
                            eventAdapter.notifyDataSetChanged();
                        }
                    });

                    // List events for current day
                    long date = calendarView.getDate();
                    Date currDate = new Date(date);
                }
            }
        });
    }

    private void readLocalCalendar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<LocalEvent> events = LocalEventGetter.readCalendarEvent(CalendarMainActivity.this);
                if (events == null) {
                    readPermissionDenied();
                } else {
                    if (events.size() > 0) {
                        displayCollectedEventsMsg(events);
                    } else {
                        displayOtherCalendarOptions();
                    }
                }
            }
        }).run();

        /*for (LocalEvent localEvent : events) {
            System.out.println("NAME OF EVENT: " + localEvent.getName());
            System.out.println("START: " + localEvent.getStartDate());
            System.out.println("START: " + localEvent.getEndDate());
            System.out.println("DESCRIPTION: " + localEvent.getDescription());
        }*/
    }

    private void readPermissionDenied() {
        Intent intent = new Intent(CalendarMainActivity.this, PermissionRequestActivity.class);
        startActivity(intent);
        finish();
    }

    private void displayCollectedEventsMsg(List<LocalEvent> eventList) {
        UIHelpers.runFadeOutAnimationOn(CalendarMainActivity.this, buttons_calendar_link_layout);
        buttons_calendar_link_layout.setVisibility(View.GONE);

        int totalEvents = eventList.size();
        setup_header.setText("We found " + totalEvents + " events!");
        setup_description.setText("You can link more calendars later in the settings page");
    }

    private void displayOtherCalendarOptions() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_READ_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    readLocalCalendar();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    readPermissionDenied();
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calendar_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
