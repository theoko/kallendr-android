package com.kallendr.android.helpers;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    /**
     * Performs fade out animation
     *
     * @param ctx
     * @param target
     * @return
     */
    public static Animation runFadeOutAnimationOn(Activity ctx, View target) {
        Animation animation = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_out);
        target.startAnimation(animation);
        return animation;
    }

    /**
     * Performs fade in animation
     *
     * @param ctx
     * @param target
     * @return
     */
    public static Animation runFadeInAnimationOn(Activity ctx, View target) {
        Animation animation = AnimationUtils.loadAnimation(ctx, android.R.anim.fade_in);
        target.startAnimation(animation);
        return animation;
    }

    /**
     * This method returns a date as a string in readable format.
     * @param timeInMillis
     * @return string that represents a date in readable format
     */
    public static String getReadableDateForMillis(long timeInMillis) {
        Calendar calInstance = Calendar.getInstance();
        calInstance.setTimeInMillis(timeInMillis);
        String formattedTime;
        Format formatter;
        formatter = new SimpleDateFormat("EEEE MMMM dd, yyyy");
        formattedTime = formatter.format(calInstance.getTime());
        return formattedTime;
    }

}
