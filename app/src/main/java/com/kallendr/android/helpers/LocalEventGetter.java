package com.kallendr.android.helpers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import com.kallendr.android.data.model.LocalEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class LocalEventGetter {

    public static ArrayList<String> nameOfEvent = new ArrayList<>();
    public static ArrayList<String> startDates = new ArrayList<>();
    public static ArrayList<String> endDates = new ArrayList<>();
    public static ArrayList<String> descriptions = new ArrayList<>();

    public static List<LocalEvent> readCalendarEvent(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }

        try {

            Cursor cursor = context.getContentResolver()
                    .query(
                            Uri.parse("content://com.android.calendar/events"),
                            new String[]{"calendar_id", "title", "description",
                                    "dtstart", "dtend", "eventLocation"}, null,
                            null, null);
            Objects.requireNonNull(cursor).moveToFirst();
            // fetching calendars name
            String CNames[] = new String[cursor.getCount()];

            // fetching calendars id
            nameOfEvent.clear();
            startDates.clear();
            endDates.clear();
            descriptions.clear();
            List<LocalEvent> localEventList = new ArrayList<>();
            for (int i = 0; i < CNames.length; i++) {

                LocalEvent localEvent = new LocalEvent();
                localEvent.setName(cursor.getString(1));
                localEvent.setStartDate(getDate(Long.parseLong(cursor.getString(3))));
                localEvent.setEndDate(getDate(Long.parseLong(cursor.getString(4))));
                localEvent.setDescription(cursor.getString(2));
                localEventList.add(localEvent);
                CNames[i] = cursor.getString(1);
                cursor.moveToNext();

            }

            return localEventList;

        } catch (Exception e) {

            return null;
        }
    }

    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss a", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
