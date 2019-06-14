package com.kallendr.android.ui.home;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kallendr.android.R;
import com.kallendr.android.ui.calendar.MyCalendar;
import com.kallendr.android.ui.login.LoginActivity;
import com.kallendr.android.ui.register.RegisterActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private LinearLayout get_started_layout;
    private LinearLayout options_layout;

    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        get_started_layout = findViewById(R.id.get_started_layout);
        options_layout = findViewById(R.id.options_layout);

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        // Check if user has already signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(MainActivity.this, MyCalendar.class);
            startActivity(intent);
            finish();
        }

    }

    public void btn_createTeam(View view) {
        Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(intent);
        finish();
    }

    public void btn_joinTeam(View view) {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void btn_getStarted(View view) {
        get_started_layout.setVisibility(View.GONE);
        options_layout.setVisibility(View.VISIBLE);
    }

}
