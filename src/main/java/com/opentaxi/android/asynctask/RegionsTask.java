/*
package com.opentaxi.android.asynctask;

import android.content.Context;
import android.os.AsyncTask;
import com.stil.generated.mysql.tables.pojos.Regions;
import com.opentaxi.rest.RestClient;

import java.util.LinkedHashMap;
import java.util.Map;

*/
/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 3/21/13
 * Time: 3:22 PM
 * developer STANIMIR MARINOV
 *//*

public class RegionsTask extends AsyncTask<Context, Void, Regions[]> {

    private static final String TAG = "RegionsTask";
    //private Context context;
    private OnTaskCompleted listener;

    public RegionsTask(OnTaskCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(Regions[] regList) {
        //might want to change "executed" for the returned string passed into onPostExecute() but that is upto you
        Map<Integer,String> regionsMap = new LinkedHashMap<Integer, String>();
        if (regList != null) {
            for (Regions region : regList) {
                regionsMap.put(region.getId(), region.getDescription());
            }
        }
        listener.onTaskCompleted(regionsMap);
    }

    @Override
    protected Regions[] doInBackground(Context... params) {
        //if (params.length == 1) context = params[0];

        return RestClient.getInstance().getRegions();
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {

    }

    public interface OnTaskCompleted {
        void onTaskCompleted(Map<Integer,String> regionsMap);
    }
}*/
