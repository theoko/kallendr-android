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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Database {
    private static Database INSTANCE = null;
    private FirebaseDatabase firebaseDatabase;

    private Database() {}

    public static Database getInstance()
    {
        if (INSTANCE == null) {
            INSTANCE = new Database();
        }
        return INSTANCE;
    }

    /**
     * Checks if the user has logged in before
     * @param firstLoginCallback
     */
    public void firstLogin(final FirstLoginCallback firstLoginCallback)
    {
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
                            if (dataSnapshot.exists())
                            {
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
    public void onCalendarSetupComplete()
    {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference().child(Constants.userDB).child(uid);
        Map<String, Object> postValues = new HashMap<>();
        postValues.put("firstLogin", Constants.COMPLETE);
        mUserReference.updateChildren(postValues);
    }

    /**
     * Sets preferences for authenticated user
     * @param prefMap
     */
    public void setPreferences(Map<String, Boolean> prefMap) {
        if (prefMap.get(Constants.allowNotifications) != null)
        {

        }
        if(prefMap.get(Constants.allowCalendarAccess) != null)
        {

        }
    }

    /**
     * Gets preferences for authenticated user from the server
     */
    public void getPreferences(final PrefMapCallback prefMapCallback)
    {
        Map<String, Boolean> returnPrefMap = new HashMap<>();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference mPrefReference = FirebaseDatabase.getInstance().getReference().child(Constants.userDB).child(uid);
        ValueEventListener mPrefValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mPrefReference.addListenerForSingleValueEvent(mPrefValueEventListener);
        mPrefReference.removeEventListener(mPrefValueEventListener);
    }

    public void getEvents(Date date, final EventCallback eventCallback)
    {
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
