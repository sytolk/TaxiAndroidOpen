package com.opentaxi.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import com.opentaxi.models.NewRequest;
import com.opentaxi.rest.RestClient;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 3/21/13
 * Time: 3:22 PM
 * developer STANIMIR MARINOV
 */
public class RequestInfoTask extends AsyncTask<Context, Void, NewRequest> {

    private static final String TAG = "RequestInfoTask";
    private Context context;
    private OnTaskCompleted listener;

    public RequestInfoTask(OnTaskCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(NewRequest newRequest) {
        //might want to change "executed" for the returned string passed into onPostExecute() but that is upto you
        listener.onTaskCompleted(newRequest);
    }

    @Override
    protected NewRequest doInBackground(Context... params) {
        if (params.length == 1) context = params[0];

        return RestClient.getInstance().RequestInfo();
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {

    }

    public interface OnTaskCompleted {
        void onTaskCompleted(NewRequest newRequest);
    }
}