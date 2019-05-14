package com.kallendr.android.ui.register;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.kallendr.android.R;

public class RegisterActivity extends AppCompatActivity {

    LinearLayout inviteMembersForm;

    Button btnAddMember;
    Button btnDone;

    Button btnCreateTeam;
    Button btnInviteTeam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        inviteMembersForm = findViewById(R.id.inviteMembersForm);

        btnAddMember = findViewById(R.id.btnAddMember);
        btnDone = findViewById(R.id.btnDone);

        btnInviteTeam = findViewById(R.id.btnInviteTeam);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);

        addOnClickListeners();
    }

    private void addOnClickListeners() {
        btnAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnInviteTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
