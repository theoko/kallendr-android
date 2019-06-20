package com.kallendr.android.ui.register;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kallendr.android.R;
import com.kallendr.android.helpers.OnTaskCompleted;
import com.kallendr.android.helpers.TextValidator;
import com.kallendr.android.helpers.UIHelpers;
import com.kallendr.android.ui.calendar.MyCalendar;
import com.kallendr.android.ui.home.MainActivity;

import java.util.ArrayList;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static Context mContext;

    LinearLayout registrationForm;
    LinearLayout inviteMembersForm;

    EditText emailAddress, teamName, userPassword, userPasswordConfirmation;

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
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Registration fields
        emailAddress = findViewById(R.id.emailAddress);
        teamName = findViewById(R.id.teamName);
        userPassword = findViewById(R.id.userPassword);
        userPasswordConfirmation = findViewById(R.id.userPasswordConfirmation);

        registrationForm = findViewById(R.id.registrationForm);
        inviteMembersForm = findViewById(R.id.inviteMembersForm);

        btnAddMember = findViewById(R.id.btnAddMember);
        btnDone = findViewById(R.id.btnDone);

        btnInviteTeam = findViewById(R.id.btnInviteTeam);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);

        numberOfLines = 1;
        emails = new ArrayList<>();
        mContext = this.getApplicationContext();

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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        emailAddress.setError("Invalid email address", getDrawable(R.drawable.ic_input_error));
                    }
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        teamName.setError("Invalid team name. Team names should not contain spaces.", getDrawable(R.drawable.ic_input_error));
                    }
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
                    LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, UIHelpers.spToPx(50f, RegisterActivity.this));
                    editText.setAlpha(0.6f);
                    editText.setBackgroundColor(Color.TRANSPARENT);
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
                String userPass = userPassword.getText().toString();

                /* Input validation */
                if(userEmail.trim().equals("")) {

                }
                if(userTeamName.trim().equals("")) {

                }
                if(userPass.trim().equals("")) {

                }

                // Create user with Firebase Authentication
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(userEmail, userPass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if(task.isSuccessful()) {
                                    // Display progress bar
                                    ProgressBar progressBar = new ProgressBar(RegisterActivity.this, null, android.R.attr.progressBarStyleLarge);
                                    progressBar.setIndeterminate(true);
                                    registrationForm.addView(progressBar);

                                    final TextView progressDescription = new TextView(getApplicationContext());
                                    progressDescription.setText("User created!");
                                    registrationForm.addView(progressDescription);

                                    // Hide registration form at this point
                                    emailAddress.setVisibility(View.GONE);
                                    teamName.setVisibility(View.GONE);
                                    inviteMembersForm.setVisibility(View.GONE);
                                    btnInviteTeam.setVisibility(View.GONE);
                                    btnCreateTeam.setVisibility(View.GONE);

                                    progressBar.setVisibility(View.VISIBLE);

                                    // Invite team members
                                    for (int i = 1; i <= numberOfLines; i++) {
                                        // Get field by tag ID
                                        EditText editText = inviteMembersForm.findViewWithTag("invMember" + i);
                                        if (editText != null) {
                                            String tmpEmail = editText.getText().toString();
                                            emails.add(tmpEmail);
                                            progressDescription.setText("Inviting " + tmpEmail + "...");
                                            System.out.println("Added email: " + tmpEmail);
                                        } else {
                                            System.out.println("i is null at: " + i);
                                        }
                                    }

                                    // Invite members
                                    inviteList = new TeamInvite(emails, progressBar);
                                    inviteList.sendEmails(new OnTaskCompleted() {
                                        @Override
                                        public void onTaskCompleted() {
                                            progressDescription.setText("Done");
                                            finishedRegistration();
                                        }
                                    });

                                } else {
                                    Toast.makeText(getApplicationContext(), "Registration failed!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

            }
        });
    }

    public void finishedRegistration() {
        Intent intent = new Intent(mContext, MyCalendar.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(mContext, MainActivity.class);
        startActivity(intent);
    }
}
