package com.kallendr.android.data.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event {
    private String titleOfEvent;
    private String description;

    public String getTimeOfEvent() {
        return titleOfEvent;
    }

    public void setTitleOfEvent(String eventTitle, Date timeOfEvent) {
        this.titleOfEvent = eventTitle + " at ";
        this.titleOfEvent += new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(timeOfEvent);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
