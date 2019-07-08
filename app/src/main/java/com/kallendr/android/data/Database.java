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
     * This method adds a member (by their UID) to a team
     *
     * @param teamID
     * @param uid
     */
    public void addMemberToTeam(String teamID, String uid) {
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.teamDB)
                .child(teamID)
                .child(Constants.teamMembers)
                .push()
                .setValue(uid);
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
                            for (String email : emails) {
                                // This will generate a random ID as key to the new child
                                // and will push the email to the list of invites
                                mTeamEmailsReference.child(Constants.teamInvites).push().setValue(email);
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
     * This method returns the teams that the user belongs to
     */
    public void getTeamStatus(final Result<List<Team>> listOfTeamIDs) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mTeamsUserBelongsTo = FirebaseDatabase.getInstance().getReference()
                .child(Constants.teamDB);
        mTeamsUserBelongsTo.orderByKey().startAt(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Team> resultList = new ArrayList<>();
                        for (DataSnapshot dt : dataSnapshot.getChildren()) {
                            String teamID = dt.getKey();
                            if (Constants.DEBUG_MODE)
                                System.out.println("KEY: " + teamID);

                            String teamName = (String) dt.child(Constants.teamName).getValue();
                            long membersCount = dt.child(Constants.teamMembers).getChildrenCount();
                            Team team = new Team();
                            team.setTeamID(teamID);
                            team.setTeamName(teamName);
                            if (membersCount == 1) {
                                team.setDescription(membersCount + " member");
                            } else {
                                team.setDescription(membersCount + " members");
                            }
                            resultList.add(team);
                        }
                        if(Constants.DEBUG_MODE)
                            System.out.println("listOfTeamIDs.success()");
                        listOfTeamIDs.success(resultList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        listOfTeamIDs.fail(new ArrayList<Team>());
                    }
                });
    }

    /**
     * This method is responsible for getting the events of a team that are within the range of the provided dates
     *
     * @param startTimeInMillis
     * @param endTimeInMillis
     * @param eventCallback
     */
    public void getTeamEvents(final long startTimeInMillis, final long endTimeInMillis, final EventCallback eventCallback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // Get team members
        DatabaseReference mMembersReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.teamDB)
                .child(uid)
                .child(Constants.teamMembers);
        // Get events of members
    }
}
