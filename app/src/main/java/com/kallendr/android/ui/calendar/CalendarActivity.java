package com.kallendr.android.ui.calendar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.kallendr.android.helpers.Helpers;
import com.kallendr.android.helpers.LocalEventGetter;
import com.kallendr.android.helpers.Navigation;
import com.kallendr.android.helpers.UIHelpers;
import com.kallendr.android.helpers.interfaces.EventCallback;
import com.kallendr.android.helpers.interfaces.Result;
import com.kallendr.android.services.InitialEventUploadService;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {

    /**
     * Main app functionality
     */
    private CalendarView mainCalendarView;
    private ArrayList<Event> listViewItems;
    private EventAdapter eventAdapterForDay;
    private ListView listViewForDay;
    private LinearLayout events_loader_layout;
    private LinearLayout no_events_msg_layout;
    private TextView txtViewForDay;
    private Button btnScheduleMeeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        listViewForDay = findViewById(R.id.eventListForDay);
        events_loader_layout = findViewById(R.id.events_loader_layout);
        no_events_msg_layout = findViewById(R.id.no_events_msg_layout);
        txtViewForDay = findViewById(R.id.txtViewForDay);
        btnScheduleMeeting = findViewById(R.id.btnScheduleMeeting);
        listViewItems = new ArrayList<>();
        eventAdapterForDay = new EventAdapter(
                CalendarActivity.this,
                listViewItems
        );
        listViewForDay.setAdapter(eventAdapterForDay);

        // Set text for currently authenticated user
        View headerView = navigationView.getHeaderView(0);
        TextView fullNameTextView = headerView.findViewById(R.id.userFullName);
        TextView emailTextView = headerView.findViewById(R.id.userEmail);
        Navigation.populateNav(getApplicationContext(), fullNameTextView, emailTextView);

        /*
         * Move to meeting setup screen
         */
        btnScheduleMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newMeetingIntent = new Intent(CalendarActivity.this, NewMeetingScheduleActivity.class);
                // TODO: switch to startActivityForResult
//                newMeetingIntent.putExtra()
                startActivity(newMeetingIntent);
            }
        });

        /*
         * Display events
         */
        mainCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // List events for selected day
                if (Constants.DEBUG_MODE) {
                    System.out.println("Changed date to: " + year + "/" + month + "/" + dayOfMonth);
                }
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                populateDayWithEvents(calendar.getTime());
            }
        });

        /**
         * List team events for current day
         */
        long date = mainCalendarView.getDate();
        Date currDate = new Date(date);
        populateDayWithEvents(currDate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        detectEventsOnLocalCalendar();
    }

    List<LocalEvent> localEvents;
    List<LocalEvent> toUpload;
    int[] completedChecks = {0};
    private void detectEventsOnLocalCalendar() {
        localEvents = LocalEventGetter.readCalendarEvent(getApplicationContext());
        toUpload = new ArrayList<>();
        if (localEvents != null) {
            if (localEvents.size() > 0) {
                // Check which events are new
                for (final LocalEvent localEvent : localEvents) {
                    Database.getInstance().getEventInfo(
                            CalendarActivity.this,
                            localEvent.getStartDate(),
                            localEvent.getEndDate(),
                            new Result<LocalEvent>() {
                                @Override
                                public void success(LocalEvent arg) {
                                    // Event already exists in Firebase DB
                                    completedChecks[0]++;
                                    askToSyncEvents();
                                }

                                @Override
                                public void fail(LocalEvent arg) {
                                    toUpload.add(localEvent);
                                    completedChecks[0]++;
                                    askToSyncEvents();
                                }
                            }
                    );
                }
            }
        }
    }

    private void askToSyncEvents() {
        if (completedChecks[0] == localEvents.size() && toUpload.size() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
            // Set title
            builder.setTitle("We found " + toUpload.size() + " new events!");
            // Add the buttons
            builder.setPositiveButton("Upload", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    Database.getInstance().localEventsList = toUpload;
                    Intent eventUploadServiceIntent = new Intent(CalendarActivity.this, InitialEventUploadService.class);
                    startService(eventUploadServiceIntent);
                }
            });
            builder.setNegativeButton("Don't upload", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });
            // Create the AlertDialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void populateDayWithEvents(Date currDate) {
        if (no_events_msg_layout.getVisibility() == View.VISIBLE) {
            no_events_msg_layout.setVisibility(View.GONE);
        }
        no_events_msg_layout.setVisibility(View.GONE);
        events_loader_layout.setVisibility(View.VISIBLE);
        final long[] times = Helpers.generateStartAndEndMillis(currDate);
        Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
        Database.getInstance(accountType).getTeamEvents(
                times[0],
                times[1],
                new EventCallback() {
                    @Override
                    public void onSuccess(List<LocalEvent> eventList) {
                        Log.d(getClass().getName(), "onSuccess() called!");
                        // Update UI
                        if (no_events_msg_layout.getVisibility() == View.VISIBLE) {
                            no_events_msg_layout.setVisibility(View.GONE);
                        }
                        listViewItems.clear();
                        for (LocalEvent localEvent : eventList) {
                            Event event = new Event();
                            event.setTitleOfEvent(localEvent.getName(), new Date(localEvent.getStartDate()));
                            event.setDescription(localEvent.getDescription());
                            listViewItems.add(event);
                        }
                        if (Constants.DEBUG_MODE) {
                            System.out.println("Adapter total events: " + eventAdapterForDay.getCount());
                        }
                        eventAdapterForDay.notifyDataSetChanged();
                        events_loader_layout.setVisibility(View.GONE);
                        String no_events = getString(R.string.no_events);
                        txtViewForDay.setText(no_events + " " + UIHelpers.getReadableDateForMillis(times[0]));
                        if (eventList.isEmpty()) {
                            no_events_msg_layout.setVisibility(View.VISIBLE);
                        } else {
                            no_events_msg_layout.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFail(String message) {
                        // Show message to user
                        Log.d(getClass().getName(), "onFail() called!");
                        String no_events = getString(R.string.no_events);
                        txtViewForDay.setText(no_events + " " + UIHelpers.getReadableDateForMillis(times[0]));
                        events_loader_layout.setVisibility(View.GONE);
                        no_events_msg_layout.setVisibility(View.VISIBLE);
                    }
                }
        );
    }
}
