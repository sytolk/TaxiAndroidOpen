/*
package com.opentaxi.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.rest.RestClient;

*/
/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 3/21/13
 * Time: 3:22 PM
 * developer STANIMIR MARINOV
 *//*

public class SendCoordinatesTask extends AsyncTask<Context, Void, Integer> {

    private static final String TAG = "SendCoordinatesTask";
    private float north;
    private float east;
    private long time;
    private Context context;

    public SendCoordinatesTask(float north, float east, long time) {
        this.north = north;
        this.east = east;
        this.time = time;
    }

    @Override
    protected void onPostExecute(Integer cloudMessageId) {
        //might want to change "executed" for the returned string passed into onPostExecute() but that is upto you
        processMessage(cloudMessageId);
    }

    @Override
    protected Integer doInBackground(Context... params) {
        if (params.length == 1) context = params[0];

        return RestClient.getInstance().sendCoordinates(north, east, time);
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

    private synchronized void processMessage(Integer cloudMessageId) {
        if (AppPreferences.getInstance() != null && cloudMessageId != null) {
        */
/*    if (AppPreferences.getInstance().isEnableProcessMsg()) {*//*

            //   Integer lastCloudMessage = AppPreferences.getInstance().getLastCloudMessage();
            //if (cloudMessageId != null && AppPreferences.getInstance() != null && lastCloudMessage != null && !lastCloudMessage.equals(cloudMessageId) && lastCloudMessage + 5 > cloudMessageId */
/*if its have more that 10 msg ignore it*//*
) {
            // if (lastCloudMessage == null || lastCloudMessage < cloudMessageId) {
            // for (int i = lastCloudMessage; i <= cloudMessageId; i++) {
            AsyncTask<Context, Void, Boolean> msgTask = new ProcessMessageTask(cloudMessageId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                msgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context);
            else msgTask.execute(context);
            //     AppPreferences.getInstance().setLastCloudMessage(cloudMessageId);
            //  }
        }
    }
}*/
