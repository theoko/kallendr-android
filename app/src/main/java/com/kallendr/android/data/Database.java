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
import com.kallendr.android.helpers.EventCallback;
import com.kallendr.android.helpers.FirstLoginCallback;

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

    public void firstLogin(final FirstLoginCallback firstLoginCallback) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final DatabaseReference mUserReference = FirebaseDatabase.getInstance().getReference().child(Constants.userDB).child(uid);
        ValueEventListener mUserValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() > 0) {
                    firstLoginCallback.onFirstLogin(false);
                } else {
                    Map<String, Object> postValues = new HashMap<String,Object>();
                    postValues.put("firstLogin", "completed");
                    mUserReference.updateChildren(postValues);
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
