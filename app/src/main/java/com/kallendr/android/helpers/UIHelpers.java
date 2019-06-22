package com.kallendr.android.helpers;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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
     * @param ctx
     * @param target
     * @return
     */
    public static Animation runFadeOutAnimationOn(Activity ctx, View target) {
        Animation animation = AnimationUtils.loadAnimation(ctx,android.R.anim.fade_out);
        target.startAnimation(animation);
        return animation;
    }

    /**
     * Performs fade in animation
     * @param ctx
     * @param target
     * @return
     */
    public static Animation runFadeInAnimationOn(Activity ctx, View target) {
        Animation animation = AnimationUtils.loadAnimation(ctx,android.R.anim.fade_in);
        target.startAnimation(animation);
        return animation;
    }

}
