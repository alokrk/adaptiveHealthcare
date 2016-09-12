package com.soc.adaptivehealthcare;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "value of url = ";
    //private static final int MY_PERMISSION_ACCESS_COURSE_LOCATION = 11;

    private float bar, lux, dur;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    //LocationManager mLocation;

    private TextView mLatitudeText;
    private TextView mLongitudeText;

    double latitude;
    double longitude;

    SensorManager mSensorManager;
    //SensorEventListener listener;
    Sensor mLight;
    Sensor mPressure;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        if (mPressure != null) {
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, "Barometer present", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, "Barometer absent. Assume pressure < 29.5", Toast.LENGTH_SHORT);
            bar = 0;
            toast.show();
        }

        if (mLight != null) {
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, "Light sensor present", Toast.LENGTH_LONG);
            toast.show();
        } else {
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, "No light sensor. Assume light < 400 lux", Toast.LENGTH_LONG);
            lux = 0;
            toast.show();
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.d(TAG, "Connection set");
        }
        catch(SecurityException e){
            Log.d(TAG, "Error finding location");
        }

        //Log.d(TAG,"longitude" + (String.valueOf(mLastLocation.getLatitude())));
        if (mLastLocation != null) {

            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));

            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            //Log.d(TAG, "latitude = " + (String.valueOf(mLastLocation.getLatitude())));

            try {
                dur = GetDistance(latitude, longitude);
            }
            catch(IOException ie){
                ie.printStackTrace();
            }
        }
    }

    //http://stackoverflow.com/questions/6456090/android-google-map-finding-distance/6456161#6456161
    public float GetDistance(Double latitude, Double longitude) throws IOException{

        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/distancematrix/json?");

        urlString.append("origins=");//from
        urlString.append(Double.toString(latitude));
        urlString.append(",");
        urlString.append(Double.toString(longitude));

        //Coordinates for REX Hospital hardcoded as per discussions in a lecture
        urlString.append("&destinations=");//to
        urlString.append("35.8178743");
        urlString.append(",");
        urlString.append("-78.7047277");

        urlString.append("&key=AIzaSyAXUZV0ncmTvFTvzcsq5RrbrhY2lNIg4q0");

        Log.i(TAG, urlString.toString());

        HttpURLConnection urlConnection = null;
        URL url = null;

        url = new URL(urlString.toString());
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        urlConnection.connect();

        InputStream inStream = urlConnection.getInputStream();
        BufferedReader bReader = new BufferedReader(new InputStreamReader(inStream));

        String temp, response = "";
        while ((temp = bReader.readLine()) != null) {
            response += temp;
        }

        bReader.close();
        inStream.close();
        urlConnection.disconnect();

        //Response
        try {

            JSONObject object = (JSONObject) new JSONTokener(response).nextValue();

            JSONArray array = object.getJSONArray("rows");
            JSONObject rows = array.getJSONObject(0);

            JSONArray elements = rows.getJSONArray("elements");
            JSONObject distance = elements.getJSONObject(0);

            JSONObject duration = elements.getJSONObject(1);

            float value = duration.getInt("value");

            dur = value;

        }
        catch(JSONException e){
            e.printStackTrace();
        }
        return dur;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Connection not established");
    }

    @Override
    public void onSensorChanged (SensorEvent event){

        Sensor sensor = event.sensor;

        if (sensor.getType() == Sensor.TYPE_LIGHT) {
            lux = event.values[0];
            /*if (lux < 400) {
                sum = sum + 1;
            }*/
        }

        if (sensor.getType() == Sensor.TYPE_PRESSURE) {
            bar = event.values[0];
            //converting 29.5 inches of mercury to hPa as sensor returns value in hPa
            /*if (bar < 1000) {
                sum = sum + 1;
            }*/
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //not required for now
    }

    @Override
    protected void onStart() {

        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this, mLight);
        mSensorManager.unregisterListener(this, mPressure);
    }

    @Override
    protected void onStop() {

        mGoogleApiClient.disconnect();
        super.onStop();
    }

    public void loadData(View view) throws IOException{

        //Log.d(TAG, "" + dur);
        //Log.d(TAG, "" + lux);

        //bar = 200000;
        //lux = 500;
        //dur = 500;

        //threshold value of 29.5 converted to 998.9847 as sensor returns millibar
        if( (bar <= 998.9847) && (lux <= 400) && (dur <= 300)) {
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            startActivity(intent);
        }
        else {
            Context context = getApplicationContext();
            Toast toast = Toast.makeText(context, "Not an emergency", Toast.LENGTH_LONG);
            toast.show();
        }
    }
}