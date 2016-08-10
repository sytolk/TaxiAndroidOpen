package com.opentaxi.android.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.opentaxi.android.TaxiApplication;
import com.opentaxi.rest.RestClient;

import java.io.IOException;


public class GCMRegisterService extends IntentService {

    public GCMRegisterService() {
        super("GCMRegisterService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        //Log.i("GCMRegisterService", "started");
        InstanceID iid = InstanceID.getInstance(getApplicationContext());
        try {
            String[] senderIds = RestClient.getInstance().getGCMsenderIds();
            if (senderIds != null) {
                for (String sender : senderIds) {
                    String token = iid.getToken(sender, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    Boolean success = RestClient.getInstance().gcmRegister(token);
                    if (success != null && !success) {
                        TaxiApplication.setGCMRegistrationId(token);
                        Log.e("GCMRegisterService", "gcm not registered on server regId:" + token);
                    } else Log.i("GCMRegisterService", "gcm registered regId:" + token);
                }
            } //else Log.i("GCMRegisterService", "not loged in");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // String regId = intent.getStringExtra("regId");
    }
}
