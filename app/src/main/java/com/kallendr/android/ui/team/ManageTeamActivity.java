package com.kallendr.android.ui.team;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kallendr.android.R;
import com.kallendr.android.data.Database;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.Helpers;
import com.kallendr.android.helpers.Navigation;
import com.kallendr.android.helpers.UIHelpers;
import com.kallendr.android.helpers.interfaces.Result;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.List;

public class ManageTeamActivity extends AppCompatActivity {

    // Layouts
    private LinearLayout inviteMemberLayout;

    // Forms
    private EditText invitedUserEmailAddress;

    // Buttons
    private Button btnAddTeamMember;

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
        Navigation.populateNav(getApplicationContext(), fullNameTextView, emailTextView);

        inviteMemberLayout = findViewById(R.id.inviteMemberLayout);
        invitedUserEmailAddress = findViewById(R.id.invitedUserEmailAddress);
        btnAddTeamMember = findViewById(R.id.btnAddTeamMember);
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
        teamMembersListView.setAdapter(membersAdapter);
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
        Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
        Database.getInstance(accountType).getTeamMembers(new Result<List<String>>() {
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
        Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
        Database.getInstance(accountType).getInvitedUsers(new Result<List<String>>() {
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

    public void btn_addMember(View view) {
        // Validate email if the invitation form is visible
        if (inviteMemberLayout.getVisibility() == View.VISIBLE) {
            String userEmail = invitedUserEmailAddress.getText().toString();
            boolean emailValid = Helpers.isEmailValid(userEmail);
            if (emailValid) {
                Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
                Database.getInstance(accountType).invite(userEmail);
                Toast.makeText(ManageTeamActivity.this, "User invited!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(ManageTeamActivity.this, "Please enter a valid email address", Toast.LENGTH_LONG).show();
            }
        } else {
            // Update button text
            final String teamID = Prefs.getString(Constants.selectedTeam, null);
            if (teamID != null) {
                Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
                Database.getInstance(accountType).getTeamNameByID(teamID, new Result<String>() {
                    @Override
                    public void success(String teamName) {
                        btnAddTeamMember.setText("Invite to " + teamName);
                    }

                    @Override
                    public void fail(String arg) {
                        btnAddTeamMember.setText("Invite");
                    }
                });
            }
            // Show edittext
            inviteMemberLayout.setVisibility(View.VISIBLE);
            UIHelpers.runFadeInAnimationOn(ManageTeamActivity.this, inviteMemberLayout);
            // Request focus
            invitedUserEmailAddress.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(invitedUserEmailAddress, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
