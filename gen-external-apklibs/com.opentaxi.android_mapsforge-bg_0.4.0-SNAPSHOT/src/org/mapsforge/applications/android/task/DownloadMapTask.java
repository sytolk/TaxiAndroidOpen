package org.mapsforge.applications.android.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 3/21/13
 * Time: 3:22 PM
 * developer STANIMIR MARINOV
 */
public class DownloadMapTask extends AsyncTask<Context, Integer, File> {

    private static final String TAG = "DownloadUpdateTask";
    private OnTaskCompleted listener;
    private String mapFile = "bulgaria.map";

    public DownloadMapTask(OnTaskCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected void onPostExecute(File path) {
        listener.onTaskCompleted(path);
    }

    @Override
    protected File doInBackground(Context... params) {

        try {
            URL url = new URL("http://taxi-bulgaria.com:8888/" + mapFile);
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            // getting file length
            int lenghtOfFile = c.getContentLength();

            //String PATH = "/mnt/sdcard/Download/";
            File file = new File(Environment.getExternalStorageDirectory(), "Download/");
            file.mkdirs();
            File outputFile = new File(file, mapFile);
            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            long total = 0;
            while ((len1 = is.read(buffer)) != -1) {
                total += len1;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress((int) ((total * 100) / lenghtOfFile));

                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();

        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(TAG, "error! " + e.getMessage());
        }

        return new File(Environment.getExternalStorageDirectory(), "Download/" + mapFile);
    }

    /**
     * Updating progress bar
     */
    protected void onProgressUpdate(Integer... progress) {
        // setting progress percentage
        listener.progressUpdated(progress[0]);
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(File path);

        void progressUpdated(int progress);
    }
}