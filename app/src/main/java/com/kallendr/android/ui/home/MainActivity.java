package com.kallendr.android.ui.home;

import android.content.ContextWrapper;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kallendr.android.R;
import com.kallendr.android.ui.calendar.CalendarMainActivity;
import com.kallendr.android.ui.calendar.MyCalendar;
import com.kallendr.android.ui.login.LoginActivity;
import com.kallendr.android.ui.register.RegisterActivity;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private LinearLayout get_started_layout;
    private LinearLayout options_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        get_started_layout = findViewById(R.id.get_started_layout);
        options_layout = findViewById(R.id.options_layout);
        // Register Shared Preferences library
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        // Check if user has already signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (user != null || account != null) {
            Intent intent = new Intent(MainActivity.this, CalendarMainActivity.class);
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
