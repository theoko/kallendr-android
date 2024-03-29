package com.kallendr.android.ui.calendar;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.kallendr.android.R;
import com.kallendr.android.data.Database;
import com.kallendr.android.data.adapters.EventAdapter;
import com.kallendr.android.data.adapters.TeamAdapter;
import com.kallendr.android.data.model.Event;
import com.kallendr.android.data.model.LocalEvent;
import com.kallendr.android.data.model.Team;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.Helpers;
import com.kallendr.android.helpers.LocalEventGetter;
import com.kallendr.android.helpers.Navigation;
import com.kallendr.android.helpers.UIHelpers;
import com.kallendr.android.helpers.interfaces.FirstLoginCallback;
import com.kallendr.android.helpers.interfaces.Result;
import com.kallendr.android.services.InitialEventUploadService;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.kallendr.android.helpers.Constants.DEBUG_MODE;

public class CalendarMainActivity extends AppCompatActivity {

    /**
     * Calendar setup
     */
    private TextView setup_header;
    private TextView setup_description;

    private LinearLayout buttons_calendar_link_layout;
    private LinearLayout events_layout;
    private Button btn_outlook_link;
    private Button btn_google_link;
    private Button btn_phone_calendar;
    private Button btn_continue_with_local_calendar;
    private Button btn_link_calendar_later;

    private ListView initialLocalEventsList;
    private EventAdapter initialEventAdapter;
    private ArrayList<Event> initialLocalEventItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loader);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // First login
        checkIfFirstLogin();
    }

    private void checkIfFirstLogin() {
        Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
        if (DEBUG_MODE) {
            Log.d(getClass().getName(), "Auth type: " + accountType);
            if (accountType == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
                Log.d(getClass().getName(), "UID: " + FirebaseAuth.getInstance().getCurrentUser().getUid());
            } else if (accountType == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
                Log.d(getClass().getName(), "UID: " + account.getIdToken());
            }
        }
        if  (DEBUG_MODE)
            Log.d(getClass().getName(), "calling firstLogin() at 89");
        Database.getInstance(accountType).firstLogin(getApplicationContext(), new FirstLoginCallback() {
            @Override
            public void onFirstLogin(boolean firstLogin) {
                Helpers.checkPermissionsAndRequestIfNeeded(CalendarMainActivity.this);
                if (firstLogin) {
                    if  (DEBUG_MODE)
                        Log.d(getClass().getName(), "firstLogin(): true, calling showLinkCalendar()");
                    showLinkCalendar();
                } else {
                    if  (DEBUG_MODE)
                        Log.d(getClass().getName(), "firstLogin(): false, calling showMainCalendar(false)");
                    showMainCalendar(false);
                }
            }
        });
    }

    private void showLinkCalendar() {
        setContentView(R.layout.link_calendar);

        setup_header = findViewById(R.id.setup_header);
        setup_description = findViewById(R.id.setup_description);

        buttons_calendar_link_layout = findViewById(R.id.buttons_calendar_link_layout);
        events_layout = findViewById(R.id.events_layout);
        btn_outlook_link = findViewById(R.id.btn_outlook_link);
        btn_google_link = findViewById(R.id.btn_google_link);
        btn_phone_calendar = findViewById(R.id.btn_phone_calendar);
        btn_continue_with_local_calendar = findViewById(R.id.btn_continue_with_local_calendar);
        btn_link_calendar_later = findViewById(R.id.btn_link_calendar_later);
        initialLocalEventsList = findViewById(R.id.eventList);
        initialLocalEventItems = new ArrayList<>();

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
        btn_continue_with_local_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainCalendar(true);
            }
        });
        btn_link_calendar_later.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainCalendar(true);
            }
        });
    }

    /**
     * This method is called after the user has completed the calendar setup.
     * It finds if a user belongs to a team and lets them choose from which team to pull events from.
     * If the user belongs to one team only, it selects that team by default.
     *
     * @param firstLogin
     */
    private void showMainCalendar(final boolean firstLogin) {
        if (firstLogin) {
            if (DEBUG_MODE)
                System.out.println("Calling onCalendarSetupComplete()");
            Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
            Database.getInstance(accountType).onCalendarSetupComplete(getApplicationContext());
        }
        // We should check if the user belongs to many teams.
        // In case they do, let them choose which team they would like to pull events from.
        Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
        /* THIS CALLBACK NEVER GETS BACK IF A USER DOES NOT BELONG TO ANY TEAM */
        Database.getInstance(accountType).getTeamStatus(getApplicationContext(), new Result<List<Team>>() {
            @Override
            public void success(List<Team> teamList) {
                if (teamList.size() > 1) {
                    if (DEBUG_MODE)
                        Log.d(getClass().getName(), "showMainCalendar(): team size() > 1");
                    // The user should choose the team they wish to sign in to
                    chooseTeam(teamList);
                } else {
                    if (DEBUG_MODE)
                        Log.d(getClass().getName(), "set selected team...");
                    // Set selected team
                    String teamID = teamList.get(0).getTeamID();
                    Prefs.putString(Constants.selectedTeam, teamID);
                    showCalendar();
                }
            }

            @Override
            public void fail(List<Team> arg) {
                // Display error to user
                if (DEBUG_MODE)
                    Log.e(getClass().getName(), "Failed to get team status!");
            }
        });
    }

    /**
     * This method is called whenever a user belongs to more than one teams
     *
     * @param teamList
     */
    private void chooseTeam(List<Team> teamList) {
        final String selectedTeam = Prefs.getString(Constants.selectedTeam, null);
        if (selectedTeam == null) {
            setContentView(R.layout.team_chooser);
            final ListView teamChooseList = findViewById(R.id.teamChooseList);
            TeamAdapter teamAdapter = new TeamAdapter(
                    CalendarMainActivity.this,
                    new ArrayList<>(teamList)
            );
            teamChooseList.setAdapter(teamAdapter);
            teamChooseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Team selectedTeam = (Team) teamChooseList.getItemAtPosition(position);
                    if (DEBUG_MODE)
                        System.out.println("selected team: " + selectedTeam.getTeamName());
                    // Set selected team
                    Prefs.putString(Constants.selectedTeam, selectedTeam.getTeamID());
                    showCalendar();
                }
            });
        } else {
            showCalendar();
        }
    }

    /**
     * Finally, this method will switch to the calendar activity
     */
    private void showCalendar() {
        if (DEBUG_MODE)
            System.out.println("Setting view to calendar");
        startActivity(new Intent(CalendarMainActivity.this, CalendarActivity.class));
        finish();
    }

    private void readLocalCalendar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<LocalEvent> events = LocalEventGetter.readCalendarEvent(CalendarMainActivity.this);
                // TODO: show error reading calendar here, not permission denied error!
                if (events == null) {
                    readPermissionDenied();
                } else {
                    if (events.size() > 0)
                        displayCollectedEventsMsg(events);
                    else
                        displayOtherCalendarOptions();
                }
            }
        }).run();
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
        for (LocalEvent localEvent : eventList) {
            Event event = new Event();
            event.setTitleOfEvent(localEvent.getName(), new Date(localEvent.getStartDate()));
            event.setDescription(localEvent.getDescription());
            initialLocalEventItems.add(
                    event
            );
        }
        initialEventAdapter = new EventAdapter(CalendarMainActivity.this, initialLocalEventItems);
        initialLocalEventsList.setAdapter(initialEventAdapter);

        setup_header.setText("We found " + totalEvents + " events!");
        setup_description.setText("You can link more calendars later in the settings page");
        events_layout.setVisibility(View.VISIBLE);

        // Start InitialEventUploadService
        Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
        Database.getInstance(accountType).localEventsList = eventList;
        Intent eventUploadServiceIntent = new Intent(CalendarMainActivity.this, InitialEventUploadService.class);
        startService(eventUploadServiceIntent);
    }

    // TODO: Fix this
    private void displayOtherCalendarOptions() {
        setup_header.setText("We did not find any events on your phone calendar");
        btn_phone_calendar.setVisibility(View.GONE);
        btn_link_calendar_later.setVisibility(View.VISIBLE);
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
                    // Check for first login
                    Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
                    if  (DEBUG_MODE)
                        Log.d(getClass().getName(), "calling firstLogin() at 296");
                    Database.getInstance(accountType).firstLogin(getApplicationContext(), new FirstLoginCallback() {
                        @Override
                        public void onFirstLogin(boolean firstLogin) {
                            if (firstLogin) {
                                readLocalCalendar();
                            } else {
                                showMainCalendar(false);
                            }
                        }
                    });
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
        // TODO: check if setup is complete first. Otherwise, this will cause the app to crash.
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Navigation.backPressed(CalendarMainActivity.this, drawer);
    }

    /**
     * Options menu
     */
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calendar_main, menu);
        return true;
    }*/

    /*@Override
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
    }*/

}
