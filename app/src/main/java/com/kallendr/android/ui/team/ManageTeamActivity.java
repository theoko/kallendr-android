package com.kallendr.android.ui.team;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.kallendr.android.R;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.Navigation;

public class ManageTeamActivity extends AppCompatActivity {

    private ListView teamMembersListView;
    private ListView invitedUsersListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_team);
        setTitle(Constants.APP_NAME);
        Toolbar toolbar = findViewById(R.id.toolbar_manage_team);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout_manage_team);
        NavigationView navigationView = findViewById(R.id.nav_view_manage_team);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                ManageTeamActivity.this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                DrawerLayout drawer = findViewById(R.id.drawer_layout_manage_team);
                Navigation.selectedItem(ManageTeamActivity.this, drawer, menuItem);
                return true;
            }
        });
        // Set text for currently authenticated user
        View headerView = navigationView.getHeaderView(0);
        TextView fullNameTextView = headerView.findViewById(R.id.userFullName);
        TextView emailTextView = headerView.findViewById(R.id.userEmail);
        Navigation.populateNav(fullNameTextView, emailTextView);

        teamMembersListView = findViewById(R.id.teamMembersListView);
        invitedUsersListView = findViewById(R.id.invitedUsersListView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayTeamMembers();
        displayInvitedUsers();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout_manage_team);
        Navigation.backPressed(ManageTeamActivity.this, drawer);
    }

    private void displayTeamMembers() {

    }

    private void displayInvitedUsers() {

    }
}
