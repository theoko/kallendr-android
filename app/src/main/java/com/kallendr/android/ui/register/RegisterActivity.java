package com.kallendr.android.ui.register;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kallendr.android.R;
import com.kallendr.android.helpers.TextValidator;
import com.kallendr.android.helpers.UIHelpers;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    LinearLayout registrationForm;
    LinearLayout inviteMembersForm;

    EditText emailAddress;
    EditText teamName;

    Button btnAddMember;
    Button btnDone;

    Button btnCreateTeam;
    Button btnInviteTeam;
    private int numberOfLines;
    TeamInvite inviteList;
    ArrayList<String> emails;

    boolean hasErrors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Registration fields
        emailAddress = findViewById(R.id.emailAddress);
        teamName = findViewById(R.id.teamName);

        registrationForm = findViewById(R.id.registrationForm);
        inviteMembersForm = findViewById(R.id.inviteMembersForm);

        btnAddMember = findViewById(R.id.btnAddMember);
        btnDone = findViewById(R.id.btnDone);

        btnInviteTeam = findViewById(R.id.btnInviteTeam);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);

        numberOfLines = 1;
        emails = new ArrayList<>();

        addValidatorListeners();
        addOnClickListeners();
    }

    private void addValidatorListeners() {
        hasErrors = true;

        emailAddress.addTextChangedListener(new TextValidator(emailAddress) {
            @Override
            public void validate(TextView textView, String text) {
                if (!UIHelpers.isValidEmail(text)) {
                    // Add errors to field
                    emailAddress.setError("Invalid email address", getDrawable(R.drawable.ic_input_error));
                    hasErrors = true;
                } else {
                    hasErrors = false;
                }
            }
        });

        teamName.addTextChangedListener(new TextValidator(teamName) {
            @Override
            public void validate(TextView textView, String text) {
                if (!UIHelpers.isValidTeamName(text)) {
                    // Add errors to field
                    teamName.setError("Invalid team name. Team names should not contain spaces.", getDrawable(R.drawable.ic_input_error));
                    hasErrors = true;
                } else {
                    hasErrors = false;
                }
            }
        });
    }

    private void addOnClickListeners() {
        btnAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numberOfLines < 5) {
                    EditText editText = new EditText(getApplicationContext());
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    editText.setLayoutParams(p);

                    // Increment number of lines
                    numberOfLines++;
                    editText.setTag("invMember" + numberOfLines);
                    editText.setHint("Member " + numberOfLines + " Email");
                    inviteMembersForm.addView(editText, numberOfLines - 1);
                } else {
                    Toast.makeText(getApplicationContext(), "You can invite more team members later", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Set invite members form visibility to gone
                inviteMembersForm.setVisibility(View.GONE);
                btnCreateTeam.setVisibility(View.VISIBLE);
                btnInviteTeam.setVisibility(View.VISIBLE);
            }
        });

        btnInviteTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteMembersForm.setVisibility(View.VISIBLE);
                btnCreateTeam.setVisibility(View.GONE);
                btnInviteTeam.setVisibility(View.GONE);
            }
        });

        btnCreateTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get field values
                String userEmail = emailAddress.getText().toString();
                String userTeamName = teamName.getText().toString();



                // Display progress bar
                ProgressBar progressBar = new ProgressBar(RegisterActivity.this,null, android.R.attr.progressBarStyleLarge);
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                inviteMembersForm.addView(progressBar);

                // Invite team members
                for (int i=1; i<=numberOfLines; i++) {
                    // Get field by tag ID
                    EditText editText = inviteMembersForm.findViewWithTag("invMember" + i);
                    if(editText != null) {
                        String tmpEmail = editText.getText().toString();
                        emails.add(tmpEmail);
                        System.out.println("Added email: " + tmpEmail);
                    } else {
                        System.out.println("i is null at: " + i);
                    }
                }

                // Invite members
                inviteList = new TeamInvite(emails);

                progressBar.setVisibility(View.GONE);
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
