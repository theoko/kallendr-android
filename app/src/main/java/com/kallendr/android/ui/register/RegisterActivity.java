package com.kallendr.android.ui.register;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.interfaces.OnTaskCompleted;
import com.kallendr.android.helpers.TextValidator;
import com.kallendr.android.helpers.UIHelpers;
import com.kallendr.android.services.EmailService;
import com.kallendr.android.ui.calendar.CalendarMainActivity;
import com.kallendr.android.ui.home.MainActivity;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class RegisterActivity extends AppCompatActivity {

    private static Context mContext;

    LinearLayout registrationForm;
    LinearLayout inviteMembersForm;

    TextView pageTitle;
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

        // Page title
        pageTitle = findViewById(R.id.pageTitle);

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

        // Number of users invited
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
                if (numberOfLines < 5) {
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
                pageTitle.setText("Registration");
                UIHelpers.runFadeOutAnimationOn(RegisterActivity.this, inviteMembersForm);
                inviteMembersForm.setVisibility(View.GONE);

                UIHelpers.runFadeInAnimationOn(RegisterActivity.this, emailAddress);
                emailAddress.setVisibility(View.VISIBLE);
                UIHelpers.runFadeInAnimationOn(RegisterActivity.this, userPassword);
                userPassword.setVisibility(View.VISIBLE);
                UIHelpers.runFadeInAnimationOn(RegisterActivity.this, userPasswordConfirmation);
                userPasswordConfirmation.setVisibility(View.VISIBLE);
                UIHelpers.runFadeInAnimationOn(RegisterActivity.this, teamName);
                teamName.setVisibility(View.VISIBLE);
                UIHelpers.runFadeInAnimationOn(RegisterActivity.this, btnCreateTeam);
                btnCreateTeam.setVisibility(View.VISIBLE);
                UIHelpers.runFadeInAnimationOn(RegisterActivity.this, btnInviteTeam);
                btnInviteTeam.setVisibility(View.VISIBLE);
            }
        });

        btnInviteTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageTitle.setText("Invite members");
                UIHelpers.runFadeInAnimationOn(RegisterActivity.this, inviteMembersForm);
                inviteMembersForm.setVisibility(View.VISIBLE);

                UIHelpers.runFadeOutAnimationOn(RegisterActivity.this, emailAddress);
                emailAddress.setVisibility(View.GONE);
                UIHelpers.runFadeOutAnimationOn(RegisterActivity.this, userPassword);
                userPassword.setVisibility(View.GONE);
                UIHelpers.runFadeOutAnimationOn(RegisterActivity.this, userPasswordConfirmation);
                userPasswordConfirmation.setVisibility(View.GONE);
                UIHelpers.runFadeOutAnimationOn(RegisterActivity.this, teamName);
                teamName.setVisibility(View.GONE);
                UIHelpers.runFadeOutAnimationOn(RegisterActivity.this, btnCreateTeam);
                btnCreateTeam.setVisibility(View.GONE);
                UIHelpers.runFadeOutAnimationOn(RegisterActivity.this, btnInviteTeam);
                btnInviteTeam.setVisibility(View.GONE);
            }
        });

        btnCreateTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Get field values
                final String userEmail = emailAddress.getText().toString();
                final String userTeamName = teamName.getText().toString();
                String userPass = userPassword.getText().toString();

                /* Input validation */
                if (userEmail.trim().equals("")) {
                    return;
                }
                if (userTeamName.trim().equals("")) {
                    return;
                }
                if (userPass.trim().equals("")) {
                    return;
                }

                // Create user with FirebaseAuth
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(userEmail, userPass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
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
                                    Set<String> invitedUserEmails = new HashSet<>();
                                    for (int i = 1; i <= numberOfLines; i++) {
                                        // Get field by tag ID
                                        EditText editText = inviteMembersForm.findViewWithTag("invMember" + i);
                                        if (editText != null) {
                                            String tmpEmail = editText.getText().toString();
                                            if (!tmpEmail.equals("")) {
                                                emails.add(tmpEmail);
                                                invitedUserEmails.add(tmpEmail);
                                                progressDescription.setText("Inviting " + tmpEmail + "...");
                                                if (Constants.DEBUG_MODE)
                                                    System.out.println("Added email: " + tmpEmail);
                                            }
                                        } else {
                                            if (Constants.DEBUG_MODE)
                                                System.out.println("i is null at: " + i);
                                        }
                                    }

                                    // Important!
                                    // Add user data to our shared preferences
                                    // before running the asynchronous task which
                                    // will write the emails provided to the database
                                    Prefs.putString(Constants.userEmail, userEmail);
                                    Prefs.putString(Constants.teamName, userTeamName);
                                    Prefs.putOrderedStringSet(Constants.emailSet, invitedUserEmails);

                                    // Invite members
                                    inviteList = new TeamInvite(emails, progressBar);
                                    inviteList.sendEmails(new OnTaskCompleted() {
                                        @Override
                                        public void onTaskCompleted(boolean taskSuccessful, String taskMessage) {
                                            /**
                                             * We ignore taskSuccessful and taskMessage since the user has just
                                             * registered which means that the team that was created is their first team.
                                             * Otherwise, we would check taskSuccessful and display taskMessage.
                                             */
                                            progressDescription.setText("Done");
                                            finishedRegistration();
                                        }
                                    });

                                    startService(new Intent(RegisterActivity.this, EmailService.class));

                                } else {
                                    Toast.makeText(getApplicationContext(), "Registration failed!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

            }
        });
    }

    public void finishedRegistration() {
        Intent intent = new Intent(mContext, CalendarMainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(mContext, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
