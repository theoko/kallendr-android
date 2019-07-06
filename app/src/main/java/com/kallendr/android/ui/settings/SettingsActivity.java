package com.kallendr.android.ui.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.kallendr.android.R;
import com.kallendr.android.data.Database;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.Navigation;
import com.kallendr.android.helpers.interfaces.PrefMapCallback;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private Switch notifications_switch;
    private Switch calendar_switch;

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
        /**
         * Get preferences from the server
         */
        Database.getInstance().getPreferences(new PrefMapCallback() {
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
                Database.getInstance().setPreferences(prefsMap);
            }
        });
        calendar_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Map<String, Boolean> prefsMap = new HashMap<>();
                prefsMap.put(Constants.allowCalendarAccess, isChecked);
                Database.getInstance().setPreferences(prefsMap);
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
}
