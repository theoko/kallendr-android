package com.kallendr.android.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event {
    private String timeOfEvent;
    private String description;

    public String getTimeOfEvent() {
        return timeOfEvent;
    }

    public void setTimeOfEvent(Date timeOfEvent) {
        this.timeOfEvent = new SimpleDateFormat("HH:mm", Locale.US).format(timeOfEvent);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
