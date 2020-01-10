package com.kallendr.android.ui.calendar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.kallendr.android.R;
import com.kallendr.android.helpers.Constants;
import com.kallendr.android.helpers.Helpers;

public class PermissionRequestActivity extends AppCompatActivity {

    private Button btn_allow_access_phone_calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.failed_accepting_permissions);

        btn_allow_access_phone_calendar = findViewById(R.id.btn_allow_access_phone_calendar);
        btn_allow_access_phone_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PermissionRequestActivity.this, Manifest.permission.READ_CALENDAR)
                        != PackageManager.PERMISSION_GRANTED) {
                    Helpers.requestReadCalendarPermission(PermissionRequestActivity.this);
                }
            }
        });
    }

    private void returnToCalendarMainActivity() {
        // Return to previous activity
        Intent intent = new Intent(PermissionRequestActivity.this, TeamCalendarActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.PERMISSIONS_REQUEST_READ_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    returnToCalendarMainActivity();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // Do nothing.
                }
                return;
            }
        }
    }
}
