package com.example.android.sunshine.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends Activity {

    private TextView mTime;
    private TextView mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTime = (TextView) stub.findViewById(R.id.time);
                mDate = (TextView) stub.findViewById(R.id.date);

                mTime.setText(getCurrentTime());
                mDate.setText(getCurrentDate());
            }
        });
    }

    private String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        DateFormat date = new SimpleDateFormat("HH:mm");
        date.setTimeZone(cal.getTimeZone());
        return date.format(cal.getTime());
    }

    private String getCurrentDate() {
        Calendar cal = Calendar.getInstance();
        DateFormat date = new SimpleDateFormat("EEE, MMM dd yyyy");
        date.setTimeZone(cal.getTimeZone());
        return date.format(cal.getTime()).toUpperCase();
    }
}
