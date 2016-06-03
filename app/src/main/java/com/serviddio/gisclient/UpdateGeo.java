package com.serviddio.gisclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


//attenzione il service opera nello stesso processo principale dell'applicazione in cui si trova l'activity principale (main thread)
//per evitare un forte crollo delle performance quando lavoriamo con l'interfaccia conviene delegare ad un altro thread azione di lunga durata specialmente
// se l'interazione risulta di tipo bloccante


//ci sono due tipi di service class, LA Service che gestisce le operazioni nel main thread e la IntentService  che usa un worker thread
//per gestire le operazioni in sequenza (non simultaneamente) è una sottoclasse della prima

public class UpdateGeo extends Service {

    private static final int INTERVAL = 5;

    public static final String
            ACTION_LOCATION_BROADCAST = UpdateGeo.class.getName() + "LocationBroadcast",
            EXTRA_LATITUDE = "extra_latitude",
            EXTRA_LONGITUDE = "extra_longitude";


    private LocationManager locationManager;
    private LocationListener locationListener;
    private String Tag = "GeoPos";


    @Override
    public void onCreate() {
        super.onCreate();

        //richiede una istanza del location manager al sistema
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //possiamo fare una query a tutti location provider per conoscere l'ultima location user nota

        //Register/unregister for periodic updates of the user's current location from a
        // location provider (specified either by criteria or name).
        //Register/unregister for a given Intent to be fired
        // if the device comes within a given proximity (specified by radius in meters) of a given lat/long.

        String locationProvider = LocationManager.NETWORK_PROVIDER;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        final Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        Log.i("Lastknowlocation", " LastKnowLocation value: " + lastKnownLocation);
        sendBroadcastMessage(lastKnownLocation);




        /*

        Getting user location in Android works by means of callback.
        You indicate that you'd like to receive location updates
        from the LocationManager ("Location Manager") by calling requestLocationUpdates(),
        passing it a LocationListener. Your LocationListener must
        implement several callback methods that the Location Manager calls
        when the user location changes or when the status of the service changes.

         */

        locationListener = new LocationListener() {


            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {
                // Do work with new location. Implementation of this method will be covered later.
                sendBroadcastMessage(location);

                if (lastKnownLocation == null)
                    Log.e("lastKnowLoc", "LastKnownLocation is null");
                else
                    Log.e("lastKnowLoc", "LastKnownLocation is " + lastKnownLocation.toString());
                if (isBetterLocation(lastKnownLocation, location)) {
                    Log.i("Update lastknowlocation", "Update LastKnowLocation with: " + location);
                    sendBroadcastMessage(location);


                }
            }
        };




    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // The service is starting, due to a call to startService()
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.e("Errore", "Errore permessi");
            return START_STICKY;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4, 0, locationListener);


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4, 0, locationListener);

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.removeUpdates(locationListener);
    }






    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //"Not yet implemented
        return null;



    }

    private void sendBroadcastMessage(Location location) {
        if (location != null) {
            Log.d(Tag, "Update GeoPosition Lat: " + location.getLatitude() + " e Long: " + location.getLongitude());
            Intent intent = new Intent(ACTION_LOCATION_BROADCAST);
            intent.putExtra(EXTRA_LATITUDE, location.getLatitude());
            intent.putExtra(EXTRA_LONGITUDE, location.getLongitude());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }


    protected boolean isBetterLocation(Location location, Location currentBestLocation) {

        if (currentBestLocation == null) {
            return true;
        }

        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantNewer = timeDelta > INTERVAL;
        boolean isSignificantOlder = timeDelta < -INTERVAL;
        boolean isNewer = timeDelta > 0;

        if (isSignificantNewer) {
            return true;

        } else if (isSignificantOlder) {
            return false;
        }

        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;

    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}
