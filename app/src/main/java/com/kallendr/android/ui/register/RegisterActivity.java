package com.kallendr.android.ui.register;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.kallendr.android.R;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    LinearLayout inviteMembersForm;

    Button btnAddMember;
    Button btnDone;

    Button btnCreateTeam;
    Button btnInviteTeam;
    private int numberOfLines;
    TeamInvite inviteList;
    ArrayList<String> emails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inviteMembersForm = findViewById(R.id.inviteMembersForm);

        btnAddMember = findViewById(R.id.btnAddMember);
        btnDone = findViewById(R.id.btnDone);

        btnInviteTeam = findViewById(R.id.btnInviteTeam);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);

        numberOfLines = 1;
        emails = new ArrayList<>();

        addOnClickListeners();
    }

    private void addOnClickListeners() {
        btnAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = new EditText(getApplicationContext());
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                editText.setLayoutParams(p);

                // Increment number of lines
                numberOfLines++;
                editText.setTag("invMember" + numberOfLines);
                editText.setHint("Member " + numberOfLines + " Email");
                inviteMembersForm.addView(editText, numberOfLines - 1);
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display animation
            }
        });

        btnInviteTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteMembersForm.setVisibility(View.VISIBLE);
            }
        });

        btnCreateTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    public void btn_inviteMembers(View view) {
        inviteMembersForm.setVisibility(View.VISIBLE);
        btnCreateTeam.setEnabled(false);
        btnCreateTeam.setBackgroundColor(android.R.drawable.btn_default);
        btnCreateTeam.setTextColor(getResources().getColor(android.R.color.black));
    }

    public void btn_createTeam(View view) {

    }

}
