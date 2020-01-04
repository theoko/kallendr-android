package com.kallendr.android.ui.calendar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kallendr.android.R;
import com.kallendr.android.data.Database;
import com.kallendr.android.data.adapters.AvailabilityListAdapter;
import com.kallendr.android.data.model.AvailableGroupMember;
import com.kallendr.android.data.observers.AvailableGroupMemberObserver;
import com.kallendr.android.helpers.Helpers;
import com.kallendr.android.helpers.ObjectSerializer;
import com.kallendr.android.helpers.interfaces.Result;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.kallendr.android.helpers.Constants.DEBUG_MODE;
import static com.kallendr.android.helpers.Constants.meeting_breaks;
import static com.kallendr.android.helpers.Constants.meeting_duration;
import static com.kallendr.android.helpers.Constants.meeting_participants;
import static com.kallendr.android.helpers.Constants.meeting_start_time;

public class NewMeetingScheduleActivity extends AppCompatActivity {

    private int meetingDuration;
    private int meetingBreaksDuration;
    private DateTime dateSelected;
    private DateTime meetingStart;
    private DateTime meetingEnd;

    private ProgressBar availabilityProgressBar;
    private TextView readableDateHeader;
    private LinearLayout availabilityLinearLayout;
    private LinearLayout loadingAvailabilityLayout;
    private ListView availableMembersList;
    private Button btnScheduleMeeting;

    // Meeting info
    private ArrayList<AvailableGroupMember> availableGroupMembersList;
    private int totalTeamMembers = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meeting_schedule);

        availabilityProgressBar = findViewById(R.id.availabilityProgressBar);
        readableDateHeader = findViewById(R.id.readableDateHeader);
        availabilityLinearLayout = findViewById(R.id.availabilityLinearLayout);
        loadingAvailabilityLayout = findViewById(R.id.loadingAvailabilityLayout);
        availableMembersList = findViewById(R.id.availableMembersList);
        btnScheduleMeeting = findViewById(R.id.btnAvailabilityScheduleMeeting);

        btnScheduleMeeting.setEnabled(false);

        btnScheduleMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Schedule a team meeting
                // Generate meeting ID
                // teams/teamID/meetings/meetingID: start_at, end_at
                if (availableGroupMembersList.size() < totalTeamMembers) {
                    // if a user is not available, ask for confirmation and provide re-scheduling option
                    new AlertDialog.Builder(getApplicationContext())
                            .setTitle("Schedule meeting?")
                            .setMessage("Not all group members are available. Are you sure you want to schedule a meeting?")

                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Continue with delete operation
                                    scheduleMeeting();
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                } else {
                    scheduleMeeting();
                }
            }
        });
    }

    private void scheduleMeeting() {
        // Schedule meeting and return to home screen
        Intent newMeetingConfigurationIntent = new Intent(getApplicationContext(), NewMeetingConfigurationActivity.class);
//            String availableGroupMembersListSerialized = ObjectSerializer.serialize(availableGroupMembersList);
        ArrayList<String> memberEmails = new ArrayList<>();
        for (AvailableGroupMember member : availableGroupMembersList) {
            String email = member.getEmail();
            memberEmails.add(email);
        }
        newMeetingConfigurationIntent.putExtra(meeting_start_time, meetingStart.getMillis());
        newMeetingConfigurationIntent.putExtra(meeting_duration, meetingDuration);
        newMeetingConfigurationIntent.putExtra(meeting_participants, memberEmails);
        newMeetingConfigurationIntent.putExtra(meeting_breaks, meetingBreaksDuration);
        startActivity(newMeetingConfigurationIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get availability for team members
        getSettings();
        getDate();
        getTeamMembers();
    }

    private void getSettings() {
        // Dummy values (for now)
        meetingDuration = 30;
        meetingBreaksDuration = 10;
    }

    private void getDate() {
        dateSelected = new DateTime().plusMinutes(meetingBreaksDuration);
        meetingStart = dateSelected;
        meetingEnd = dateSelected.plusMinutes(meetingBreaksDuration + meetingDuration);

        // Update UI
        readableDateHeader.setText(
                Helpers.readableDate(meetingStart)
        );
    }

    private void getTeamMembers() {
        Database.getInstance().getTeamMembers(new Result<List<String>>() {
            @Override
            public void success(List<String> arg) {
                if (DEBUG_MODE) {
                    Log.d(getClass().getName(), arg.toString());
                }
                totalTeamMembers = arg.size();
                getAvailability(arg);
            }

            @Override
            public void fail(List<String> arg) {
                Intent calendarMainIntent = new Intent(NewMeetingScheduleActivity.this, CalendarMainActivity.class);
                startActivity(calendarMainIntent);
                finish();
            }
        });
    }

    private void getAvailability(final List<String> emails) {
        final ArrayList<AvailableGroupMember> members = new ArrayList<>();
        for (int i=0; i<emails.size(); i++) {
            final AvailableGroupMember groupMember = new AvailableGroupMember(
                    emails.get(i),
                    meetingStart.getMillis(),
                    meetingEnd.getMillis()
            );
            final int finalI = i;
            new AvailableGroupMemberObserver(
                    groupMember,
                    new Result<Boolean>() {
                        @Override
                        public void success(Boolean arg) {
                            if (DEBUG_MODE) {
                                Log.d(getClass().getName(), "Available: " + arg);
                            }
                            /*boolean found = false;
                            for (int i=0; i<members.size(); i++) {
                                if (members.get(i).equals(groupMember))
                                    found = true;
                            }
                            if (!found) {
                                members.add(groupMember);
                            }*/
                            members.add(groupMember);
                            if (finalI == emails.size() - 1) {
                                updateListUI(members);
                                availableGroupMembersList = members;
                            }
                        }

                        @Override
                        public void fail(Boolean arg) {
                            members.add(groupMember);
                            if (finalI == emails.size() - 1) {
                                updateListUI(members);
                                availableGroupMembersList = members;
                            }
                        }
                    }
            );
        }
    }

    private void updateListUI(ArrayList<AvailableGroupMember> availableGroupMembers) {
        if (DEBUG_MODE)
            Log.d(getClass().getName(), "availableGroupMembers.size(): " + availableGroupMembers.size());
        AvailabilityListAdapter availabilityListAdapter = new AvailabilityListAdapter(
                NewMeetingScheduleActivity.this,
                availableGroupMembers
        );
        availableMembersList.setAdapter(availabilityListAdapter);

        // Show the list
        if (availabilityLinearLayout.getVisibility() == View.GONE) {
            availabilityLinearLayout.setVisibility(View.VISIBLE);
        }

        // Hide loaders
        availabilityProgressBar.setVisibility(View.GONE);
        loadingAvailabilityLayout.setVisibility(View.GONE);

        // Enable schedule meeting button
        btnScheduleMeeting.setEnabled(true);
    }
}
