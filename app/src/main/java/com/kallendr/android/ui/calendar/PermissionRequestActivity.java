package com.kallendr.android.ui.calendar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.kallendr.android.R;
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
                        != PackageManager.PERMISSION_GRANTED)
                {
                    Helpers.requestReadCalendarPermission(PermissionRequestActivity.this);
                }
                returnToCalendarMainActivity();
            }
        });
    }

    private void returnToCalendarMainActivity()
    {
        if (ContextCompat.checkSelfPermission(PermissionRequestActivity.this, Manifest.permission.READ_CALENDAR)
                == PackageManager.PERMISSION_GRANTED)
        {
            // Return to previous activity
            Intent intent = new Intent(PermissionRequestActivity.this, CalendarMainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}
