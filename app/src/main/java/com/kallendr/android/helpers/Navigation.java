package com.kallendr.android.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.kallendr.android.R;
import com.kallendr.android.ui.calendar.CalendarMainActivity;
import com.kallendr.android.ui.settings.SettingsActivity;
import com.kallendr.android.ui.team.ManageTeamActivity;

public class Navigation {
    public static void selectedItem(Context context, DrawerLayout drawer, MenuItem menuItem) {
        int id = menuItem.getItemId();

        drawer.closeDrawer(GravityCompat.START);
        if (id == R.id.nav_home) {
            Intent calendarMainIntent = new Intent(context, CalendarMainActivity.class);
            calendarMainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            calendarMainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(calendarMainIntent);
        } else if (id == R.id.nav_settings) {
            Intent settingsIntent = new Intent(context, SettingsActivity.class);
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(settingsIntent);
        } else if (id == R.id.nav_manage_team) {
            Intent manageTeamIntent = new Intent(context, ManageTeamActivity.class);
            manageTeamIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            manageTeamIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(manageTeamIntent);
        }
    }

    public static void backPressed(DrawerLayout drawerLayout)
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }
}
