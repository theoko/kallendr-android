package com.kallendr.android.helpers;

public class Constants {
    /**
     * App constants
     */
    public static String APP_NAME = "Kallendr";
    public static Boolean DEBUG_MODE = true;

    /**
     * Database
     */
    public static String userDB = "user_preferences";
    public static String userTeams = "user_teams";
    public static String userInvites = "user_invites";
    public static String eventsDB = "events";
    public static String teamDB = "teams";
    public static String teamInvites = "invites";


    public static String COMPLETE = "completed";
    public static String INCOMPLETE = "incomplete";

    /**
     * LocalEvent model
     */
    public static String name_field = "name";
    public static String description_field = "description";
    public static String startDate_field = "startDate";
    public static String endDate_field = "endDate";

    /**
     * Team model
     */
    public static String teamName = "teamName";
    public static String teamMembers = "members";

    /**
     * Permission codes
     */
    public static final int PERMISSIONS_REQUEST_READ_CALENDAR = 1;

    /**
     * Preferences
     */
    public static String allowNotifications = "allowNotif";
    public static String allowCalendarAccess = "allowCalAccess";

    /**
     * Shared Preferences Keys
     */
    public static String userEmail = "userEmail";
    public static String calendarPermission = "calendarPermission";
    public static String notificationPermission = "notificationPermission";
    public static String emailSet = "emailSet";
    public static String selectedTeam = "sTeam";
}
