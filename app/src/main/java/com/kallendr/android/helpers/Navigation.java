package com.kallendr.android.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.widget.TextView;

import com.kallendr.android.R;
import com.kallendr.android.data.Database;
import com.kallendr.android.ui.calendar.CalendarMainActivity;
import com.kallendr.android.ui.settings.SettingsActivity;
import com.kallendr.android.ui.team.ManageTeamActivity;

/**
 * Intent.FLAG_ACTIVITY_SINGLE_TOP: This flag brings back the desired activity in case is already running
 */

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

    public static void backPressed(final Context ctx, DrawerLayout drawerLayout) {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            // Close drawer in case it is open
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Ask if user wants to exit the app
            AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
            alert.setTitle("Are you sure that you want to exit " + Constants.APP_NAME + "?");
            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
//                    Database.getInstance().saveBeforeExit();
                    dialog.dismiss();
                    android.os.Process.killProcess(android.os.Process.myPid());
                }
            });
            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alert.show();
        }
    }

    public static void populateNav(TextView nameTxt, TextView userTxt) {
        // Get user info from Firebase

    }
}
