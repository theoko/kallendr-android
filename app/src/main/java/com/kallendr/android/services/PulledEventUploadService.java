package com.kallendr.android.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.kallendr.android.data.Database;
import com.kallendr.android.data.model.LocalEvent;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.LocalEventGetter;

import java.util.List;

public class PulledEventUploadService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Try to pull new events
        List<LocalEvent> events = LocalEventGetter.readCalendarEvent(this);
        if (events != null) {
            if (Constants.DEBUG_MODE)
                System.out.println("Pulled events: " + events.size());
            /* Upload pulled events to Firebase */
            Database.getInstance().uploadEvents(events);
        }

        /**
         * If this service's process is killed while it is started (after returning from onStartCommand(Intent, int, int)),
         * then it will be scheduled for a restart and the last delivered Intent re-delivered to it again
         * via onStartCommand(Intent, int, int).
         */
        return START_REDELIVER_INTENT;
    }
}
