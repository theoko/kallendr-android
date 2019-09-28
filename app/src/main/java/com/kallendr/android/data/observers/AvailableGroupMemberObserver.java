package com.kallendr.android.data.observers;

import android.util.Log;

import com.kallendr.android.helpers.interfaces.Result;
import com.kallendr.android.data.model.AvailableGroupMember;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static com.kallendr.android.helpers.Constants.DEBUG_MODE;

public class AvailableGroupMemberObserver implements PropertyChangeListener {
    Result<Boolean> booleanResult;
    public AvailableGroupMemberObserver(AvailableGroupMember availableGroupMember, Result<Boolean> booleanResult) {
        availableGroupMember.addChangeListener(this);
        this.booleanResult = booleanResult;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (DEBUG_MODE) {
            Log.d(getClass().getName(), "Changed property: " + event.getPropertyName() + " [old -> "
                    + event.getOldValue() + "] | [new -> " + event.getNewValue() + "]");
        }
        String b = (String) event.getNewValue();
        booleanResult.success(Boolean.valueOf(b));
    }
}
