package com.kallendr.android.ui.calendar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kallendr.android.R;
import com.kallendr.android.data.Database;
import com.kallendr.android.data.model.AvailableGroupMember;
import com.kallendr.android.data.observers.AvailableGroupMemberObserver;
import com.kallendr.android.helpers.interfaces.Result;
import com.pixplicity.easyprefs.library.Prefs;

import org.joda.time.DateTime;

import java.beans.PropertyChangeListener;
import java.util.Date;
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
    private Button btnAvailabilityScheduleMeeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meeting_schedule);

        availabilityProgressBar = findViewById(R.id.availabilityProgressBar);
        readableDateHeader = findViewById(R.id.readableDateHeader);
        availabilityLinearLayout = findViewById(R.id.availabilityLinearLayout);
        btnAvailabilityScheduleMeeting = findViewById(R.id.btnAvailabilityScheduleMeeting);
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
        dateSelected = new DateTime();
        meetingStart = dateSelected;
        meetingEnd = dateSelected.plusMinutes(meetingDuration);
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

    private void getAvailability(List<String> emails) {
        for (String email : emails) {
            AvailableGroupMember groupMember = new AvailableGroupMember(
                    email,
                    meetingStart.getMillis(),
                    meetingEnd.getMillis()
            );
            AvailableGroupMemberObserver availableGroupMemberObserver = new AvailableGroupMemberObserver(
                    groupMember,
                    new Result<Boolean>() {
                        @Override
                        public void success(Boolean arg) {
                            if (DEBUG_MODE) {
                                Log.d(getClass().getName(), "Available: " + arg);
                            }
                        }

                        @Override
                        public void fail(Boolean arg) {

                        }
                    }
            );
        }
    }
}
