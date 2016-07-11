/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *//*


package com.opentaxi.android;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.opentaxi.android.asynctask.ProcessMessageTask;
import com.stil.generated.mysql.tables.pojos.CloudMessages;
import com.stil.generated.mysql.tables.pojos.Messages;

*/
/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 *//*

public class GcmIntentService extends IntentService {
    */
/*public static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;*//*


    public GcmIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "GcmIntentService";

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (messageType != null && extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            */
/*
             * Filter messages based on message type. Since it is likely that GCM will be
             * extended in the future with new message types, just ignore any message types you're
             * not interested in, or that you don't recognize.
             *//*

            if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR)) {
                Log.e(TAG, "Send error: " + extras.toString());

            } else if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_DELETED)) {
                Log.e(TAG, "Deleted messages on server: " + extras.toString());
                // If it's a regular GCM message, do some work.

            } else if (messageType.equals(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE)) {
                Log.i(TAG, "Received message"); // + intent.getStringExtra("full_address"));

                // Waking up mobile if it is sleeping
                //WakeLocker.acquire(this);

                String cloudMsg = intent.getStringExtra(CloudMessages.class.getName());
                if (cloudMsg != null) {
                    Integer cloudMsgId = Integer.parseInt(cloudMsg);
                    if (cloudMsgId > 0) {
                        Log.i(TAG, "Message id:" + cloudMsgId);
                        //AppPreferences.getInstance().setLastCloudMessage(cloudMsgId);
                        AsyncTask<Context, Void, Messages> msgTask = new ProcessMessageTask(cloudMsgId);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                            msgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
                        else msgTask.execute(this);
                    }
                } else Log.e(TAG, "Received cloudMsg=null");

            } else {
                Log.e(TAG, "Unknown messageType:" + messageType);
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    */
/*private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DemoActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_stat_gcm)
        .setContentTitle("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }*//*

}
*/
