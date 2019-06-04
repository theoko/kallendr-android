package com.kallendr.android.ui.register;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.kallendr.android.R;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.OnTaskCompleted;
import com.kallendr.android.helpers.TextValidator;
import com.kallendr.android.helpers.UIHelpers;
import com.kallendr.android.ui.login.LoginActivity;

import java.util.ArrayList;

public class RegisterActivity extends AppCompatActivity {

    private static Context mContext;

    LinearLayout registrationForm;
    LinearLayout inviteMembersForm;

    EditText emailAddress;
    EditText teamName;
    EditText password;
    EditText confirmPassword;

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
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);

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
                final String userTeamName = teamName.getText().toString();

                String userPass = password.getText().toString();
                String userConfirmPass = confirmPassword.getText().toString();

                if(!userPass.equals(userConfirmPass)) {
                    Toast.makeText(RegisterActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Display progress bar
                final ProgressBar progressBar = new ProgressBar(RegisterActivity.this,null, android.R.attr.progressBarStyleLarge);
                progressBar.setIndeterminate(true);
                registrationForm.addView(progressBar);

                // Hide registration form at this point
                emailAddress.setVisibility(View.GONE);
                teamName.setVisibility(View.GONE);
                inviteMembersForm.setVisibility(View.GONE);
                btnInviteTeam.setVisibility(View.GONE);
                btnCreateTeam.setVisibility(View.GONE);

                progressBar.setVisibility(View.VISIBLE);

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(userEmail, userPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // Invite team members
                            DatabaseReference mTeamMembersRef = FirebaseDatabase.getInstance().getReference().child(userTeamName.toLowerCase().trim()).child(Constants.DB_INVITED_USERS);
                            for (int i=1; i<=numberOfLines; i++) {
                                // Get field by tag ID
                                EditText editText = inviteMembersForm.findViewWithTag("invMember" + i);
                                if(editText != null) {
                                    String tmpEmail = editText.getText().toString();
                                    emails.add(tmpEmail);
                                    mTeamMembersRef.push().setValue(tmpEmail);
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
                                    finishedRegistration();
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to create user!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

            }
        });
    }

    public void finishedRegistration() {
        Intent intent = new Intent(mContext, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
