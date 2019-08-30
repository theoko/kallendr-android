package com.kallendr.android.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kallendr.android.data.model.LocalEvent;
import com.kallendr.android.data.model.Team;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.Helpers;
import com.kallendr.android.helpers.interfaces.EventCallback;
import com.kallendr.android.helpers.interfaces.FirstLoginCallback;
import com.kallendr.android.helpers.interfaces.PrefMapCallback;
import com.kallendr.android.helpers.interfaces.Result;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static Database INSTANCE = null;
    private FirebaseDatabase firebaseDatabase;
    private Constants.ACCOUNT_TYPE account_type;

    /**
     * Temp data holders
     */
    public List<LocalEvent> localEventsList;

    private Database() {
        this.account_type = Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT;
    }

    private Database(Constants.ACCOUNT_TYPE account_type) {
        new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        this.account_type = account_type;
    }

    public static Database getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Database();
        }
        return INSTANCE;
    }

    public static Database getInstance(Constants.ACCOUNT_TYPE account_type) {
        if (INSTANCE == null) {
            INSTANCE = new Database(account_type);
        }
        return INSTANCE;
    }

    /**
     * Checks if the user has logged in before
     *
     * @param firstLoginCallback
     */
    public void firstLogin(final Context context, final FirstLoginCallback firstLoginCallback) {
        String uid;
        if (this.account_type == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else if (this.account_type == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            uid = account.getId();
        } else {
            return;
        }
        final DatabaseReference mUserReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(Constants.userDB)
                .child(uid);
        ValueEventListener mUserValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if user is invited to a team.
                // In case they are, add them to that team
                createUser(context);
                settleInvites(context);
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    DatabaseReference firstLogin = mUserReference.child("firstLogin");
                    ValueEventListener firstLoginValueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                firstLoginCallback.onFirstLogin(false);
                            } else {
                                firstLoginCallback.onFirstLogin(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    };
                    firstLogin.addListenerForSingleValueEvent(firstLoginValueEventListener);
                    firstLogin.removeEventListener(firstLoginValueEventListener);
                } else {
                    firstLoginCallback.onFirstLogin(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mUserReference.addListenerForSingleValueEvent(mUserValueEventListener);
        mUserReference.removeEventListener(mUserValueEventListener);
    }

    public void createUser(Context context) {
        if (Constants.DEBUG_MODE) {
            System.out.println("Calling createUser()");
        }
        final String uid;
        String email;
        String displayName;
        if (this.account_type == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            displayName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        } else if (this.account_type == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            uid = account.getId();
            email = account.getEmail();
            displayName = account.getDisplayName();
        } else {
            return;
        }
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.users)
                .child(uid)
                .child(Constants.emailField)
                .setValue(email);
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.users)
                .child(uid)
                .child(Constants.usernameField)
                .setValue(displayName);
    }

    public void settleInvites(Context context) {
        if (Constants.DEBUG_MODE) {
            System.out.println("Calling settleInvites()");
        }
        final String uid;
        final String email;
        if (this.account_type == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } else if (this.account_type == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            uid = account.getId();
            email = account.getEmail();
        } else {
            return;
        }
        getInviteStatus(context, new Result<List<Team>>() {
            @Override
            public void success(List<Team> arg) {
                if (Constants.DEBUG_MODE) {
                    System.out.println("getInviteStatus size: " + arg.size());
                }
                for (Team team : arg) {
                    String teamID = team.getTeamID();
                    addMemberToTeam(teamID, email, uid);
                    removeMemberFromInvitedUser(teamID, email);
                }
            }

            @Override
            public void fail(List<Team> arg) {

            }
        });
    }

    /**
     * Called when the initial calendar setup is completed
     */
    public void onCalendarSetupComplete(Context context) {
        // Updates database to indicate that the user has completed setup
        String uid;
        if (this.account_type == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else if (this.account_type == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            uid = account.getId();
        } else {
            return;
        }
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.userDB)
                .child(uid)
                .child(Constants.firstLoginField)
                .setValue(Constants.COMPLETE);
    }

    /**
     * Sets preferences for authenticated user
     *
     * @param prefMap
     */
    public void setPreferences(Context context, Map<String, Boolean> prefMap) {
        String uid;
        if (this.account_type == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else if (this.account_type == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            uid = account.getId();
        } else {
            return;
        }
        DatabaseReference mPrefReference = FirebaseDatabase.getInstance().getReference().child(Constants.userDB).child(uid);
        if (prefMap.get(Constants.allowNotifications) != null) {
            mPrefReference.child(Constants.allowNotifications).setValue(prefMap.get(Constants.allowNotifications));
        }
        if (prefMap.get(Constants.allowCalendarAccess) != null) {
            mPrefReference.child(Constants.allowCalendarAccess).setValue(prefMap.get(Constants.allowCalendarAccess));
        }
    }

    /**
     * Gets preferences for authenticated user from the server
     *
     * @param prefMapCallback
     */
    public void getPreferences(Context context, final PrefMapCallback prefMapCallback) {
        String uid;
        if (this.account_type == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else if (this.account_type == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            uid = account.getId();
        } else {
            return;
        }
        final DatabaseReference mPrefReference = FirebaseDatabase.getInstance().getReference().child(Constants.userDB).child(uid);
        ValueEventListener mPrefValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Boolean> returnPrefMap = new HashMap<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.getKey() != null && ds.getValue() != null) {
                            switch (ds.getKey()) {
                                case "allowNotif":
                                    returnPrefMap.put(ds.getKey(), Boolean.valueOf(ds.getValue().toString()));
                                    break;

                                case "allowCalAccess":
                                    returnPrefMap.put(ds.getKey(), Boolean.valueOf(ds.getValue().toString()));
                                    break;

                                default:

                            }
                        }
                    }
                    prefMapCallback.onSuccess(returnPrefMap);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                prefMapCallback.onFail(databaseError.getMessage());
            }
        };
        mPrefReference.addListenerForSingleValueEvent(mPrefValueEventListener);
        mPrefReference.removeEventListener(mPrefValueEventListener);
    }

    /**
     * This method adds a member (by their UID) to a team.
     * This includes the date that the user joined the team.
     *
     * @param teamID
     * @param uid
     */
    public void addMemberToTeam(String teamID, String email, String uid) {
        long time = new Date().getTime();
        /* teams -> ...uid...: ...time... */
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.teamDB)
                .child(teamID)
                .child(Constants.teamMembers)
                .child(uid)
                .setValue(time).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                }
            }
        });
        /* user_teams -> ...uid...: ...time..., user_teams -> email: ...email... */
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.userTeams)
                .child(uid)
                .child(Constants.userTeamsChild)
                .child(teamID)
                .setValue(time).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                }
            }
        });
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.userTeams)
                .child(uid)
                .child(Constants.emailField)
                .setValue(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                }
            }
        });
    }

    /**
     * This method removes a user account from the invited users
     *
     * @param teamID
     * @param email
     */
    private void removeMemberFromInvitedUser(String teamID, String email) {
        String encodedEmail = Helpers.encodeEmailForFirebase(email);
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.teamDB)
                .child(teamID)
                .child(Constants.teamInvites)
                .child(encodedEmail)
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                }
            }
        });
    }

    /**
     * This method will store the team's name to the database and
     * add the list of emails as child elements to the team that was just created
     *
     * @param emails
     */
    public void setTeamNameAndAddEmailsToInvitationList(Context context, final ArrayList<String> emails, final Result<String> result) {
        final String uid;
        final String email;
        if (this.account_type == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } else if (this.account_type == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            uid = account.getId();
            email = account.getEmail();
        } else {
            return;
        }
        final String teamName = Prefs.getString(Constants.teamName, null);
        if (teamName == null) {
            // teamName cannot be null
            // If it is, something went completely wrong
            return;
        } else {
            final String teamID = Helpers.generateUniqueTeamIdentifier(uid, teamName);
            final DatabaseReference mTeamEmailsReference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.teamDB)
                    .child(teamID);
            mTeamEmailsReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        // Save the team name to the database
                        mTeamEmailsReference.child(Constants.teamName)
                                .setValue(teamName);
                        // Add authenticated user to their own team
                        addMemberToTeam(teamID, email, uid);
                        // Check if we have any invited users
                        if (emails.size() > 0) {
                            long time = new Date().getTime();
                            for (String email : emails) {
                                // This will generate a random ID as key to the new child
                                // and will push the email to the list of invites
                                String encodedEmail = Helpers.encodeEmailForFirebase(email);
                                mTeamEmailsReference.child(Constants.teamInvites).child(encodedEmail).setValue(time);
                                FirebaseDatabase.getInstance().getReference()
                                        .child(Constants.userInvites)
                                        .child(encodedEmail)
                                        .child(teamID)
                                        .setValue(time).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                        }
                                    }
                                });
                            }
                        }
                        // Our team has been created successfully
                        result.success("Team created successfully");
                    } else {
                        // Team already exists
                        result.fail("The team already exists!");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    /**
     * This method is responsible for uploading the list of events provided as the first argument to Firebase
     *
     * @param localEventsList
     */
    public void uploadEvents(Context context, final List<LocalEvent> localEventsList) {
        String uid;
        if (this.account_type == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else if (this.account_type == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            uid = account.getId();
        } else {
            return;
        }
        final DatabaseReference mUploadEventsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.eventsDB)
                .child(uid);
        ValueEventListener mUploadEventsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() != localEventsList.size()) {
                    for (LocalEvent localEvent : localEventsList) {
                        // This will generate a unique ID as a key for the event and set its value
                        // automatically by reading the fields of the LocalEvent class
                        mUploadEventsReference.push().setValue(localEvent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mUploadEventsReference.addListenerForSingleValueEvent(mUploadEventsValueEventListener);
    }

    /**
     * This method is responsible for retrieving events that are within the range of the provided dates
     *
     * @param startTimeInMillis
     * @param endTimeInMillis
     * @param eventCallback
     */
    public void getEvents(Context context, final long startTimeInMillis, final long endTimeInMillis, final EventCallback eventCallback) {
        String uid;
        if (this.account_type == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else if (this.account_type == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            uid = account.getId();
        } else {
            return;
        }
        DatabaseReference mEventsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.eventsDB)
                .child(uid);
        ValueEventListener mEventsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<LocalEvent> returnList = new ArrayList<>();
                for (DataSnapshot event : dataSnapshot.getChildren()) {
                    long startDate = Long.valueOf(event.child("startDate").getValue().toString());
                    long endDate = Long.valueOf(event.child("endDate").getValue().toString());

                    if (startTimeInMillis >= startDate && endTimeInMillis <= endDate) {
                        String name = event.child("name").getValue().toString();
                        String description = event.child("description").getValue().toString();

                        LocalEvent localEvent = new LocalEvent();
                        localEvent.setName(name);
                        localEvent.setStartDate(startDate);
                        localEvent.setEndDate(endDate);
                        localEvent.setDescription(description);
                        returnList.add(localEvent);
                    }
                }

                eventCallback.onSuccess(returnList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                eventCallback.onFail(databaseError.getMessage());
            }
        };
        mEventsReference.addListenerForSingleValueEvent(mEventsValueEventListener);
        mEventsReference.removeEventListener(mEventsValueEventListener);
    }

    /**
     * This method is responsible for getting a user's events.
     *
     * @param userUID
     * @param startTimeInMillis
     * @param endTimeInMillis
     * @param eventCallback
     */
    public void getEventsForUID(String userUID, final long startTimeInMillis, final long endTimeInMillis, final EventCallback eventCallback) {
        DatabaseReference mEventsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.eventsDB)
                .child(userUID);
        ValueEventListener mEventsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    List<LocalEvent> localEventList = new ArrayList<>();
                    for (DataSnapshot eventDt : dataSnapshot.getChildren()) {
                        String eventName = (String) eventDt.child(Constants.name_field).getValue();
                        String eventDescription = (String) eventDt.child(Constants.description_field).getValue();
                        long eventStartDate = (long) eventDt.child(Constants.startDate_field).getValue();
                        long eventEndDate = (long) eventDt.child(Constants.endDate_field).getValue();

                        LocalEvent localEvent = new LocalEvent();
                        localEvent.setName(eventName);
                        localEvent.setDescription(eventDescription);
                        localEvent.setStartDate(eventStartDate);
                        localEvent.setEndDate(eventEndDate);
                        localEventList.add(localEvent);
                    }
                    eventCallback.onSuccess(localEventList);
                } else {
                    eventCallback.onFail("No events found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                eventCallback.onFail(databaseError.getMessage());
            }
        };
        mEventsReference.addListenerForSingleValueEvent(mEventsValueEventListener);
        mEventsReference.removeEventListener(mEventsValueEventListener);
    }

    /**
     * This method is responsible for getting a team's name by providing the team ID.
     * To learn how a team ID is generated, see Helpers.java.
     *
     * @param teamID
     * @param result
     */
    public void getTeamNameByID(String teamID, final Result<String> result) {
        DatabaseReference mTeam = FirebaseDatabase.getInstance().getReference()
                .child(Constants.teamDB)
                .child(teamID)
                .child(Constants.teamName);
        ValueEventListener mTeamValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                result.success((String) dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                result.fail("Failed to get team name");
            }
        };
        mTeam.addListenerForSingleValueEvent(mTeamValueEventListener);
        mTeam.removeEventListener(mTeamValueEventListener);
    }

    /**
     * This method is responsible for getting information for the specified team.
     *
     * @param teamID
     * @param result
     */
    public void getTeamInfo(final String teamID, final Result<Team> result) {
        DatabaseReference mTeam = FirebaseDatabase.getInstance().getReference()
                .child(Constants.teamDB)
                .child(teamID);
        ValueEventListener mTeamValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    String teamName = (String) dataSnapshot.child(Constants.teamName).getValue();
                    long membersCount = dataSnapshot.child(Constants.teamMembers).getChildrenCount();
                    Team team = new Team();
                    team.setTeamID(teamID);
                    team.setTeamName(teamName);
                    if (membersCount == 1) {
                        team.setDescription(membersCount + " member");
                    } else {
                        team.setDescription(membersCount + " members");
                    }
                    result.success(team);
                } else {
                    result.fail(null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                result.fail(null);
            }
        };
        mTeam.addListenerForSingleValueEvent(mTeamValueEventListener);
        mTeam.removeEventListener(mTeamValueEventListener);
    }

    /**
     * This method returns the teams that the user belongs to.
     *
     * @param listOfTeamIDs
     */
    public void getTeamStatus(Context context, final Result<List<Team>> listOfTeamIDs) {
        String uid;
        if (this.account_type == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else if (this.account_type == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            uid = account.getId();
        } else {
            return;
        }
        DatabaseReference mTeamsUserBelongsTo = FirebaseDatabase.getInstance().getReference()
                .child(Constants.userTeams)
                .child(uid)
                .child(Constants.userTeamsChild);
        mTeamsUserBelongsTo
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final List<Team> resultList = new ArrayList<>();
                        final long childrenCount = dataSnapshot.getChildrenCount();
                        for (DataSnapshot dt : dataSnapshot.getChildren()) {
                            String teamID = dt.getKey();
                            if (Constants.DEBUG_MODE)
                                System.out.println("getTeamStatus KEY: " + teamID);

                            // Get info for this team
                            Database.getInstance(account_type).getTeamInfo(teamID, new Result<Team>() {
                                @Override
                                public void success(Team arg) {
                                    if (arg == null) {
                                        listOfTeamIDs.fail(null);
                                    }
                                    resultList.add(arg);
                                    if (Constants.DEBUG_MODE)
                                        System.out.println("resultList size: " + resultList.size() + ", childrenCount: " + childrenCount);
                                    // Return whenever we get all the teams
                                    if (resultList.size() == childrenCount) {
                                        listOfTeamIDs.success(resultList);
                                    }
                                }

                                @Override
                                public void fail(Team arg) {
                                    if (Constants.DEBUG_MODE)
                                        System.out.println("Failed to get team!!!");
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listOfTeamIDs.fail(new ArrayList<Team>());
                    }
                });
    }

    /**
     * This method returns a list of teams that the user is invited to.
     *
     * @param listOfTeamIDs
     */
    public void getInviteStatus(Context context, final Result<List<Team>> listOfTeamIDs) {
        String email;
        if (this.account_type == Constants.ACCOUNT_TYPE.EMAIL_PASSWD_ACCOUNT) {
            email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        } else if (this.account_type == Constants.ACCOUNT_TYPE.GOOGLE_ACCOUNT) {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
            email = account.getEmail();
        } else {
            return;
        }
        email = Helpers.encodeEmailForFirebase(email);
        if (Constants.DEBUG_MODE) {
            System.out.println("Encoding email to: " + email);
        }
        DatabaseReference mTeamsUserBelongsTo = FirebaseDatabase.getInstance().getReference()
                .child(Constants.userInvites)
                .child(email);
        mTeamsUserBelongsTo
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final List<Team> resultList = new ArrayList<>();
                        final long childrenCount = dataSnapshot.getChildrenCount();
                        for (DataSnapshot dt : dataSnapshot.getChildren()) {
                            String teamID = dt.getKey();
                            if (Constants.DEBUG_MODE)
                                System.out.println("KEY: " + teamID);

                            Database.getInstance(account_type).getTeamInfo(teamID, new Result<Team>() {
                                @Override
                                public void success(Team arg) {
                                    resultList.add(arg);
                                    if (resultList.size() == childrenCount) {
                                        if (Constants.DEBUG_MODE)
                                            System.out.println("listOfTeamIDs.success()");
                                        listOfTeamIDs.success(resultList);
                                    }
                                }

                                @Override
                                public void fail(Team arg) {
                                    listOfTeamIDs.fail(null);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listOfTeamIDs.fail(new ArrayList<Team>());
                    }
                });
    }

    /**
     * This method is responsible for getting the events of a team that are within the range of the provided dates.
     *
     * @param startTimeInMillis
     * @param endTimeInMillis
     * @param eventCallback
     */
    public void getTeamEvents(final long startTimeInMillis, final long endTimeInMillis, final EventCallback eventCallback) {
        String selectedTeam = Prefs.getString(Constants.selectedTeam, null);
        if (selectedTeam != null) {
            // Get team members
            DatabaseReference mTeamMembersReference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.teamDB)
                    .child(selectedTeam)
                    .child(Constants.teamMembers);
            // For each member, get their events
            mTeamMembersReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    // A team should always have at least one user
                    if (dataSnapshot.getChildrenCount() > 0) {
                        if (Constants.DEBUG_MODE) {
                            // Print selected date
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(startTimeInMillis);

                            int mYear = calendar.get(Calendar.YEAR);
                            int mMonth = calendar.get(Calendar.MONTH);
                            int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                            System.out.println("Start time: " + mYear + "/" + mMonth + "/" + mDay);
                            calendar.setTimeInMillis(endTimeInMillis);
                            mYear = calendar.get(Calendar.YEAR);
                            mMonth = calendar.get(Calendar.MONTH);
                            mDay = calendar.get(Calendar.DAY_OF_MONTH);
                            System.out.println("End time: " + mYear + "/" + mMonth + "/" + mDay);
                        }
                        final List<LocalEvent> eventsWithinRangeList = new ArrayList<>();
                        for (DataSnapshot dt : dataSnapshot.getChildren()) {
                            String userID = dt.getKey();
                            if (Constants.DEBUG_MODE)
                                System.out.println("Getting events for: " + userID);
                            Database.getInstance(account_type).getEventsForUID(userID, startTimeInMillis, endTimeInMillis, new EventCallback() {
                                @Override
                                public void onSuccess(List<LocalEvent> eventList) {
                                    // Events for this user
                                    int remaining = eventList.size();
                                    if (Constants.DEBUG_MODE)
                                        System.out.println("Got " + remaining + " events total!");
                                    for (LocalEvent event : eventList) {
                                        long startDate = event.getStartDate();
                                        long endDate = event.getEndDate();
                                        // Check if event is within range
                                        if (startDate >= startTimeInMillis && startDate <= endTimeInMillis) {
                                            if (Constants.DEBUG_MODE)
                                                System.out.println("Adding event: " + event.getName());
                                            eventsWithinRangeList.add(event);
                                        }
                                        // Return when we reach the end of events and the end of children
                                        if (remaining == 1) {
                                            eventCallback.onSuccess(eventsWithinRangeList);
                                        }
                                        remaining--;
                                    }
                                }

                                @Override
                                public void onFail(String message) {

                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            eventCallback.onFail("No team selected");
        }
    }

    /**
     * This method is responsible for getting a list containing the emails of the invited users for the selected team.
     *
     * @param emails
     */
    public void getInvitedUsers(final Result<List<String>> emails) {
        String selectedTeam = Prefs.getString(Constants.selectedTeam, null);
        if (selectedTeam != null) {
            DatabaseReference mInvitedUsersReference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.teamDB)
                    .child(selectedTeam)
                    .child(Constants.teamInvites);
            ValueEventListener mInvitesUsersValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        List<String> emailList = new ArrayList<>();
                        for (DataSnapshot dt : dataSnapshot.getChildren()) {
                            String userEmail = Helpers.decodeEmailFromFirebase(dt.getKey());
                            emailList.add(userEmail);
                        }
                        emails.success(emailList);
                    } else {
                        emails.success(new ArrayList<String>());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    emails.fail(new ArrayList<String>());
                }
            };
            mInvitedUsersReference.addListenerForSingleValueEvent(mInvitesUsersValueEventListener);
            mInvitedUsersReference.removeEventListener(mInvitesUsersValueEventListener);
        } else {
            emails.fail(new ArrayList<String>());
        }
    }

    /**
     * This method is responsible for getting a list containing the emails of the team members for the selected team.
     *
     * @param emails
     */
    public void getTeamMembers(final Result<List<String>> emails) {
        String selectedTeam = Prefs.getString(Constants.selectedTeam, null);
        if (selectedTeam != null) {
            DatabaseReference mMembersReference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.teamDB)
                    .child(selectedTeam)
                    .child(Constants.teamMembers);
            ValueEventListener mInvitesUsersValueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    final long childrenCount = dataSnapshot.getChildrenCount();
                    if (dataSnapshot.exists() && childrenCount > 0) {
                        final List<String> emailList = new ArrayList<>();
                        for (DataSnapshot dt : dataSnapshot.getChildren()) {
                            // Get email from UID
                            String userUID = dt.getKey();
                            DatabaseReference mEmailReference = FirebaseDatabase.getInstance().getReference()
                                    .child(Constants.userTeams)
                                    .child(userUID)
                                    .child(Constants.emailField);
                            ValueEventListener mEmailValueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String userEmail = (String) dataSnapshot.getValue();
                                    userEmail = Helpers.decodeEmailFromFirebase(userEmail);
                                    emailList.add(userEmail);
                                    if (Constants.DEBUG_MODE)
                                        System.out.println("emailList size: " + emailList.size() + ", childrenCount: " + childrenCount);
                                    if (childrenCount == emailList.size())
                                        emails.success(emailList);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    emails.fail(new ArrayList<String>());
                                }
                            };
                            mEmailReference.addListenerForSingleValueEvent(mEmailValueEventListener);
                            mEmailReference.removeEventListener(mEmailValueEventListener);
                        }
                    } else {
                        emails.success(new ArrayList<String>());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    emails.fail(new ArrayList<String>());
                }
            };
            mMembersReference.addListenerForSingleValueEvent(mInvitesUsersValueEventListener);
            mMembersReference.removeEventListener(mInvitesUsersValueEventListener);
        } else {
            emails.fail(new ArrayList<String>());
        }
    }

    public void invite(String userEmail) {
        String selectedTeam = Prefs.getString(Constants.selectedTeam, null);
        if (selectedTeam != null) {
            String encodedEmail = Helpers.encodeEmailForFirebase(userEmail);
            long time = new Date().getTime();
            FirebaseDatabase.getInstance().getReference()
                    .child(Constants.teamDB)
                    .child(selectedTeam)
                    .child(Constants.teamInvites)
                    .child(encodedEmail)
                    .setValue(time).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                    }
                }
            });
            FirebaseDatabase.getInstance().getReference()
                    .child(Constants.userInvites)
                    .child(encodedEmail)
                    .child(selectedTeam)
                    .setValue(time).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {

                    }
                }
            });
        }
    }
}
