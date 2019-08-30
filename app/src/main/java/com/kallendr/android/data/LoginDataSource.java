package com.kallendr.android.data;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kallendr.android.data.model.LoggedInUser;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.interfaces.OnTaskCompleted;
import com.kallendr.android.ui.calendar.CalendarMainActivity;
import com.kallendr.android.ui.calendar.MyCalendar;
import com.pixplicity.easyprefs.library.Prefs;

import java.io.IOException;

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
                            // Launch MyCalendar activity
                            Intent intent = new Intent(context, CalendarMainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }
                    }
                });

    }

    public void logout(Context context, final OnTaskCompleted onTaskCompleted) {
        Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, null));
        if (accountType == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            FirebaseAuth.getInstance().signOut();
            onTaskCompleted.onTaskCompleted(true, "");
        } else if (accountType == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            // Build a GoogleSignInClient with the options specified by gso.
            GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
            mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        onTaskCompleted.onTaskCompleted(
                                task.isSuccessful(),
                                ""
                        );
                    } else {
                        if (task.getException() != null)
                            onTaskCompleted.onTaskCompleted(
                                    task.isSuccessful(),
                                    task.getException().getMessage()
                            );
                        else
                            onTaskCompleted.onTaskCompleted(
                                    task.isSuccessful(),
                                    ""
                            );
                    }
                }
            });
        }
    }
}
