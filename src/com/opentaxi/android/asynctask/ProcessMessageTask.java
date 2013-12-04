package com.opentaxi.android.asynctask;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import com.opentaxi.android.MessageActivity;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.enums.RequestAcceptStatus;
import com.opentaxi.enums.RequestStatus;
import com.opentaxi.generated.mysql.tables.pojos.Advertisement;
import com.opentaxi.generated.mysql.tables.pojos.CloudMessages;
import com.opentaxi.generated.mysql.tables.pojos.Messages;
import com.opentaxi.models.NewRequest;
import com.opentaxi.rest.RestClient;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 3/21/13
 * Time: 3:22 PM
 * developer STANIMIR MARINOV
 */
public class ProcessMessageTask extends AsyncTask<Context, Void, Boolean> {

    private static final String TAG = "ProcessMessageTask";
    private int cloudMsgId;
    private Context context;

    public ProcessMessageTask(int cloudMsgId) {
        this.cloudMsgId = cloudMsgId;
    }

    @Override
    protected void onPostExecute(Boolean cloudMessageId) {
        //might want to change "executed" for the returned string passed into onPostExecute() but that is upto you
        if (cloudMessageId != null && cloudMessageId) {
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

                //playSound();
                //generateNotification(context, "Заявка");

            } catch (Exception e) {
                if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    protected Boolean doInBackground(Context... params) {
        if (params.length == 1) context = params[0];

        if (AppPreferences.getInstance() != null) {
            Integer lastCloudMessage = AppPreferences.getInstance().getLastCloudMessage();
            if (lastCloudMessage == null || lastCloudMessage < cloudMsgId) {
                CloudMessages cloudMessages = RestClient.getInstance().getCloudMessage(cloudMsgId);
                if (cloudMessages != null) {
                    if (AppPreferences.getInstance() != null) {
                        if (cloudMessages.getClassName().equals(NewRequest.class.getName())) {

                            NewRequest request = null;
                            try {
                                request = AppPreferences.getInstance().getMapper().readValue(cloudMessages.getMsg(), NewRequest.class);
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                            if (request != null) {
                                if (request.getStatus().equals(RequestStatus.NEW_REQUEST_DELETE.getCode())) { //DELETE REQUEST
                                    //deleteRequest(request.getRequestsId(), "Съжаляваме но заявката (" + AppPreferences.getInstance().getRequestText(request) + ") е отказана!");
                                } else if (request.getStatus().equals(RequestStatus.NEW_REQUEST_EDIT.getCode())) { //EDIT REQUEST
                                    Messages messages = new Messages();

                                    //messages.setMsg("Заявката е променена: " + AppPreferences.getInstance().getRequestText(request)); // region + " " + request.getFullAddress());

                                    Intent msgIntent = new Intent(context, MessageActivity.class);
                                    msgIntent.putExtra(Messages.class.getName(), messages);
                                    msgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    msgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(msgIntent);
                                } else if (request.getAcceptType().equals(RequestAcceptStatus.RI_TRANSFER_SUCCESS.getCode())) { //TRANSFER SUCCESSFUL
                                    deleteRequest(request.getRequestsId(), "Трансфера на заявката (" + request.getFullAddress() + ") е успешен!");
                                } else if (request.getAcceptType().equals(RequestAcceptStatus.RI_TRANSFER.getCode())) { //TRANSFER FAILED

                                    Messages messages = new Messages();
                                    //messages.setMsg("Трансфера е неуспешен! Моля изпълнете заявката (" + AppPreferences.getInstance().getRequestText(request) + ")");

                                    Intent msgIntent = new Intent(context, MessageActivity.class);
                                    msgIntent.putExtra(Messages.class.getName(), messages);
                                    msgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    msgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(msgIntent);
                                } else { //NEW REQUEST
                                    /*AppPreferences.getInstance().setCarState(CarState.STATE_PROPOSAL.getCode());
                                    Intent proposalIntent = new Intent(context, ProposalBoxActivity.class);
                                    proposalIntent.putExtra(NewRequest.class.getName(), request);
                                    proposalIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    proposalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(proposalIntent);*/
                                }
                            }
                        } else if (cloudMessages.getClassName().equals(Messages.class.getName())) {
                            Messages messages = null;
                            try {
                                messages = AppPreferences.getInstance().getMapper().readValue(cloudMessages.getMsg(), Messages.class);
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                            if (messages != null) {
                                Intent msgIntent = new Intent(context, MessageActivity.class);
                                msgIntent.putExtra(Messages.class.getName(), messages);
                                msgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                msgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(msgIntent);
                            }
                        } else if (cloudMessages.getClassName().equals(Advertisement.class.getName())) {

                            Log.i(TAG, "New advertisement available");

                        }

                        AppPreferences.getInstance().setLastCloudMessage(cloudMsgId);

                        return true;
                    }
                } else Log.e(TAG, "No message exist on server:" + cloudMsgId);
            } else Log.e(TAG, "Message is already processed:" + cloudMsgId);
        } else Log.e(TAG, "AppPreferences.getInstance()=null" + cloudMsgId);
        return false;
    }

    private void deleteRequest(Integer requestId, String msg) {
        NewRequest currRequest = AppPreferences.getInstance().getCurrentRequest();
        if (currRequest != null) {

            if (currRequest.getRequestsId().equals(requestId)) {
                AppPreferences.getInstance().setCurrentRequest(null);
            } else {
                Log.e(TAG, "currRequest:" + currRequest.getRequestsId() + " request:" + requestId);
                NewRequest nextRequest = AppPreferences.getInstance().getNextRequest();
                if (nextRequest != null && nextRequest.getRequestsId().equals(requestId)) {
                    Log.e(TAG, "nextRequest:" + currRequest.getRequestsId() + " request:" + requestId);
                    AppPreferences.getInstance().setNextRequest(null);
                }
            }

            Messages messages = new Messages();
            messages.setMsg(msg);

            Intent msgIntent = new Intent(context, MessageActivity.class);
            msgIntent.putExtra(Messages.class.getName(), messages);
            msgIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            msgIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(msgIntent);
        }
    }


    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    /*private static void generateNotification(Context context, String message) {
        try {
            int icon = R.drawable.ic_launcher;
            long when = System.currentTimeMillis();
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = new Notification(icon, message, when);

            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            // Play default notification sound
            notification.defaults |= Notification.DEFAULT_SOUND;

            // Vibrate if vibrate is enabled
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notificationManager.notify(0, notification);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }*/

    private void playSound() {
        Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

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
        }
    }
}