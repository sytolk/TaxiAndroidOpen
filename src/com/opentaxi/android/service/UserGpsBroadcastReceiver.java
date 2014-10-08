/*
package com.opentaxi.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.models.CoordinatesLight;

import java.util.Date;

public class UserGpsBroadcastReceiver extends BroadcastReceiver {

    static final String TAG = "UserGpsBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        final LocationInfo locationInfo = (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
        if (locationInfo != null && locationInfo.anyLocationDataReceived()) {
            //locationInfo.refresh(context);
           // new SendCoordinatesTask(locationInfo.lastLat, locationInfo.lastLong, locationInfo.lastLocationUpdateTimestamp).execute(context);
            CoordinatesLight coordinates  = new CoordinatesLight();
            coordinates.setN(locationInfo.lastLat);
            coordinates.setE(locationInfo.lastLong);
            coordinates.setT(locationInfo.lastLocationUpdateTimestamp);
            Intent i = new Intent(context, CoordinatesService.class);
            i.putExtra("coordinates", coordinates);
            context.startService(i);

            if (AppPreferences.getInstance() != null) {

                Date now = new Date();
                AppPreferences.getInstance().setNorth((double) locationInfo.lastLat);
                AppPreferences.getInstance().setEast((double) locationInfo.lastLong);
                AppPreferences.getInstance().setCurrentLocationTime(locationInfo.lastLocationBroadcastTimestamp);
                AppPreferences.getInstance().setGpsLastTime(now.getTime());
            }
            Log.i("LocationBroadcastReceiver", "onReceive: received location update:" + locationInfo.lastLat + ", " + locationInfo.lastLong);
        } else Log.e(TAG, "onReceive: anyLocationDataReceived=false");
    }
}*/
