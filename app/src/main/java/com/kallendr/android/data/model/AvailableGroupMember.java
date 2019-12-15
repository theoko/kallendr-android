package com.kallendr.android.data.model;

import com.kallendr.android.data.Database;
import com.kallendr.android.helpers.interfaces.Result;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class AvailableGroupMember {
    private List<PropertyChangeListener> listener = new ArrayList<>();
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
                setAvailable(arg);
            }

            @Override
            public void fail(Boolean arg) {

            }
        });
    }

    public String getEmail() {
        return email;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        notifyListeners(
                this,
                String.valueOf(available),
                String.valueOf(this.available),
                String.valueOf(this.available = available)
        );
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private void notifyListeners(Object object, String property, String oldValue, String newValue) {
        for (PropertyChangeListener name : listener) {
            name.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }

    public void addChangeListener(PropertyChangeListener newListener) {
        listener.add(newListener);
    }
}
