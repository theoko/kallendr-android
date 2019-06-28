package com.kallendr.android.data.model;

import java.text.SimpleDateFormat;

public class Event {
    private SimpleDateFormat timeOfEvent;
    private String description;

    public SimpleDateFormat getTimeOfEvent() {
        return timeOfEvent;
    }

    public void setTimeOfEvent(SimpleDateFormat timeOfEvent) {
        this.timeOfEvent = timeOfEvent;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
