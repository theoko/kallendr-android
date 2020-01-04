package com.kallendr.android.ui.calendar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.kallendr.android.R;
import com.kallendr.android.data.adapters.MeetingParticipantsAdapter;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.UIHelpers;

import java.util.ArrayList;
import java.util.Calendar;

import static com.kallendr.android.helpers.Constants.DEBUG_MODE;
import static com.kallendr.android.helpers.Constants.meeting_breaks;
import static com.kallendr.android.helpers.Constants.meeting_duration;
import static com.kallendr.android.helpers.Constants.meeting_start_time;

public class NewMeetingConfigurationActivity extends AppCompatActivity {

    private EditText meetingTitleEditText;
    private EditText meetingStartTimeEditText;
    private EditText meetingDurationEditText;
    private EditText meetingDescriptionEditText;

    private RecyclerView participantsRecyclerView;
    private MeetingParticipantsAdapter meetingParticipantsAdapter;
    private Button continueButton;

    // Data from previous activity
    private ArrayList<String> meetingParticipants;
    private long meetingStart;
    private int meetingDuration;
    private int meetingBreaks;

    // Meeting details
    private String meetingTitle;


    // Step
    private int step = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meeting_configuration);
        setTitle("New Meeting");
        participantsRecyclerView = findViewById(R.id.participantsRecyclerView);
        meetingTitleEditText = findViewById(R.id.meetingTitleEditText);
        meetingStartTimeEditText = findViewById(R.id.meetingStartTimeEditText);
        meetingDurationEditText = findViewById(R.id.meetingDurationEditText);
        meetingDescriptionEditText = findViewById(R.id.meetingDescriptionEditText);
        continueButton = findViewById(R.id.continueButton);

        // Get meeting information
        Intent intent = getIntent();
        meetingParticipants = intent.getStringArrayListExtra(Constants.meeting_participants);
        meetingStart = intent.getLongExtra(meeting_start_time, 0);
        meetingDuration = intent.getIntExtra(meeting_duration, 0);
        meetingBreaks = intent.getIntExtra(meeting_breaks, 0);
        if (DEBUG_MODE) {
            Log.e(getClass().getName(), "Participants: " + meetingParticipants.toString());
            Log.e(getClass().getName(), "Meeting start time: " + intent.getLongExtra(meeting_start_time, 0L));
            Log.e(getClass().getName(), "Meeting duration: " + intent.getIntExtra(meeting_duration, 0));
            Log.e(getClass().getName(), "Meeting breaks: " + intent.getIntExtra(meeting_breaks, 0));
        }
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (step) {
                    case 0:
                        if (step0()) {
                            step++;
                        }
                        break;
                    case 1:
                        if (step1()) {
                            step++;
                        }
                        break;
                    case 2:
                        step2();
                        break;
                    default:
                }

            }
        });
    }

    private boolean step0() {
        meetingTitle = meetingTitleEditText.getText().toString();
        if (!meetingTitle.equals("") && meetingTitle.length() < 20) {
            // Continue with meeting time
            findViewById(R.id.meetingTitleTextView).setVisibility(View.GONE);
            UIHelpers.runFadeOutAnimationOn(NewMeetingConfigurationActivity.this, meetingTitleEditText);
            meetingTitleEditText.setVisibility(View.GONE);
            UIHelpers.runFadeOutAnimationOn(NewMeetingConfigurationActivity.this, participantsRecyclerView);
            participantsRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.meetingParticipantsTextView).setVisibility(View.GONE);
            // Show meeting start time
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(meetingStart);
            meetingStartTimeEditText.setText(calendar.getTime().toString());
            findViewById(R.id.meetingStartTimeTextView).setVisibility(View.VISIBLE);
            meetingStartTimeEditText.setVisibility(View.VISIBLE);
            // Show meeting duration field
            findViewById(R.id.meetingDurationTextView).setVisibility(View.VISIBLE);
            meetingDurationEditText.setVisibility(View.VISIBLE);
            //meetingDurationEditText.setText(meetingDuration);
            // Show meeting description field
            findViewById(R.id.meetingDescriptionTextView).setVisibility(View.VISIBLE);
            meetingDescriptionEditText.setVisibility(View.VISIBLE);
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Invalid meeting title", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean step1() {
        return true;
    }

    private void step2() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        meetingParticipantsAdapter = new MeetingParticipantsAdapter(getLayoutInflater(), meetingParticipants);
        participantsRecyclerView.setAdapter(meetingParticipantsAdapter);
        participantsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        meetingTitleEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(meetingTitleEditText, InputMethodManager.SHOW_IMPLICIT);
    }
}
