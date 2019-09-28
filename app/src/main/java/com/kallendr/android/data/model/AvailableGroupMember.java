package com.kallendr.android.data.model;

import com.kallendr.android.data.Database;
import com.kallendr.android.helpers.interfaces.Result;

public class AvailableGroupMember {
    private String email;
    private long startTime;
    private long endTime;
    private Boolean available = null;

    public AvailableGroupMember(String email, long startTime, long endTime) {
        this.email = email;
        this.startTime = startTime;
        this.endTime = endTime;
        available(startTime, endTime);
    }

    private void available(long startTime, long endTime) {
        Database.getInstance().userAvailable(this.email, startTime, endTime, new Result<Boolean>() {
            @Override
            public void success(Boolean arg) {
                available = arg;
            }

            @Override
            public void fail(Boolean arg) {

            }
        });
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}
