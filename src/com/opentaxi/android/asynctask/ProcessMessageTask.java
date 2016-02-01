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
import com.opentaxi.android.MessageActivity;
import com.opentaxi.android.R;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.models.NewRequestDetails;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Advertisement;
import com.stil.generated.mysql.tables.pojos.CloudMessages;
import com.stil.generated.mysql.tables.pojos.Messages;
import com.taxibulgaria.enums.MessagePriority;
import com.taxibulgaria.enums.RequestAcceptStatus;
import com.taxibulgaria.enums.RequestAction;
import com.taxibulgaria.enums.RequestStatus;

import java.io.IOException;
import java.net.URLDecoder;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 3/21/13
 * Time: 3:22 PM
 * developer STANIMIR MARINOV
 */
public class ProcessMessageTask extends AsyncTask<Context, Void, Messages> {

    private static final String TAG = "ProcessMessageTask";
    private int cloudMsgId;
    private Context context;

    public ProcessMessageTask(int cloudMsgId) {
        this.cloudMsgId = cloudMsgId;
    }

    @Override
    protected void onPostExecute(Messages messages) {
        //might want to change "executed" for the returned string passed into onPostExecute() but that is upto you
        if (messages != null) {
            if (messages.getPriority() != null && messages.getPriority().equals(MessagePriority.IMPORTANT.getCode())) {
                msg(messages);
            } else generateNotification(messages);
        } else Log.e(TAG, "ProcessMessage onPostExecute messages=null");
    }

    @Override
    protected Messages doInBackground(Context... params) {
        if (params.length == 1) context = params[0];
        //Log.i(TAG, "ProcessMessage:" + cloudMsgId);
        if (AppPreferences.getInstance() != null) {
            Integer lastCloudMessage = AppPreferences.getInstance().getLastCloudMessage();
            if (lastCloudMessage == null || lastCloudMessage < cloudMsgId) {
                CloudMessages cloudMessages = RestClient.getInstance().getCloudMessage(cloudMsgId);
                if (cloudMessages != null) {
                    Log.i(TAG, "ProcessMessage:" + cloudMessages.getClassName() + " msg:" + cloudMessages.getMsg());
                    if (cloudMessages.getClassName().equals(NewRequestDetails.class.getName()) || cloudMessages.getClassName().equals(NewRequestDetails.class.getSimpleName())) {

                        NewRequestDetails request = null;
                        try {
                            request = AppPreferences.getInstance().getMapper().readValue(cloudMessages.getMsg(), NewRequestDetails.class);
                        } catch (IOException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }

                        if (request != null) {
                            if (request.getStatus().equals(RequestStatus.NEW_REQUEST_DELETE.getCode())) { //DELETE REQUEST
                                // generateNotification("Taxi", "поръчка " + request.getRequestsId(), "Съжаляваме но поръчка " + request.getRequestsId() + " (" + request.getFullAddress() + ") е отказана!");
                                Messages messages = new Messages();
                                messages.setMsg(context.getString(R.string.request_rejected, request.getRequestsId(), request.getFullAddress()));
                                //messages.setPriority(MessagePriority.IMPORTANT.getCode());
                                return messages;
                            } else if (request.getStatus().equals(RequestStatus.NEW_REQUEST_EDIT.getCode())) { //EDIT REQUEST
                                //msg("Поръчка " + request.getRequestsId() + " (" + request.getFullAddress() + ") е променена!");
                                Messages messages = new Messages();
                                messages.setMsg(context.getString(R.string.request_edited, request.getRequestsId(), request.getFullAddress()));
                                //messages.setPriority(MessagePriority.IMPORTANT.getCode());
                                return messages;
                            } else if (request.getAcceptType().equals(RequestAcceptStatus.RI_TRANSFER_SUCCESS.getCode())) { //TRANSFER SUCCESSFUL
                                //deleteRequest(request.getRequestsId(), context.getString(R.string.transfer_success, request.getFullAddress()));
                            } else if (request.getAcceptType().equals(RequestAcceptStatus.RI_TRANSFER.getCode())) { //TRANSFER FAILED
                                //messages.setMsg("Трансфера е неуспешен! Моля изпълнете заявката (" + AppPreferences.getInstance().getRequestText(request) + ")");
                            } else { //NEW REQUEST
                                Log.i(TAG, "New REQUEST msg");
                                    /*AppPreferences.getInstance().setCarState(CarState.STATE_PROPOSAL.getCode());
                                    Intent proposalIntent = new Intent(context, ProposalBoxActivity.class);
                                    proposalIntent.putExtra(NewRequest.class.getName(), request);
                                    proposalIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    proposalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(proposalIntent);*/
                            }
                        }
                    } else if (cloudMessages.getClassName().equals(Messages.class.getName()) || cloudMessages.getClassName().equals(Messages.class.getSimpleName())) {
                        try {
                            Messages messages = AppPreferences.getInstance().getMapper().readValue(cloudMessages.getMsg(), Messages.class);
                            messages.setMsg(URLDecoder.decode(messages.getMsg(), "UTF-8"));
                            return messages;
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (cloudMessages.getClassName().equals(Advertisement.class.getName())) {
                        Log.i(TAG, "New advertisement available");
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
        Intent msgIntent = new Intent(context, MessageActivity.class);
        msgIntent.putExtra(Messages.class.getName(), messages);
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