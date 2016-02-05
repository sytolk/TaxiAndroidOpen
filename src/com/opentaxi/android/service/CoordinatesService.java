package com.opentaxi.android.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import com.opentaxi.android.asynctask.ProcessMessageTask;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.models.CoordinatesLight;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Messages;

import java.io.Serializable;


public class CoordinatesService extends IntentService {

    public CoordinatesService() {
        super("CoordinatesService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        CoordinatesLight coordinates = intent.getParcelableExtra("coordinates");
        Integer cloudMessageId = RestClient.getInstance().sendCoordinates(coordinates);

        if (AppPreferences.getInstance() != null && cloudMessageId != null) {
            AsyncTask<Context, Void, Serializable> msgTask = new ProcessMessageTask(cloudMessageId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                msgTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.getApplicationContext());
            else msgTask.execute(this.getApplicationContext());
        }
    }
}
