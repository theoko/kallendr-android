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
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.interfaces.EventCallback;
import com.kallendr.android.helpers.interfaces.FirstLoginCallback;
import com.kallendr.android.helpers.interfaces.PrefMapCallback;
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
     * This method will store the team's name to the database and
     * add the list of emails as child elements to the team that was just created
     *
     * @param emails
     */
    public void setTeamNameAndAddEmailsToInvitationList(ArrayList<String> emails) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String teamName = Prefs.getString(Constants.teamName, null);
        if (teamName == null) {
            // teamName cannot be null
            // If it is, something went completely wrong
            return;
        } else {
            DatabaseReference mTeamEmailsReference = FirebaseDatabase.getInstance().getReference()
                    .child(Constants.teamDB)
                    .child(uid);
            // Save the team name to the database
            mTeamEmailsReference.child(Constants.teamName)
                    .setValue(teamName);
            if (emails.size() > 0) {
                for (String email : emails) {
                    // This will generate a random ID as key to the new child
                    // and will push the email to the list of invites
                    mTeamEmailsReference.child(Constants.teamInvites).push().setValue(email);
                }
            }
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
    public void getTeamStatus() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

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
