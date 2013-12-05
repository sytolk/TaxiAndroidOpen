package com.opentaxi.android;

import android.app.Application;
import android.util.Log;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 12/27/12
 * Time: 2:02 PM
 * To change this template use File | Settings | File Templates.
 */
@ReportsCrashes(formKey = "dF8wOUJYbFhCeDVlMG1JT3FkN2xXM0E6MQ", logcatFilterByPid = true)
public class TaxiApplication extends Application {

    private static boolean requestsVisible=false;
    private static boolean requestsDetailsVisible=false;

    public static boolean isRequestsVisible() {
        return requestsVisible;
    }

    public static void requestsResumed() {
        requestsVisible = true;
    }

    public static void requestsPaused() {
        requestsVisible = false;
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

    @Override
    public void onCreate() {

        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        super.onCreate();

        Log.d("TaxiApplication", "onCreate()");

        // The following line triggers the initialization of ACRA
        ACRA.init(this);

        // output debug to LogCat, with tag LittleFluffyLocationLibrary
        //LocationLibrary.showDebugOutput(true);

        // in most cases the following initialising code using defaults is probably sufficient:
        //
        // LocationLibrary.initialiseLibrary(getBaseContext(), "com.opentaxi.android.service");
        //
        // however for the purposes of the test app, we will request unrealistically frequent location broadcasts
        // every 1 minute, and force a location update if there hasn't been one for 2 minutes.
        LocationLibrary.initialiseLibrary(getBaseContext(), 30 * 1000, 60 * 1000, "com.opentaxi.android");
        LocationLibrary.useFineAccuracyForRequests(true);
        //LocationLibrary.forceLocationUpdate(getBaseContext());
        LocationLibrary.startAlarmAndListener(getBaseContext());
    }
}