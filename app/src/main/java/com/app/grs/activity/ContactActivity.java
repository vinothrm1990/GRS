package com.app.grs.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.app.grs.R;
import com.app.grs.helper.GRS;

public class ContactActivity extends AppCompatActivity {

    TextView s1, s2, s3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Contact Us");
        setContentView(R.layout.activity_contact);

        s1 = findViewById(R.id.store1);
        s2 = findViewById(R.id.store2);
        s3 = findViewById(R.id.store3);

        s1.setSelected(true);
        s2.setSelected(true);
        s3.setSelected(true);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        GRS.freeMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        GRS.registerReceiver(ContactActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // For Internet disconnect checking
        GRS.unregisterReceiver(ContactActivity.this);
    }
}
