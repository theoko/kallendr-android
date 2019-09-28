package com.kallendr.android.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.kallendr.android.data.Database;
import com.kallendr.android.data.model.LocalEvent;
import com.kallendr.android.helpers.Constants;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.List;

public class InitialEventUploadService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        if (Constants.DEBUG_MODE)
            Log.d(getClass().getName(), "Service InitialEventUploadService created!");
    }

    @Override
    public void onDestroy() {
        if (Constants.DEBUG_MODE)
            Log.d(getClass().getName(), "Service InitialEventUploadService stopped");
    }

    @Override
    public void onStart(Intent intent, int startId) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {if (Constants.DEBUG_MODE)
        Log.d(getClass().getName(), "Service InitialEventUploadService started");
        Constants.ACCOUNT_TYPE accountType = Constants.ACCOUNT_TYPE.valueOf(Prefs.getString(Constants.accountType, Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT.name()));
        List<LocalEvent> localEventsList = Database.getInstance(accountType).localEventsList;
        if (localEventsList != null) {
            if (Constants.DEBUG_MODE)
                System.out.println("Local events: " + localEventsList.size());
            /* Upload all local events to Firebase */
            Database.getInstance(accountType).uploadEvents(this, localEventsList);
        }

        /**
         * If this service's process is killed while it is started (after returning from onStartCommand(Intent, int, int)),
         * then it will be scheduled for a restart and the last delivered Intent re-delivered to it again
         * via onStartCommand(Intent, int, int).
         */
        return START_REDELIVER_INTENT;
    }
}
