package com.kallendr.android.data;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kallendr.android.data.model.Event;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.interfaces.EventCallback;
import com.kallendr.android.helpers.interfaces.FirstLoginCallback;
import com.kallendr.android.helpers.interfaces.PrefMapCallback;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private static Database INSTANCE = null;
    private FirebaseDatabase firebaseDatabase;

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
     * This method will add the list of emails as child elements to the team that was just created
     * @param emails
     */
    public void addEmailsToTeam(ArrayList<String> emails) {
        String teamName = Prefs.getString(Constants.teamName, null);
        if (teamName == null)
            return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mEmailsReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.teamDB)
                .child(uid)
                .child(Constants.teamInvites);
        for (String email : emails)
        {
            mEmailsReference.push().setValue(email);
        }
    }

    public void getEvents(Date date, final EventCallback eventCallback) {
        DatabaseReference mEventsReference = FirebaseDatabase.getInstance().getReference().child(Constants.eventsDB);
        ValueEventListener mEventsValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot event : dataSnapshot.getChildren()) {
                    Event thisEvent = event.getValue(Event.class);
                    System.out.println(thisEvent.getDescription());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mEventsReference.addListenerForSingleValueEvent(mEventsValueEventListener);
        mEventsReference.removeEventListener(mEventsValueEventListener);
    }
}
