package com.kallendr.android.helpers.interfaces;

import com.kallendr.android.data.model.LocalEvent;

import java.util.List;

public interface EventCallback {
    void onSuccess(List<LocalEvent> eventList);
    void onFail(String message);
}
