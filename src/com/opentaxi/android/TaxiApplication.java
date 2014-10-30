package com.opentaxi.android;

import android.app.Application;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationRequest;
import com.opentaxi.android.service.CoordinatesService;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.models.CoordinatesLight;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 12/27/12
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
@ReportsCrashes(formKey = "dF8wOUJYbFhCeDVlMG1JT3FkN2xXM0E6MQ", logcatFilterByPid = true)
public class TaxiApplication extends Application {

    private static boolean havePlayService = true;
    private static boolean requestsVisible = false;
    private static boolean requestsHistory = false;
    private static boolean requestsDetailsVisible = false;
    private static boolean userPassVisible = false;
    private static boolean mapVisible = false;
    private static Integer lastRequestId;

    public static void setHavePlayService(boolean havePlayService) {
        TaxiApplication.havePlayService = havePlayService;
    }

    public static boolean isHavePlayService() {
        return havePlayService;
    }

    public static boolean isRequestsVisible() {
        return requestsVisible;
    }

    public static void requestsResumed() {
        requestsVisible = true;
    }

    public static void requestsPaused() {
        requestsVisible = false;
    }

    public static boolean isRequestsHistory() {
        return requestsHistory;
    }

    public static void requestsHistory(boolean history) {
        requestsHistory = history;
    }

    public static boolean isRequestsDetailsVisible() {
        return requestsDetailsVisible;
    }

    public static void requestsDetailsResumed() {
        requestsDetailsVisible = true;
    }

    public static void requestsDetailsPaused() {
        requestsDetailsVisible = false;
    }

    public static boolean isUserPassVisible() {
        return userPassVisible;
    }

    public static void userPassResumed() {
        userPassVisible = true;
    }

    public static void userPassPaused() {
        userPassVisible = false;
    }

    public static boolean isMapVisible() {
        return mapVisible;
    }

    public static void mapResumed() {
        mapVisible = true;
    }

    public static void mapPaused() {
        mapVisible = false;
    }

    public static Integer getLastRequestId() {
        return lastRequestId;
    }

    public static void setLastRequestId(Integer lastRequestId) {
        TaxiApplication.lastRequestId = lastRequestId;
    }

    private float SUFFICIENT_ACCURACY = 300; //meters
    private long UPDATE_LOCATION_INTERVAL = 30000; //millis
    private long FASTEST_LOCATION_INTERVAL = 10000; //millis

    @Override
    public void onCreate() {

        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        super.onCreate();

        //RestClient.getInstance().clearCache(); //todo temp

        AndroidGraphicFactory.createInstance(this);

        //Log.d("TaxiApplication", "onCreate()");

        // The following line triggers the initialization of ACRA
        ACRA.init(this);

        /*// output debug to LogCat, with tag LittleFluffyLocationLibrary
        //LocationLibrary.showDebugOutput(true);

        // in most cases the following initialising code using defaults is probably sufficient:
        //
        // LocationLibrary.initialiseLibrary(getBaseContext(), "com.opentaxi.android.service");
        //
        // however for the purposes of the test app, we will request unrealistically frequent location broadcasts
        // every 1 minute, and force a location update if there hasn't been one for 2 minutes.
        LocationLibrary.initialiseLibrary(getBaseContext(), 30 * 1000, 60 * 1000, "com.opentaxi.android");
        LocationLibrary.useFineAccuracyForRequests(true);
        LocationLibrary.showDebugOutput(true);
        //LocationLibrary.forceLocationUpdate(getBaseContext());
        LocationLibrary.startAlarmAndListener(getBaseContext());*/

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getApplicationContext()) == ConnectionResult.SUCCESS) {

            LocationRequest request = LocationRequest.create() //standard GMS LocationRequest
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(UPDATE_LOCATION_INTERVAL)
                    .setFastestInterval(FASTEST_LOCATION_INTERVAL);

            ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(this.getApplicationContext());
            Subscription subscription = locationProvider.getUpdatedLocation(request)
                    .filter(new Func1<Location, Boolean>() {
                        @Override
                        public Boolean call(Location location) {
                            return location.getAccuracy() < SUFFICIENT_ACCURACY;
                        }
                    })    // you can filter location updates
                    .subscribe(new Action1<Location>() {
                        @Override
                        public void call(Location location) {
                            doObtainedLocation(location);
                        }
                    });
        }
    }

    private void doObtainedLocation(Location location) {
        try {
            CoordinatesLight coordinates = new CoordinatesLight();
            coordinates.setN(location.getLatitude());
            coordinates.setE(location.getLongitude());
            coordinates.setT(location.getTime());
            Intent i = new Intent(this, CoordinatesService.class);
            i.putExtra("coordinates", coordinates);
            startService(i);

            if (AppPreferences.getInstance() != null) {

                Date now = new Date();
                AppPreferences.getInstance().setNorth(location.getLatitude());
                AppPreferences.getInstance().setEast(location.getLongitude());
                AppPreferences.getInstance().setCurrentLocationTime(location.getTime());
                AppPreferences.getInstance().setGpsLastTime(now.getTime());
            }
            Log.i("doObtainedLocation", "onReceive: received location update:" + location.getLatitude() + ", " + location.getLongitude());
        } catch (Exception e) {
            Log.e("doObtainedLocation", "onReceive:" + e.getMessage());
        }
    }
}