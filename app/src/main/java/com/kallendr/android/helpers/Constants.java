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
    public static String users = "users";
    public static String userDB = "user_preferences";
    public static String userTeams = "user_teams";
    public static String userTeamsChild = "belongs_to";
    public static String userInvites = "user_invites";
    public static String userDetails = "user_details";
    public static String eventsDB = "events";
    public static String teamDB = "teams";
    public static String teamInvites = "invites";
    public static String emailField = "email";
    public static String usernameField = "username";
    public static String uidField = "UID";


    public static String firstLoginField = "firstLogin";
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
     * Team meeting model
     */
    public static String meeting_start_time = "startTime";
    public static String meeting_duration = "duration";
    public static String meeting_participants = "participants";
    public static String meeting_breaks = "breaks";
    public static String meeting_description = "description";

    /**
     * Team model (team name, team members, team meetings)
     */
    public static String teamName = "teamName";
    public static String teamMembers = "members";
    public static String teamMeetings = "teamMeetings";

    /**
     * Codes
     */
    public static final int PERMISSIONS_REQUEST_READ_CALENDAR = 1;
    public static final int RC_SIGN_IN = 1000;

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
    public static String accountType = "accountType";

    /**
     * Accounts
     */
    public enum ACCOUNT_TYPE {
        EMAIL_PASSWD_ACCOUNT,
        GOOGLE_ACCOUNT
    }
}
