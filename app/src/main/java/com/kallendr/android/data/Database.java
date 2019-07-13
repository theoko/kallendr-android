package com.kallendr.android.data;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kallendr.android.data.model.Event;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static Database INSTANCE = null;
    private FirebaseDatabase firebaseDatabase;

    /**
     * Temp data holders
     */
    public List<LocalEvent> localEventsList;

    private Database() {
    }

    public static Database getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Database();
        }
        return INSTANCE;
    }

    /**
     * Checks if the user has logged in before
     *
     * @param firstLoginCallback
     */
    public void firstLogin(final FirstLoginCallback firstLoginCallback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference().child(Constants.userDB).child(uid);
        ValueEventListener mUserValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                    DatabaseReference firstLogin = mUserReference.child("firstLogin");
                    ValueEventListener firstLoginValueEventListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                firstLoginCallback.onFirstLogin(false);
                            } else {
                                // Check if user is invited to a team.
                                // In case they are, add them to that team
                                settleInvites();
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

    public void settleInvites() {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getInviteStatus(new Result<List<Team>>() {
            @Override
            public void success(List<Team> arg) {
                for (Team team : arg) {
                    String teamID = team.getTeamID();
                    addMemberToTeam(teamID, uid);
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
    public void onCalendarSetupComplete() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference().child(Constants.userDB).child(uid);
        // Updates database to indicate that the user has completed setup
        Map<String, Object> postValues = new HashMap<>();
        postValues.put("firstLogin", Constants.COMPLETE);
        mUserReference.updateChildren(postValues);
    }

    /**
     * Sets preferences for authenticated user
     *
     * @param prefMap
     */
    public void setPreferences(Map<String, Boolean> prefMap) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
    public void getPreferences(final PrefMapCallback prefMapCallback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
    public void addMemberToTeam(String teamID, String uid) {
        long time = new Date().getTime();
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.teamDB)
                .child(teamID)
                .child(Constants.teamMembers)
                .child(uid)
                .setValue(time);
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.userTeams)
                .child(uid)
                .child(teamID)
                .setValue(time);
    }

    /**
     * This method will store the team's name to the database and
     * add the list of emails as child elements to the team that was just created
     *
     * @param emails
     */
    public void setTeamNameAndAddEmailsToInvitationList(final ArrayList<String> emails, final Result<String> result) {
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                        addMemberToTeam(teamID, uid);
                        // Check if we have any invited users
                        if (emails.size() > 0) {
                            long time = new Date().getTime();
                            for (String email : emails) {
                                // This will generate a random ID as key to the new child
                                // and will push the email to the list of invites
                                mTeamEmailsReference.child(Constants.teamInvites).child(email).setValue(time);
                                FirebaseDatabase.getInstance().getReference()
                                        .child(Constants.userInvites)
                                        .child(email)
                                        .child(teamID)
                                        .setValue(time);
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
    public void uploadEvents(final List<LocalEvent> localEventsList) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
    public void getEvents(final long startTimeInMillis, final long endTimeInMillis, final EventCallback eventCallback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
    public void getTeamStatus(final Result<List<Team>> listOfTeamIDs) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mTeamsUserBelongsTo = FirebaseDatabase.getInstance().getReference()
                .child(Constants.userTeams)
                .child(uid);
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

                            // Get info for this team
                            Database.getInstance().getTeamInfo(teamID, new Result<Team>() {
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
                            listOfTeamIDs.success(resultList);
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
    public void getInviteStatus(final Result<List<Team>> listOfTeamIDs) {
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
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

                            Database.getInstance().getTeamInfo(teamID, new Result<Team>() {
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
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
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
                        for (DataSnapshot dt : dataSnapshot.getChildren()) {
                            String userID = dt.getKey();
                            Database.getInstance().getEventsForUID(userID, startTimeInMillis, endTimeInMillis, new EventCallback() {
                                @Override
                                public void onSuccess(List<LocalEvent> eventList) {
                                    // Events for this user

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
        }

    }
}
