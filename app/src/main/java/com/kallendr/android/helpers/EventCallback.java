package com.kallendr.android.helpers;

import com.kallendr.android.data.model.Event;

import java.util.List;

public interface EventCallback {
    void onSuccess(List<Event> eventList);
    void onFail(String message);
}
