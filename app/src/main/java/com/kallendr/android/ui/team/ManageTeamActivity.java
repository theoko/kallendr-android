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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.kallendr.android.R;
import com.kallendr.android.data.Database;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.Navigation;
import com.kallendr.android.helpers.interfaces.Result;

import java.util.ArrayList;
import java.util.List;

public class ManageTeamActivity extends AppCompatActivity {

    // ListView
    private ListView teamMembersListView;
    private ListView invitedUsersListView;

    // List
    ArrayList<String> membersList;
    ArrayList<String> invitedUserList;

    // Adapters
    ArrayAdapter<String> membersAdapter;
    ArrayAdapter<String> invitedUsersAdapter;

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
        invitedUserList = new ArrayList<>();
        membersList = new ArrayList<>();
        membersAdapter = new ArrayAdapter<>(
                ManageTeamActivity.this,
                android.R.layout.simple_list_item_1,
                membersList
        );
        invitedUsersAdapter = new ArrayAdapter<>(
                ManageTeamActivity.this,
                android.R.layout.simple_list_item_1,
                invitedUserList
        );
        invitedUsersListView.setAdapter(invitedUsersAdapter);
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
        Database.getInstance().getTeamMembers(new Result<List<String>>() {
            @Override
            public void success(List<String> arg) {
                if (arg.size() > 0) {
                    membersList.clear();
                    membersList.addAll(arg);
                    membersAdapter.notifyDataSetChanged();
                } else {

                }
            }

            @Override
            public void fail(List<String> arg) {

            }
        });
    }

    private void displayInvitedUsers() {
        Database.getInstance().getInvitedUsers(new Result<List<String>>() {
            @Override
            public void success(List<String> arg) {
                if (arg.size() > 0) {
                    // Update UI
                    invitedUserList.clear();
                    invitedUserList.addAll(arg);
                    invitedUsersAdapter.notifyDataSetChanged();
                } else {

                }
            }

            @Override
            public void fail(List<String> arg) {

            }
        });
    }
}
