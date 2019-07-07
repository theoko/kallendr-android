package com.kallendr.android.helpers.interfaces;

public interface Result<T> {
    void success(T arg);
    void fail(T arg);
}
