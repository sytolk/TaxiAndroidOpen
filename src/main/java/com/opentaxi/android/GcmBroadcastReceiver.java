/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *//*

package com.opentaxi.android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import com.opentaxi.android.gcm.GCMRegisterService;


*/
/**
 * This {@code WakefulBroadcastReceiver} takes care of creating and managing a
 * partial wake lock for your app. It passes off the work of processing the GCM
 * message to an {@code IntentService}, while ensuring that the device does not
 * go back to sleep in the transition. The {@code IntentService} calls
 * {@code GcmBroadcastReceiver.completeWakefulIntent()} when it is ready to
 * release the wake lock.
 *//*


public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            String regId = intent.getExtras().getString("registration_id");
            if (regId != null && !regId.equals("") && !regId.equals(TaxiApplication.gcmId)) {
                TaxiApplication.gcmId = regId;
      */
/* Do what ever you want with the regId eg. send it to your server *//*

                Log.i("GcmBroadcastReceiver", "onReceive:" + regId);
                //RestClient.getInstance().gcmRegister(regId);
                Intent i = new Intent(context, GCMRegisterService.class);
                i.putExtra("regId", regId);
                context.startService(i);
            }
        }

        // Explicitly specify that GcmIntentService will handle the intent.
        ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
        // Start the service, keeping the device awake while it is launching.
        if (intent != null) startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
    }
}
*/
