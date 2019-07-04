package com.kallendr.android.helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;

import com.kallendr.android.R;
import com.kallendr.android.ui.settings.SettingsActivity;

public class Navigation {
    public static void selectedItem(Context context, DrawerLayout drawer, MenuItem menuItem)
    {
        int id = menuItem.getItemId();

        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(context, SettingsActivity.class);
            context.startActivity(intent);
        }

        drawer.closeDrawer(GravityCompat.START);
    }
}
