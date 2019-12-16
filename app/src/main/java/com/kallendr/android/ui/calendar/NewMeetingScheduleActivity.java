package com.kallendr.android.ui.calendar;

import android.content.Intent;
import android.os.Bundle;
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
import com.kallendr.android.helpers.interfaces.Result;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import static com.kallendr.android.helpers.Constants.DEBUG_MODE;

public class NewMeetingScheduleActivity extends AppCompatActivity {

    private int meetingDuration;
    private int meetingBreaks;
    private DateTime dateSelected;
    private DateTime meetingStart;
    private DateTime meetingEnd;

    private ProgressBar availabilityProgressBar;
    private TextView readableDateHeader;
    private LinearLayout availabilityLinearLayout;
    private LinearLayout loadingAvailabilityLayout;
    private ListView availableMembersList;
    private Button btnScheduleMeeting;

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
                // if a user is not available, ask for confirmation and provide re-scheduling option

            }
        });
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
        meetingBreaks = 10;
    }

    private void getDate() {
        dateSelected = new DateTime().plusMinutes(meetingBreaks);
        meetingStart = dateSelected;
        meetingEnd = dateSelected.plusMinutes(meetingBreaks + meetingDuration);

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
                            if (finalI == emails.size() - 1)
                                updateListUI(members);
                        }

                        @Override
                        public void fail(Boolean arg) {
                            members.add(groupMember);
                            if (finalI == emails.size() - 1)
                                updateListUI(members);
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
