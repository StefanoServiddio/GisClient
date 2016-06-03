package com.serviddio.gisclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class WGisMenu extends AppCompatActivity {

    private String user="";
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private BroadcastReceiver br;

    private ToggleButton toggleOnOff;
    private final String url="http://192.168.1.7:9925/Wgis/servRest/geopos";

    private Intent i;
    private UpdatePosTask upt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gis_menu_android);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mLatitudeText = (TextView)findViewById(R.id.LatPos);
        mLongitudeText = (TextView) findViewById(R.id.LongPos);
        toggleOnOff = (ToggleButton)findViewById(R.id.toggleButton);
       //PendingIntent pi=PendingIntent.getBroadcast();

        user=(String)getIntent().getStringExtra("user");




        br =  new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double latitude = intent.getDoubleExtra(UpdateGeo.EXTRA_LATITUDE, 0);
                double longitude = intent.getDoubleExtra(UpdateGeo.EXTRA_LONGITUDE, 0);
                Log.d("Activity Pos","Latitude"+latitude+" e ricevo Long: "+ longitude);

                mLatitudeText.setText("Lat: "+latitude);
                mLongitudeText.setText("Long: " + longitude );
                upt= new UpdatePosTask(String.valueOf(latitude), String.valueOf(longitude));
                upt.execute(url);

            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(br
               , new IntentFilter(UpdateGeo.ACTION_LOCATION_BROADCAST)
        );



        i=new Intent(this,UpdateGeo.class);
        toggleOnOff.setChecked(true);
        toggleOnOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    stopService(i);



                    mLatitudeText.setText("Lat: ");
                    mLongitudeText.setText("Long: ");

                    Log.i("Toogle", "Button true");
                } else {
                    // The toggle is disabled
                    startService(i);

                    Log.i("Toogle","Button false");
                }
            }
        });




    }


    public class UpdatePosTask extends AsyncTask<String, Void, Void> {

        private final String mLatitudeText;
        private final String mLongitudeText;

        UpdatePosTask(String mLatitudeText, String mLongitudeText) {
            this.mLatitudeText = mLatitudeText;
            this.mLongitudeText= mLongitudeText;
        }

        @Override
        protected Void doInBackground(String... url) {
            // TODO: attempt authentication against a network service.

            try {


                URL targetUrl = new URL(url[0]);


                HttpURLConnection httpConnection = (HttpURLConnection) targetUrl.openConnection();

                httpConnection.setDoOutput(true);

                httpConnection.setRequestMethod("POST");

                httpConnection.setRequestProperty("Content-Type", "application/json");
                httpConnection.setRequestProperty("Accept", "application/json");

                JSONObject input = new JSONObject().put("user_email",user).put("user_lat", mLatitudeText).put("user_long", mLongitudeText);
                Log.e("UpdatePosServer","Send New Position to Server"+input.toString());


                OutputStream outputStream = httpConnection.getOutputStream();

                DataOutputStream data = new DataOutputStream(outputStream);
                data.writeBytes(input.toString());
                data.flush();
                data.close();
                Log.d("http Request", " Http Sended: " + input.toString());

                if (httpConnection.getResponseCode() != 200) {
                    Log.e("http Error", "Error http: " + httpConnection.getResponseCode());

                    throw new RuntimeException("Failed : HTTP error code : "

                            + httpConnection.getResponseCode());

                }
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        (httpConnection.getInputStream())));
                String output=br.readLine();

                while ( br.readLine() != null) {
                    output= output+br.readLine();
                }
                Log.d("Ricevuto","Ricevuto: "+output);
                JSONObject objOutput=new JSONObject(output);

                if(objOutput.getString("ack").equals("true")){
                    httpConnection.disconnect();
                    Log.e("UpdatePosServer","Update Postion Success ");
                }
                httpConnection.disconnect();
                Log.e("UpdatePosServer","Update Postion Error");



            } catch (MalformedURLException e) {


                e.printStackTrace();



            } catch (IOException e) {


                e.printStackTrace();


            } catch (JSONException e) {
                e.printStackTrace();

            }

            return null;

        }





    }




}

