package com.kallendr.android.data.model;

public class GroupMember {
    private String email;

    public GroupMember(String email) {
        this.email = email;
    }

    public boolean available(long startTime, long endTime) {

        // stub
        return true;
    }
}
