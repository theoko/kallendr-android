package com.kallendr.android.ui.calendar;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.kallendr.android.R;

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

            }
        });
    }

}
