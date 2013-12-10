package com.opentaxi.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.opentaxi.android.asynctask.LogoutTask;
import com.opentaxi.android.map.AdvancedMapViewer;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.android.utils.Network;
import com.opentaxi.generated.mysql.tables.pojos.Servers;
import com.opentaxi.rest.RestClient;
import org.androidannotations.annotations.*;
import org.mapsforge.android.AndroidUtils;

import java.io.File;
import java.io.IOException;

@EActivity(R.layout.main)
public class MainActivity extends FragmentActivity {

    private static final int REQUEST_USER_PASS_CODE = 10;
    private static final int SERVER_CHANGE = 12;
    private static final int MAP_VIEW = 13;
    private static final int REQUEST_INFO = 14;
    private static final int MESSAGE = 40;

    @ViewById
    TextView user;

    @ViewById(R.id.txt_version)
    TextView version;

    @ViewById(R.id.bandwidth)
    TextView bandwidth;

    private static final String TAG = "MainActivity";

    private boolean oneTime = false;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    GoogleCloudMessaging gcm;
    private boolean havePlayService = true;

    /**
     * Called when the activity is first created.
     */
    @AfterViews
    void afterMain() {

        AppPreferences appPreferences = AppPreferences.getInstance(this);
        RestClient.getInstance().setSocketsType(appPreferences.getSocketType());
        checkUser();
    }

    private void checkUser() {
        if (AppPreferences.getInstance() != null) {
            String user = RestClient.getInstance().getUsername();
            String pass = RestClient.getInstance().getPassword();

            if (user == null || user.equals("") || pass == null || pass.equals("")) {
                UserPassActivity_.intent(this).startForResult(REQUEST_USER_PASS_CODE);
            } else {
                String username = AppPreferences.getInstance().decrypt(user, "user_salt");
                String password = AppPreferences.getInstance().decrypt(pass, username);

                Log.i(TAG, "checkUser username:" + username + " password:" + password);

                if (username == null || password == null) {
                    UserPassActivity_.intent(this).startForResult(REQUEST_USER_PASS_CODE);
                } else {
                    RestClient.getInstance().setAuthHeaders(username, password);

                    this.user.setText(username);
                    AppPreferences.getInstance().setRegions();

                    setVersion();
                    //appPreferences.registerGCM(getBaseContext());

                    if (!oneTime) {
                        Log.i(TAG, "Updating servers");
                        oneTime = true;
                        setServers();
                    }

                    if (servicesConnected()) {
                        gcm = GoogleCloudMessaging.getInstance(this);
                        String regid = RestClient.getInstance().getGCMRegistrationId(); //AppPreferences.getInstance().getGCMRegId();

                        if (regid == null || regid.equals("")) {
                            gcmRegister();
                        } //else sendRegistration(regid);
                    } else {
                        Log.i(TAG, "No valid Google Play Services APK found.");
                    }
                }
            }
        }
    }

    @Background
    void gcmRegister() {
        Log.i(TAG, "gcmRegister");
        if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
        }
        try {
            String regId = gcm.register(RestClient.getInstance().getGCMsenderIds());
            if (regId != null && regId.length() > 0) {
                //String oldRegid = RestClient.getInstance().getGCMRegistrationId();
                //if (oldRegid == null || !oldRegid.equals(regId)) {
                Boolean isRegister = RestClient.getInstance().gcmRegister(regId);
                if (isRegister != null && isRegister) Log.i(TAG, "gcmRegister successful");
                else {
                    Log.e(TAG, "gcmRegister not registered");
                }
                // }
            }
        } catch (IOException e) {
            if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            else Log.e(TAG, "gcmRegister IOException");
        }
    }

    // Define a DialogFragment that displays the error dialog
    public static class MainDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public MainDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (mDialog == null) super.setShowsDialog(false);
            return mDialog;
        }
    }


    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Activity Recognition",
                    "Google Play services is available.");
            havePlayService = true;
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else if (havePlayService) {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                try {
                    // Create a new DialogFragment for the error dialog
                    MainDialogFragment errorFragment = new MainDialogFragment();
                    // Set the dialog in the DialogFragment
                    errorFragment.setDialog(errorDialog);
                    // Show the error dialog in the DialogFragment
                    errorFragment.show(getSupportFragmentManager(), "Activity Recognition");
                } catch (Exception e) {
                    if (e.getMessage() != null) Log.e(TAG, e.getMessage());
                }
            }
            return false;
        }

        return false;
    }


    @Background
    void setServers() {
        Servers[] serverList = RestClient.getInstance().getServers();
        if (serverList != null) {
            RestClient.getInstance().setServerSockets(serverList);
            RestClient.getInstance().cleanSocketCheck();
        }
    }


    private void setVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version.setText(pInfo.versionName);
            if (AppPreferences.getInstance() != null && !AppPreferences.getInstance().getAppVersion().equals(pInfo.versionName)) {
                AppPreferences.getInstance().setAppVersion(pInfo.versionName);
                sendVersion(pInfo.versionName);
                // Check if app was updated; if so, it must clear the GCM registration ID
                // since the existing regID is not guaranteed to work with the new app version.
                RestClient.getInstance().setGCMRegistrationId("");//AppPreferences.getInstance().setGCMRegId("");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "setVersion:" + e.getMessage());
        }
    }

    @Background
    void sendVersion(String version) {
        RestClient.getInstance().sendVersion(version);
    }

    @Override
    protected void onStop() {
        Log.i(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_server:
                Intent intent = new Intent(this, ServersActivity_.class);
                startActivityForResult(intent, SERVER_CHANGE);
                return true;

            case R.id.options_exit:

                finish();
                return true;
            case R.id.options_send_log:

                String javaTmpDir = System.getProperty("java.io.tmpdir");
                File cacheDir = new File(javaTmpDir, "DiskLruCacheDir");
                if (cacheDir.exists()) {
                    File[] files = cacheDir.listFiles();
                    if (files != null)
                        for (File file : files) {
                            file.delete();
                        }
                }
                int i = 2 / 0;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        unregisterReceiver(networkState);
        super.onPause();
    }

    @Override
    protected void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkState, filter);
        super.onResume();
        // Check device for Play Services APK.
        servicesConnected();

        //Check Internet connection
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetInfo != null) {
            //Toast.makeText( context, "Active Network Type : " + activeNetInfo.getTypeName(), Toast.LENGTH_SHORT ).show();
            if (activeNetInfo.isConnected()) {
                StringBuilder network = new StringBuilder();
                if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                    RestClient.getInstance().setBandwidth(wm.getConnectionInfo().getLinkSpeed());
                    network.append(activeNetInfo.getTypeName()).append(" ").append(wm.getConnectionInfo().getLinkSpeed()).append("Mbps");
                } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    RestClient.getInstance().setBandwidth(tm.getNetworkType());
                    network.append(activeNetInfo.getTypeName()).append(" ").append(Network.getNetworkTypeName(tm.getNetworkType()));
                } else RestClient.getInstance().setBandwidth(0);

                onConnected(network.toString());

            } else onDisconnected();

        } else onDisconnected();

        changeNetworkState();
    }

    private void onConnected(String typeName) {
        RestClient.getInstance().setHaveConnection(true);
        RestClient.getInstance().setConnectionTypeName(typeName);
        /*String gcmRegId = RestClient.getInstance().getGCMRegistrationId();
        if (gcmRegId != null && gcmRegId.length() > 0) sendRegistration(gcmRegId);*/
    }

    private void onDisconnected() {
        RestClient.getInstance().setHaveConnection(false);
        RestClient.getInstance().setConnectionTypeName("");
    }

    private BroadcastReceiver networkState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            changeNetworkState();
        }
    };

    @UiThread(delay = 1000)
    void changeNetworkState() {
        if (RestClient.getInstance().isHaveConnection()) {
            bandwidth.setText(RestClient.getInstance().getConnectionTypeName()); //+ " strength:" + RestClient.getInstance().getBandwidth());
        } else bandwidth.setText("no connection");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case PLAY_SERVICES_RESOLUTION_REQUEST:

            /*
             * If the result code is Activity.RESULT_OK, try
             * to connect again
             */
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        break;
                    case Activity.RESULT_CANCELED:
                        havePlayService = false;
                        break;
                }
                break;
            case SERVER_CHANGE:
                if (resultCode == RESULT_OK) {

                }
                break;
            case REQUEST_USER_PASS_CODE:
                if (resultCode == RESULT_OK) {
                    //userLogin(data);
                    setVersion();
                }
                checkUser();
                break;
            default:
                Log.e(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
                break;
        }
    }

    @Click
    void exitButton() {
        //showDialog(DIALOG_EXIT);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Изход");
        alertDialogBuilder.setMessage("Наистина ли искате да излезете от системата ?");
        //null should be your on click listener
        alertDialogBuilder.setPositiveButton("ДА", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                new LogoutTask().execute();
                AppPreferences.getInstance().setLastCloudMessage(null);
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton("НЕ", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog exitDialog = alertDialogBuilder.create();

        // If Google Play services can provide an error dialog
        if (exitDialog != null) {
            try {
                // Create a new DialogFragment for the error dialog
                MainDialogFragment errorFragment = new MainDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(exitDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(), "ExitDialog");
            } catch (Exception e) {
                if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            }
        }
    }

    @Click
    void requestButton() {
        //Intent msgIntent = new Intent(getBaseContext(), NewRequestActivity_.class);
        //startActivityForResult(msgIntent, REQUEST_INFO);
        RequestsActivity_.intent(this).startForResult(REQUEST_INFO);
    }

    @Click
    void mapButton() {
        Intent intent = new Intent(MainActivity.this, AdvancedMapViewer.class);
        MainActivity.this.startActivityForResult(intent, MAP_VIEW);
    }

    @Click
    void newRequestButton() {
        NewRequestActivity_.intent(this).start();
    }

    private boolean isDataConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }

    private int isHighBandwidth() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            return wm.getConnectionInfo().getLinkSpeed();
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getNetworkType();
        }
        return 0;
    }

    /**
     * Uses the UI thread to display the given text message as toast notification.
     *
     * @param text the text message to display
     */
    void showToastOnUiThread(final String text) {
        if (AndroidUtils.currentThreadIsUiThread()) {
            Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
            toast.show();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG);
                    toast.show();
                }
            });
        }
    }
}
