package com.opentaxi.android.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.TextView;
import com.opentaxi.android.R;
import com.opentaxi.android.TaxiApplication;
import com.opentaxi.android.gcm.GCMRegisterService;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.android.utils.MessageEvent;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Servers;
import com.taxibulgaria.enums.Applications;
import com.taxibulgaria.rest.models.Users;
import de.greenrobot.event.EventBus;
import org.androidannotations.annotations.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 4/18/13
 * Time: 2:58 PM
 * developer STANIMIR MARINOV
 */
@EFragment(R.layout.content_main)
public class HomeFragment extends BaseFragment {

    //private static final String TAG = "HomeFragment";

    @ViewById
    TextView user;

    @ViewById(R.id.txt_version)
    TextView version;

    @ViewById(R.id.bandwidth)
    TextView bandwidth;

    /**
     * http://stackoverflow.com/questions/14177781/java-lang-illegalstateexception-can-not-perform-this-action-after-onsaveinstanc
     *
     * @param outState outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @AfterViews
    void afterViews() {
        PackageInfo packageInfo = getPackageInfo();
        if (packageInfo != null) version.setText(packageInfo.versionName);

        showUser(AppPreferences.getInstance(mActivity).getUsers());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mListener != null) mListener.setBarTitle(mActivity.getString(R.string.app_name));
        if (!TaxiApplication.isServersUpdated()) {
            updateServers();
        }

        if (!TaxiApplication.isVersionSend()) {
            TaxiApplication.setVersionSend(true);
            PackageInfo packageInfo = getPackageInfo();
            if (packageInfo != null) {
                //showVersion(packageInfo.versionName);
                sendVersion(packageInfo);
            }
        }
        if (mListener != null) mListener.fabVisible(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * greenEvent after user login
     *
     * @param users
     */
    public void onEventMainThread(Users users) {
        //Log.i("onEventMainThread", "users:" + users.getUsername());

        if (TaxiApplication.getGCMRegistrationId() == null) {
            mActivity.startService(new Intent(mActivity, GCMRegisterService.class));
            //Log.i("onEventMainThread", "startService:GCMRegisterService");
        }// else Log.i("onEventMainThread", "getGCMRegistrationId:" + TaxiApplication.getGCMRegistrationId());
        showUser(users);
        updateServers();
        if (mListener != null) mListener.reloadMenu();
        EventBus.getDefault().removeStickyEvent(users);
    }

    private void showUser(Users users) {
        if (user != null && users != null) {
            StringBuilder userDetails = new StringBuilder();
            if (users.getContact() != null) {
                if (users.getContact().getFirstname() != null)
                    userDetails.append(users.getContact().getFirstname());
                if (users.getContact().getLastname() != null)
                    userDetails.append(" ").append(users.getContact().getLastname());
            }
            if (userDetails.length() > 0)
                user.setText(userDetails.toString());
            else
                user.setText(users.getUsername());
        }
    }

    public void onEventMainThread(MessageEvent network) {
        if (bandwidth != null && network != null) bandwidth.setText(network.message);
    }

    private PackageInfo getPackageInfo() {
        if (isAdded()) {
            try {
                return mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("showVersion", "NameNotFoundException", e);
            } catch (Exception e) {
                Log.e("showVersion", "Exception", e);
            }
        }
        return null;
    }

    @Background
    void sendVersion(PackageInfo pInfo) {
        try {
            //if (pInfo != null) {
            Integer version = RestClient.getInstance().sendVersion(Applications.ANDROID_CLIENT.getCode(), pInfo.versionName, pInfo.versionCode);
            if (version != null && version > pInfo.versionCode) {
                updateVersionDialog(true);
            }
        } catch (Exception e) {
            Log.e("sendVersion", "Exception", e);
        }
    }

    @Background
    void updateServers() {
        Servers[] serversList = RestClient.getInstance().getServers();
        if (serversList != null) {
            RestClient.getInstance().setServerSockets(serversList);
            TaxiApplication.setServersUpdated(true);
        }
    }

    @UiThread
    void updateVersionDialog(final boolean isRejected) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("Нова версия!");
        int positiveBtn = R.string.yes;
        if (isRejected) {
            builder.setMessage("Налична е нова версия. Желаете ли да актуализирате ?");
        } else {
            builder.setMessage("Налична е нова версия. Версията с която работите е спряна от подръжка и трябва да инсталирате новата!");
            positiveBtn = R.string.okbutton;
        }
        //null should be your on click listener
        builder.setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadUpdate();
                dialog.dismiss();
            }
        });
        if (isRejected) {
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //rejectUpdate(); todo implement it to know rejections
                    dialog.dismiss();
                }
            });
        }

        final AlertDialog alert = builder.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE);
                btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);

                Button btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE);
                btnNegative.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
            }
        });
        alert.show();
        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
    }

    @Background
    void downloadUpdate() {
        try {
            URL url = new URL("http://taxi-bulgaria.com:9191/TaxiAndroidOpen.apk"); //Unacs
            //URL url = new URL("http://taxi-burgas.com/TaxiAndroidOpen.apk"); //MaxTelekom
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();

            //String PATH = "/mnt/sdcard/Download/";
            File file = new File(Environment.getExternalStorageDirectory(), "Download/");
            file.mkdirs();
            File outputFile = new File(file, "update.apk");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(outputFile);

            InputStream is = c.getInputStream();

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();

            // Check if app was updated; if so, it must clear the GCM registration ID
            // since the existing regID is not guaranteed to work with the new app version.
            // RestClient.getInstance().setGCMRegistrationId("");

        } catch (Exception e) {
            Log.e("UpdateAPP", "Update error! " + e.getMessage());
        }

        startUpdate();
    }

    @UiThread
    void startUpdate() {
        File file = new File(Environment.getExternalStorageDirectory(), "Download/update.apk");
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
            startActivity(intent);
        } else Log.e("updateVersionDialog", "file not exist:" + file.getAbsolutePath());
    }
}