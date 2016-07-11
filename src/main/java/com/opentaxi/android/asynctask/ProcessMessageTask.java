package com.opentaxi.android.asynctask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import com.opentaxi.android.MessageActivity_;
import com.opentaxi.android.R;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.CloudMessages;
import com.stil.generated.mysql.tables.pojos.Messages;
import com.taxibulgaria.enums.MessagePriority;
import com.taxibulgaria.enums.RequestAction;
import com.taxibulgaria.rest.models.NewRequestDetails;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 3/21/13
 * Time: 3:22 PM
 * developer STANIMIR MARINOV
 */
public class ProcessMessageTask extends AsyncTask<Context, Void, Serializable> {

    private static final String TAG = "ProcessMessageTask";
    private int cloudMsgId;
    private Context context;

    public ProcessMessageTask(int cloudMsgId) {
        this.cloudMsgId = cloudMsgId;
    }

    @Override
    protected void onPostExecute(Serializable obj) {
        //might want to change "executed" for the returned string passed into onPostExecute() but that is upto you
        if (obj != null) {
            if (obj instanceof Messages) {
                Messages messages = (Messages) obj;
                if (MessagePriority.IMPORTANT.getCode().equals(messages.getPriority())) {
                    msg(messages);
                } else {
                    generateNotification(messages);
                    //Log.e(TAG, "ProcessMessage unknown MessagePriority=" + messages.getPriority());
                }
            } else if (obj instanceof NewRequestDetails) {
                NewRequestDetails requestDetails = (NewRequestDetails) obj;
                request(requestDetails);
            }
        } else Log.e(TAG, "ProcessMessage onPostExecute messages=null");
    }

    @Override
    protected Serializable doInBackground(Context... params) {
        if (params.length == 1) context = params[0];
        //Log.i(TAG, "ProcessMessage:" + cloudMsgId);
        if (AppPreferences.getInstance() != null) {
            Integer lastCloudMessage = AppPreferences.getInstance().getLastCloudMessage();
            if (lastCloudMessage == null || lastCloudMessage < cloudMsgId) {
                CloudMessages cloudMessages = RestClient.getInstance().getCloudMessage(cloudMsgId);
                if (cloudMessages != null) {
                    //Log.i(TAG, "ProcessMessage:" + cloudMessages.getClassName() + " msg:" + cloudMessages.getMsg());
                    if (NewRequestDetails.class.getName().equals(cloudMessages.getClassName()) || NewRequestDetails.class.getSimpleName().equals(cloudMessages.getClassName())) {

                        NewRequestDetails request = null;

                        try {
                            request = RestClient.getInstance().getObjectMapper().readValue(cloudMessages.getMsg(), NewRequestDetails.class); //fromJson
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (request != null) return request;
                    } else if (Messages.class.getName().equals(cloudMessages.getClassName()) || Messages.class.getSimpleName().equals(cloudMessages.getClassName())) {
                        try {
                            Messages messages = RestClient.getInstance().getObjectMapper().readValue(cloudMessages.getMsg(), Messages.class); //fromJson
                            messages.setMsg(URLDecoder.decode(messages.getMsg(), "UTF-8"));
                            return messages;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    /*} else if (Advertisement.class.getName().equals(cloudMessages.getClassName())) {
                        Log.i(TAG, "New advertisement available");*/
                    } else Log.e(TAG, "Unknown class:" + cloudMessages.getClassName());

                    AppPreferences.getInstance().setLastCloudMessage(cloudMsgId);

                } else Log.e(TAG, "No message exist on server:" + cloudMsgId);
            } else Log.e(TAG, "Message is already processed:" + cloudMsgId);
        } else Log.e(TAG, "AppPreferences.getInstance()=null" + cloudMsgId);
        return null;
    }

    /*private void msg(String msg) {
        Messages messages = new Messages();
        messages.setMsg(msg);
        msg(messages);
    }*/

    private void msg(Messages messages) {
        //Log.i(TAG, "msg:" + messages.getMsg());
        Intent msgIntent = new Intent(context, MessageActivity_.class);
        msgIntent.putExtra("messages", messages);
        msgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        msgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(msgIntent);
        playSound();
    }


    private void request(NewRequestDetails requestDetails) {
        Intent msgIntent = new Intent(context, MessageActivity_.class);
        msgIntent.putExtra("requestDetails", requestDetails);
        msgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        msgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(msgIntent);
        playSound();
    }

    private static final int NOTIF_ID = 11;

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private void generateNotification(Messages message) {
        //Log.i(TAG, "generateNotification:" + message.getMsg());
        generateNotification("Taxi", context.getString(R.string.app_name), message.getMsg());
    }

    private void generateNotification(String ticker, String title, String message) {
        try {
            Intent notificationIntent = new Intent(context, RequestAction.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            //Resources res = context.getResources();
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.icon)
                    //.setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.some_big_img))
                    .setTicker(ticker)
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setContentText(message);
            Notification n = builder.build();

            //n.flags |= Notification.FLAG_AUTO_CANCEL;

            // Play default notification sound
            n.defaults |= Notification.DEFAULT_SOUND;

            // Vibrate if vibrate is enabled
            n.defaults |= Notification.DEFAULT_VIBRATE;

            nm.notify(NOTIF_ID, n);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void playSound() {
        /*Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(context, defaultRingtoneUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            mediaPlayer.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);  //SMS
            if (alert == null) {
                // alert is null, using backup
                alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                if (alert == null) {  // I can't see this ever being null (as always have a default notification) but just incase
                    // alert backup is null, using 2nd backup
                    alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                }
            }

            if (alert != null) {
                Ringtone r = RingtoneManager.getRingtone(context, alert);
                r.play();
            }

        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage());
        }
    }
}