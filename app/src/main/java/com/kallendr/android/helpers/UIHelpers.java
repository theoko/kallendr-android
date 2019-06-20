package com.kallendr.android.helpers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;

import java.util.regex.Pattern;

public class UIHelpers {

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static boolean isValidTeamName(CharSequence target) {
        return (!TextUtils.isEmpty(target) && !Pattern.compile("[,\\s]|@.*@").matcher(target).find());
    }

    public static int spToPx(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, context.getResources().getDisplayMetrics());
    }

}
