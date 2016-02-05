package com.opentaxi.android.gcm;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import com.opentaxi.android.asynctask.ProcessMessageTask;
import com.stil.generated.mysql.tables.pojos.CloudMessages;
import com.stil.generated.mysql.tables.pojos.Messages;

import java.io.Serializable;

/**
 * Created by stanimir on 12/31/15.
 */
public class GcmMessageHandler extends GcmListenerService {

    public GcmMessageHandler() {
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        if (data == null || data.isEmpty()) {
            return;
        }

        Log.d("GcmMessageHandler", "From: " + from);
        Log.d("GcmMessageHandler", "Message: " + data.getString("message"));

        Integer cloudMsgId = data.getInt(CloudMessages.class.getName());
        if (cloudMsgId > 0) {
            Log.i("GcmMessageHandler", "Message id:" + cloudMsgId);
            //AppPreferences.getInstance().setLastCloudMessage(cloudMsgId);
            AsyncTask<Context, Void, Serializable> msgTask = new ProcessMessageTask(cloudMsgId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                msgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
            else msgTask.execute(this);
        }

        /*Notification notification = null;
        if (Constants.NOTIFICATION_TYPE_NEW_EVENT.equals(
                data.getString(Constants.NOTIFICATION_TYPE_KEY))) {
            notification = createNewEventNotification(data);
        }
        // TODO(suhas): Handle other notifications.

        if (notification != null) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            int notificationId = 1;
            notificationManager.notify(notificationId, notification);
        }*/
    }

    /*private Notification createNewEventNotification(Bundle data) {
        long eventId = 0;
        String eventName = "Invitation: " + data.getString(Constants.NOTIFICATION_EVENT_NAME_KEY);
        String eventNotes = data.getString(Constants.NOTIFICATION_EVENT_DETAILS_KEY);
        long eventTimeMillis = 0;
        try {
            eventId = Long.parseLong(data.getString(Constants.EVENT_ID_KEY));
            eventTimeMillis = Long.parseLong(data.getString(Constants.NOTIFICATION_EVENT_TIME_KEY));
        } catch (NumberFormatException ex) {
            // Failed to parse.
            // Log
        }

        Intent eventDetailsIntent = new Intent(this, EventDetailsActivity.class);
        eventDetailsIntent.putExtra(Constants.EVENT_ID_KEY, String.valueOf(eventId));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(EventDetailsActivity.class);
        stackBuilder.addNextIntent(eventDetailsIntent);
        PendingIntent eventDetailsPendingIntent = stackBuilder.getPendingIntent(
                0, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent rsvpYesIntent = createPendingIntent(1, eventId, "YES");
        PendingIntent rsvpNoIntent = createPendingIntent(2, eventId, "NO");
        PendingIntent rsvpMaybeIntent = createPendingIntent(3, eventId, "MAYBE");

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle(eventName)
                .setContentIntent(eventDetailsPendingIntent)
                .addAction(0, "Yes", rsvpYesIntent)
                .addAction(0, "No", rsvpNoIntent)
                .addAction(0, "Maybe", rsvpMaybeIntent);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        style.setBuilder(notificationBuilder);
        style.setBigContentTitle(eventName);
        if (eventTimeMillis > 0) {
            String eventTimeLine = DateTimeUtils.getDisplayDateTime(this, eventTimeMillis);
            style.addLine(eventTimeLine);
            notificationBuilder.setContentText(eventTimeLine);
        }
        if (!Strings.isNullOrEmpty(eventNotes)) {
            style.addLine(eventNotes);
        }
        notificationBuilder.setStyle(style);
        return notificationBuilder.build();
    }

    private PendingIntent createPendingIntent(int requestCode, long eventId, String rsvpResponse) {
        Intent rsvpBroadcast = new Intent();
        rsvpBroadcast.setAction(Constants.RSVP_FROM_NOTIFICATION_BROADCAST);
        rsvpBroadcast.putExtra(Constants.RSVP_RESPONSE_FROM_NOTIFICATION, rsvpResponse);
        rsvpBroadcast.putExtra(Constants.EVENT_ID_FOR_RSVP, eventId);
        return PendingIntent.getBroadcast(this, requestCode, rsvpBroadcast,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }*/
}
