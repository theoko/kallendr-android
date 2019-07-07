package com.kallendr.android.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

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

}
