package com.kallendr.android.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.Calendar;
import java.util.Date;

public class Helpers {

    /**
     * Checks if the permission to read the calendar is granted and if not it tries to obtain it
     * @param context
     */
    public static void checkPermissionsAndRequestIfNeeded(Activity context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(context,
                    Manifest.permission.READ_CALENDAR)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(context,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        Constants.PERMISSIONS_REQUEST_READ_CALENDAR);

                // PERMISSIONS_REQUEST_READ_CALENDAR is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    /**
     * This method requests the permission to access calendar
     * @param context
     */
    public static void requestReadCalendarPermission(Activity context)
    {
        ActivityCompat.requestPermissions(context,
                new String[]{Manifest.permission.READ_CALENDAR},
                Constants.PERMISSIONS_REQUEST_READ_CALENDAR);
    }

    /**
     * This method generates an identifier for a team
     * @return unique team identifier
     */
    public static String generateUniqueTeamIdentifier(String uid, String teamName)
    {
        return uid + "_" + teamName;
    }

    /**
     * This method will generate the start and end date in millis given the current date.
     * @param currDate
     * @return
     */
    public static long[] generateStartAndEndMillis(Date currDate) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, currDate.getYear());
        cal.set(Calendar.MONTH, currDate.getMonth());
        cal.set(Calendar.DAY_OF_MONTH, currDate.getDay());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startDate = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endDate = cal.getTime();

        return new long[]{startDate.getTime(), endDate.getTime()};
    }

}
