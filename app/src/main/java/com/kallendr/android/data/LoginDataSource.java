package com.kallendr.android.data;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.kallendr.android.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        AuthResult authResult = FirebaseAuth.getInstance().signInWithEmailAndPassword(username, password)
                .getResult();

        if (authResult != null) {
            FirebaseUser authUser = authResult.getUser();

            return new Result.Success<>(authUser);
        } else {
            return new Result.Error(new IOException("Error logging in"));
        }

    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
    }
}
