package com.kallendr.android.ui.calendar;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.kallendr.android.R;
import com.kallendr.android.data.Database;
import com.kallendr.android.data.adapters.MeetingParticipantsAdapter;
import com.kallendr.android.data.model.MeetingBreak;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.UIHelpers;
import com.kallendr.android.helpers.interfaces.Result;
import com.pixplicity.easyprefs.library.Prefs;

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
        final int[] selectedHours = {0};
        final int[] selectedMinutes = {0};
        meetingDurationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(NewMeetingConfigurationActivity.this);
                final LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_minute_slider, null);
                final TextView hoursTextView = dialogView.findViewById(R.id.hoursTextView);
                final TextView minutesTextView = dialogView.findViewById(R.id.minutesTextView);
                SeekBar timeSlider = dialogView.findViewById(R.id.timeSlider);
                final Button btnMinuteSliderDone = dialogView.findViewById(R.id.btnMinuteSliderDone);
                int hours = meetingDuration / 60;
                int minutes = meetingDuration % 60;
                hoursTextView.setText(hours + " hours");
                minutesTextView.setText(minutes + " minutes");
                selectedHours[0] = selectedHours[0] == 0 ? hours : selectedHours[0];
                selectedMinutes[0] = selectedMinutes[0] == 0 ? minutes : selectedMinutes[0];
                int currProgress = ((selectedHours[0] * 60) + selectedMinutes[0]) / 5;
                timeSlider.setProgress(currProgress);
                hoursTextView.setText(selectedHours[0] + " hours");
                minutesTextView.setText(selectedMinutes[0] + " minutes");
                if (DEBUG_MODE) {
                    Log.e(getClass().getName(), "Progress: " + currProgress);
                }
                timeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        int hours = (progress * 5) / 60;
                        int minutes = (progress * 5) % 60;
                        hoursTextView.setText(hours + " hours");
                        minutesTextView.setText(minutes + " minutes");
                        selectedHours[0] = hours;
                        selectedMinutes[0] = minutes;
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                dialogBuilder.setView(dialogView);
                final AlertDialog alertDialog = dialogBuilder.create();
                alertDialog.show();
                btnMinuteSliderDone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        int hours = selectedHours[0];
                        int minutes = selectedMinutes[0];
                        meetingDurationEditText.setText(hours + " hours and " + minutes + " minutes");
                    }
                });
            }
        });
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (step) {
                    case 0:
                        if (step0()) {
                            step++;
                            continueButton.setText("Schedule");
                        }
                        break;
                    case 1:
                        step1();
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
            meetingDurationEditText.setText(meetingDuration + " minutes");
            // Show meeting description field
            findViewById(R.id.meetingDescriptionTextView).setVisibility(View.VISIBLE);
            meetingDescriptionEditText.setVisibility(View.VISIBLE);
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "Invalid meeting title", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void step1() {
        // Validate meeting duration and description and schedule meeting
        String meetingDescription = meetingDescriptionEditText.getText().toString();
        String teamID = Prefs.getString(Constants.selectedTeam, null);
        if (teamID != null) {
            Database.getInstance().scheduleTeamMeeting(
                    getApplicationContext(),
                    teamID,
                    meetingStart,
                    meetingParticipants,
                    meetingDuration,
                    new ArrayList<MeetingBreak>(),
                    meetingDescription,
                    new Result<Task<Void>>() {
                        @Override
                        public void success(Task<Void> arg) {
                            // Meeting is scheduled
                            Intent homeScreenIntent = new Intent(NewMeetingConfigurationActivity.this, CalendarMainActivity.class);
                            startActivity(homeScreenIntent);
                            finish();
                        }

                        @Override
                        public void fail(Task<Void> arg) {
                            // Failed to schedule meeting
                            Toast.makeText(getApplicationContext(), "Failed to schedule meeting!", Toast.LENGTH_LONG).show();
                        }
                    }
            );
        }
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
