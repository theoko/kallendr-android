package com.kallendr.android.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import com.kallendr.android.R;
import com.kallendr.android.data.Database;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.Navigation;
import com.kallendr.android.helpers.interfaces.PrefMapCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.kallendr.android.helpers.Constants.DEBUG_MODE;

public class SettingsActivity extends AppCompatActivity {

    private Switch notifications_switch;
    private Switch calendar_switch;
    private AppCompatSpinner meeting_duration_spinner;
    private AppCompatSpinner meeting_breaks_spinner;
    private List<String> meetingDurationOptions;
    private List<String> meetingBreaksOptions;
    private ArrayAdapter<String> meetingDurationAdapter;
    private ArrayAdapter<String> meetingBreaksAdapter;

    // Custom times input
    private EditText customMeetingDurationInput;
    private EditText customMeetingBreaksInput;

    private final String CUSTOM_TEXT = "Custom...";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(Constants.APP_NAME);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                SettingsActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                Navigation.selectedItem(SettingsActivity.this, drawer, menuItem);
                return true;
            }
        });
        /**
         * Preference switches
         */
        notifications_switch = findViewById(R.id.notifications_switch);
        calendar_switch = findViewById(R.id.calendar_switch);
        meeting_duration_spinner = findViewById(R.id.meeting_duration_spinner);
        meeting_breaks_spinner = findViewById(R.id.meeting_breaks_spinner);

        customMeetingDurationInput = findViewById(R.id.customMeetingDurationInput);
        customMeetingBreaksInput = findViewById(R.id.customMeetingBreaksInput);

        meetingDurationOptions = new ArrayList<>();
        meetingBreaksOptions = new ArrayList<>();
        // Add duration options
        for (int i = 0; i < 60; i = i + 5) {
            if (i > 0)
                meetingDurationOptions.add(String.valueOf(i));
        }
        meetingDurationOptions.add(CUSTOM_TEXT);
        for (int i = 0; i < 60; i = i + 5) {
            if (i > 0)
                meetingBreaksOptions.add(String.valueOf(i));
        }
        meetingBreaksOptions.add(CUSTOM_TEXT);
        meetingDurationAdapter = new ArrayAdapter<>(
                SettingsActivity.this,
                R.layout.spinner_item,
                meetingDurationOptions
        );
        meetingBreaksAdapter = new ArrayAdapter<>(
                SettingsActivity.this,
                R.layout.spinner_item,
                meetingBreaksOptions
        );
        meeting_duration_spinner.setAdapter(meetingDurationAdapter);
        meeting_breaks_spinner.setAdapter(meetingBreaksAdapter);
        // Set listeners
        meeting_duration_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = meetingDurationOptions.get(position);
                if (DEBUG_MODE) {
                    Log.d(getClass().getName(), "Selected " + selectedItem);
                }
                // Get selected item
                if (selectedItem.equals(CUSTOM_TEXT)) {
                    showCustomInputForMeetingDuration();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (DEBUG_MODE) {
                    Log.d(getClass().getName(), "Nothing selected");
                }
            }
        });
        meeting_breaks_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = meetingBreaksOptions.get(position);
                if (DEBUG_MODE) {
                    Log.d(getClass().getName(), "Selected " + meetingBreaksOptions.get(position));
                }
                if (selectedItem.equals(CUSTOM_TEXT)) {
                    showCustomInputForMeetingBreaks();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (DEBUG_MODE) {
                    Log.d(getClass().getName(), "Nothing selected");
                }
            }
        });
        /**
         * Get preferences from the server
         */
        Database.getInstance().getPreferences(getApplicationContext(), new PrefMapCallback() {
            @Override
            public void onSuccess(Map<String, Boolean> prefMap) {
                if (prefMap.get(Constants.allowNotifications) != null) {
                    notifications_switch.setChecked(
                            prefMap.get(Constants.allowNotifications)
                    );
                }
                if (prefMap.get(Constants.allowCalendarAccess) != null) {
                    calendar_switch.setChecked(
                            prefMap.get(Constants.allowCalendarAccess)
                    );
                }
            }

            @Override
            public void onFail(String message) {

            }
        });
        /**
         * Update preferences on the server with a listener
         */
        notifications_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map<String, Boolean> prefsMap = new HashMap<>();
                prefsMap.put(Constants.allowNotifications, isChecked);
                Database.getInstance().setPreferences(getApplicationContext(), prefsMap);
            }
        });
        calendar_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map<String, Boolean> prefsMap = new HashMap<>();
                prefsMap.put(Constants.allowCalendarAccess, isChecked);
                Database.getInstance().setPreferences(getApplicationContext(), prefsMap);
            }
        });
        // Set text for currently authenticated user
        View headerView = navigationView.getHeaderView(0);
        TextView fullNameTextView = headerView.findViewById(R.id.userFullName);
        TextView emailTextView = headerView.findViewById(R.id.userEmail);
        Navigation.populateNav(fullNameTextView, emailTextView);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Navigation.backPressed(SettingsActivity.this, drawer);
    }

    private void showCustomInputForMeetingDuration() {
        customMeetingDurationInput.setVisibility(View.VISIBLE);
        meeting_duration_spinner.setVisibility(View.GONE);
        // Request focus
        customMeetingDurationInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(customMeetingDurationInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void showCustomInputForMeetingBreaks() {
        customMeetingBreaksInput.setVisibility(View.VISIBLE);
        meeting_breaks_spinner.setVisibility(View.GONE);
        // Request focus
        customMeetingBreaksInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(customMeetingBreaksInput, InputMethodManager.SHOW_IMPLICIT);
    }
}
