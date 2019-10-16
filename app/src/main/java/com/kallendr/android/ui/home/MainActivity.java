package com.kallendr.android.ui.home;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kallendr.android.R;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.ui.calendar.CalendarMainActivity;
import com.kallendr.android.ui.login.LoginActivity;
import com.kallendr.android.ui.register.RegisterActivity;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Objects;

import static com.kallendr.android.helpers.Constants.DEBUG_MODE;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();
        // Register Shared Preferences library
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        // Check if user has already signed in
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (DEBUG_MODE) {
            if (user != null) {
                Log.d(getClass().getName(), "Account: " + user.getUid());
            }
            if (account != null) {
                Log.d(getClass().getName(), "Account: " + account.getId());
            }
        }
        if (user != null || account != null) {
            if (user != null) {
                Prefs.putString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name());
            }
            if (account != null) {
                Prefs.putString(Constants.accountType, Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT.name());
            }
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

}
