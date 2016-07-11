package com.opentaxi.android.gcm;

import android.content.Intent;
import com.google.android.gms.iid.InstanceIDListenerService;
import com.opentaxi.android.TaxiApplication;

/**
 * Created by stanimir on 12/31/15.
 */
public class InstanceIdListener extends InstanceIDListenerService {

    /**
     * Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
     */
    @Override
    public void onTokenRefresh() {
        //Log.i("onTokenRefresh", "Token:" + TaxiApplication.getGCMRegistrationId());
        if (TaxiApplication.getGCMRegistrationId() == null) {
            startService(new Intent(this, GCMRegisterService.class));
        }
    }
}
