package com.kallendr.android.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.joda.time.DateTime;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {

    /**
     * Checks if the permission to read the calendar is granted and if not it tries to obtain it
     *
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
     *
     * @param context
     */
    public static void requestReadCalendarPermission(Activity context) {
        ActivityCompat.requestPermissions(context,
                new String[]{Manifest.permission.READ_CALENDAR},
                Constants.PERMISSIONS_REQUEST_READ_CALENDAR);
    }

    /**
     * This method generates an identifier for a team
     *
     * @return unique team identifier
     */
    public static String generateUniqueTeamIdentifier(String uid, String teamName) {
        return uid + "_" + teamName;
    }

    /**
     * This method will generate the start and end date in millis given the current date.
     *
     * @param currDate
     * @return
     */
    public static long[] generateStartAndEndMillis(Date currDate) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currDate.getTime());
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

    /**
     * This method replaces '.' with acceptable characters
     *
     * @param email
     * @return
     */
    public static String encodeEmailForFirebase(String email) {
        return email.replaceAll("\\.", ",");
    }

    /**
     * This method replaces '.' with acceptable characters
     *
     * @param email
     * @return decoded email
     */
    public static String decodeEmailFromFirebase(String email) {
        return email.replaceAll(",", ".");
    }

    /**
     * Method is used for checking valid email id format.
     * Taken from https://stackoverflow.com/questions/6119722/how-to-check-edittexts-text-is-email-address-or-not
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static String readableDate(DateTime date) {
        String readableDate = "";
        switch (date.getDayOfWeek()) {
            case 1:
                readableDate += "Monday";
                break;
            case 2:
                readableDate += "Tuesday";
                break;
            case 3:
                readableDate += "Wednesday";
                break;
            case 4:
                readableDate += "Thursday";
                break;
            case 5:
                readableDate += "Friday";
                break;
            case 6:
                readableDate += "Saturday";
                break;
            case 7:
                readableDate += "Sunday";
                break;
            default:
        }

        readableDate += ", ";
        String monthString = new DateFormatSymbols().getMonths()[date.getMonthOfYear()-1];
        readableDate += monthString;
        readableDate += " " + date.getDayOfMonth();
        readableDate += ", " + date.getYear();

        readableDate += " at " + date.getHourOfDay() + ":" + String.format(Locale.getDefault(),"%02d", date.getMinuteOfHour());
        Log.d("Helpers", String.format(Locale.getDefault(),"%02d", (date.getMinuteOfHour() + 10)));

        return readableDate;
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
