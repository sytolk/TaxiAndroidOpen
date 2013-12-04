package com.opentaxi.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import com.opentaxi.rest.RestClient;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 3/21/13
 * Time: 3:22 PM
 * developer STANIMIR MARINOV
 */
public class LogoutTask extends AsyncTask<Context, Void, Boolean> {

    private static final String TAG = "LogoutTask";

    @Override
    protected void onPostExecute(Boolean cloudMessageId) {
        //might want to change "executed" for the returned string passed into onPostExecute() but that is upto you
    }

    @Override
    protected Boolean doInBackground(Context... params) {
        return RestClient.getInstance().Logout();
    }
}