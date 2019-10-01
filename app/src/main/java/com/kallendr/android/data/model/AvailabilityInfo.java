package com.kallendr.android.data.model;

public class AvailabilityInfo {
    long start;
    long end;
    String message;
    Boolean available;

    public AvailabilityInfo(long start, long end, Boolean available) {
        this.start = start;
        this.end = end;
        this.available = available;
    }

    public String getMessage() {

        return message;
    }
}
