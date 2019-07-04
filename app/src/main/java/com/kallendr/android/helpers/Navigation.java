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

public class Navigation implements NavigationView.OnNavigationItemSelectedListener {
    public static void selectedItem(Context context, DrawerLayout drawer, MenuItem menuItem)
    {
        int id = menuItem.getItemId();

        drawer.closeDrawer(GravityCompat.START);
        if (id == R.id.nav_home) {
            Intent calendarMainIntent = new Intent(context, CalendarMainActivity.class);
            context.startActivity(calendarMainIntent);
        } else if (id == R.id.nav_settings) {
            Intent settingsIntent = new Intent(context, SettingsActivity.class);
            context.startActivity(settingsIntent);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
