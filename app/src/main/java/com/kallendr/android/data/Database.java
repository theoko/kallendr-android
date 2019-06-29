package com.kallendr.android.data;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kallendr.android.data.model.Event;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.EventCallback;

import java.util.Date;
import java.util.List;

public class Database {
    private static Database INSTANCE = null;
    private FirebaseDatabase firebaseDatabase;

    private Database() {}

    public static Database getInstance()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new Database();
        }
        return INSTANCE;
    }

    public static void getEvents(Date date, final EventCallback eventCallback)
    {
        FirebaseDatabase.getInstance().getReference().child(Constants.eventsDB)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot event : dataSnapshot.getChildren())
                        {
                            Event thisEvent = event.getValue(Event.class);
                            System.out.println(thisEvent.getDescription());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }
}
