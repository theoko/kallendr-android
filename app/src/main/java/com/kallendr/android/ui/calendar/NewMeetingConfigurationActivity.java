package com.kallendr.android.ui.calendar;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.kallendr.android.R;
import com.kallendr.android.data.adapters.MeetingParticipantsAdapter;
import com.kallendr.android.helpers.Constants;

import java.util.ArrayList;

import static com.kallendr.android.helpers.Constants.DEBUG_MODE;

public class NewMeetingConfigurationActivity extends AppCompatActivity {

    private RecyclerView participantsRecyclerView;
    private ArrayList<String> meetingParticipants;
    private MeetingParticipantsAdapter meetingParticipantsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_meeting_configuration);
        setTitle("New Meeting");
        participantsRecyclerView = findViewById(R.id.participantsRecyclerView);

        // Get meeting information
        Intent intent = getIntent();
        meetingParticipants = intent.getStringArrayListExtra(Constants.meeting_participants);
        if (DEBUG_MODE) {
            Log.e(getClass().getName(), meetingParticipants.toString());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        meetingParticipantsAdapter = new MeetingParticipantsAdapter(getLayoutInflater(), meetingParticipants);
        participantsRecyclerView.setAdapter(meetingParticipantsAdapter);
        participantsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }
}
