package com.kallendr.android.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.kallendr.android.helpers.Constants;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Set;

public class EmailService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        // Send emails to invited users
        Set<String> emailSet = Prefs.getOrderedStringSet(Constants.emailSet, null);
        if (emailSet != null) {
            Toast.makeText(this, emailSet.toString(), Toast.LENGTH_LONG).show();
            for (String email : emailSet) {
                System.out.println("GOT EMAIL: " + email);
            }
        }
    }
}
