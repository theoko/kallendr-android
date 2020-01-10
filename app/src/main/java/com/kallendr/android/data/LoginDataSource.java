package com.kallendr.android.data;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.ui.calendar.CalendarMainActivity;
import com.kallendr.android.ui.calendar.TeamCalendarActivity;
import com.kallendr.android.ui.home.MainActivity;
import com.pixplicity.easyprefs.library.Prefs;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public void login(final Context context, String username, String password) {

        FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()) {
                            Toast.makeText(context, "Please check the credentials and try again.", Toast.LENGTH_LONG).show();
                        } else {
                            // Launch CalendarMainActivity activity
                            Intent intent = new Intent(context, TeamCalendarActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    }
                });

    }

    public void logout(Context context) {
        Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
        if (accountType == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            FirebaseAuth.getInstance().signOut();
        } else if (accountType == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            //Use app context to prevent leaks using activity
            //.enableAutoManage(this /* FragmentActivity */, connectionFailedListener)
            GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(context) //Use app context to prevent leaks using activity
                    //.enableAutoManage(this /* FragmentActivity */, connectionFailedListener)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();
            if (mGoogleApiClient.isConnected()) {
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();
            }
        }

        Intent logoutIntent = new Intent(context, MainActivity.class);
        logoutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(logoutIntent);
    }
}
