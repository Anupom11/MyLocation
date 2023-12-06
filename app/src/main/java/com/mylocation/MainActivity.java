package com.mylocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.concurrent.ExecutionException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit_client.RetrofitClient;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerList;
    EditText latText, longText;
    Button fetchLoc, submitDetails;

    private int REQUEST_CODE_PERMISSIONS = 1001;

    private final String[] REQUIRED_PERMISSIONS = new String[] {
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.ACCESS_COARSE_LOCATION"
    };

    private Location currentLocation;

    String spinnerSelected = "", latitude = "", longitude="";   // user data

    private final Timer timer1 = new Timer();

    private static final String[] spinnerArrayList = {"Select Spinner value", "Spinner 1", "Spinner 2", "Spinner 3", "Spinner 4", "Spinner 5"};

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerList     = findViewById(R.id.spinnerlist);
        latText         = findViewById(R.id.latEditText);
        longText        = findViewById(R.id.longEditText);
        fetchLoc        = findViewById(R.id.fetch_loc);
        submitDetails   = findViewById(R.id.submit);

        //-----------------------------------
        if(allPermissionsGranted()) {
            if(!checkGpsStatus()) {
                promptGPSEnableOp();
            }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
        //-----------------------------------

        //-----------------------------------------------------------------------------------------
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, spinnerArrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerList.setAdapter(adapter);
        spinnerList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //System.out.println("Position:"+position+" Long:"+id+" SpinnerSelected::"+spinnerArrayList[position]);
                if(position > 0) {
                    spinnerSelected = spinnerArrayList[position];
                }
                else if(id == 0) {
                    Toast.makeText(MainActivity.this, "Please select a different option!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //-----------------------------------------------------------------------------------------

        fetchLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(allPermissionsGranted()) {
                    if(checkGpsStatus()) {
                        //-------------------------------------------------------------------------------------------------------------------------
                        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        LocationListener locationListener = new MyLocationListener();
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                        //-------------------------------------------------------------------------------------------------------------------------
                    }
                    else {
                        //Toast.makeText(getApplicationContext(), "Turn on the GPS", Toast.LENGTH_LONG).show();
                        promptGPSEnableOp();
                    }
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
                }
            }
        });

        submitDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(longitude.length() > 0 && latitude.length() > 0 && spinnerSelected.length() > 0) {
                    doSubmitData(longitude, latitude, spinnerSelected);
                }
                else {
                    Toast.makeText(MainActivity.this, "Please add the required details!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }
    //*** end of oncreate ***

    // handle the GPS turn on operation
    private void promptGPSEnableOp() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Info");
        alertBuilder.setMessage("You have to turn on the location...")
                .setCancelable(false)
                .setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                        dialogInterface.cancel();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(timer1!=null)
                            timer1.cancel();

                        dialogInterface.cancel();

                        finish();
                    }
                });

        AlertDialog alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    public boolean checkGpsStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;
        boolean GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return GpsStatus;
    }

    private boolean allPermissionsGranted() {
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                if(!checkGpsStatus()) {
                    promptGPSEnableOp();
                }
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
        else {
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_LONG).show();
        }
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            //------------------------------------
            currentLocation = loc;
            //------------------------------------

            DecimalFormat numberFormat = new DecimalFormat("#.00000");

            longitude   = String.valueOf(numberFormat.format(loc.getLongitude()));
            latitude    = String.valueOf(numberFormat.format(loc.getLatitude()));

            if(longitude.length() > 0 && latitude.length() > 0) {
                latText.setText(latitude);
                longText.setText(longitude);
            }
            else {
                printMessage();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    }

    public void printMessage() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Getting GPS coordinate...", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void doSubmitData(String longitude, String latitude, String spinnerSelected) {
        Call<ResponseBody> submitDataCall = RetrofitClient
                .getInstance()
                .getApi()
                .submitData(longitude, latitude, spinnerSelected);

        submitDataCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if(response.isSuccessful()) {
                        String responseBody = response.body().string();
                        Log.e("My Location response", "onResponse: "+responseBody);

                        //JSONArray jsonArray = new JSONArray(responseBody);
                    }
                    else {
                        System.out.println("Response is not successfully");
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                System.out.println("Failed: "+call);
                t.printStackTrace();
            }
        });
    }


}