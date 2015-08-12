package com.example.android.sunshine.app;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.example.android.sunshine.app.data.WeatherContract;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public class WearService extends IntentService implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String ACTION_SEND_WEAR_DATA = "com.example.android.sunshine.app.ACTION_SEND_WEAR_DATA";

    private static final String PATH = "/weather";
    private static final String KEY_WEATHER_ID = "key_weather_id";
    private static final String KEY_MAX_TEMP = "key_max_temp";
    private static final String KEY_MIN_TEMP = "key_min_temp";

    private GoogleApiClient mGoogleApiClient;
    private PutDataMapRequest mRequestMap;

    public WearService() {
        super("WearService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && ACTION_SEND_WEAR_DATA.equals(intent.getAction())) {
            mGoogleApiClient = new GoogleApiClient.Builder(WearService.this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Wearable.API)
                    .build();
            mGoogleApiClient.connect();
            Log.d("WearService", "action_send_wear_data");
            updateWearable();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        updateWearable();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private void updateWearable() {
        String locationQuery = Utility.getPreferredLocation(this);
        Uri weatherUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationQuery, System.currentTimeMillis());
        Cursor c = getContentResolver().query(
                weatherUri,
                new String[]{ WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                        WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                        WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
                }, null, null, null);
        if (c.moveToFirst()) {
            int weatherId = c.getInt(c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_WEATHER_ID));
            String maxTemp = Utility.formatTemperature(this, c.getDouble(
                    c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP)));
            String minTemp = Utility.formatTemperature(this, c.getDouble(
                    c.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP)));
            mRequestMap = PutDataMapRequest.create(PATH);
            mRequestMap.getDataMap().putInt(KEY_WEATHER_ID, weatherId);
            mRequestMap.getDataMap().putString(KEY_MAX_TEMP, maxTemp);
            mRequestMap.getDataMap().putString(KEY_MIN_TEMP, minTemp);
            Log.d("WearService", "weatherId: "+weatherId+" maxTemp: "+maxTemp+" minTemp: "+minTemp);

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d("WearService", "PendingResult DataApi");
                    PendingResult<DataApi.DataItemResult> result =
                            Wearable.DataApi.putDataItem(mGoogleApiClient, mRequestMap.asPutDataRequest());
                }
            });
            thread.start();
        }
        c.close();
    }

    @Override
    public void onDestroy() {
        mGoogleApiClient.disconnect();
        super.onDestroy();
    }

}
