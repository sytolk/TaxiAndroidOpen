/*
package com.opentaxi.android.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.opentaxi.rest.RestClient;


public class GCMRegisterService extends IntentService {

    public GCMRegisterService() {
        super("GCMRegisterService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        String regId = intent.getStringExtra("regId");
        boolean success = RestClient.getInstance().gcmRegister(regId);
        if (!success) Log.e("GCMRegisterService", "gcm not registered on server regId:" + regId);
    }
}
*/
