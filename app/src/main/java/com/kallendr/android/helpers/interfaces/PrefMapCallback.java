package com.kallendr.android.helpers.interfaces;

import java.util.Map;

public interface PrefMapCallback {
    void onSuccess(Map<String, Boolean> prefMap);

    void onFail(String message);
}
