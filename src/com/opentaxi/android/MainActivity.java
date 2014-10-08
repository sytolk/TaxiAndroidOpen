package com.opentaxi.android;

import android.app.*;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.opentaxi.android.asynctask.LogoutTask;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.android.utils.Network;
import com.stil.generated.mysql.tables.pojos.Servers;
import com.opentaxi.models.Users;
import com.opentaxi.rest.RestClient;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.listeners.OnLogoutListener;
import com.taxibulgaria.enums.Applications;
import org.androidannotations.annotations.*;

import java.io.IOException;

@EActivity(R.layout.main)
public class MainActivity extends FragmentActivity {

    private static final int REQUEST_USER_PASS_CODE = 10;
    public static final int HELP = 11;
    public static final int SERVER_CHANGE = 12;
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
    //private boolean havePlayService = true;

    public MainActivity() {

    }

    /**
     * Called when the activity is first created.
     */
    @AfterViews
    void afterMain() {
        /*PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo("com.opentaxi.android", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }*/

        AppPreferences appPreferences = AppPreferences.getInstance(this);
        RestClient.getInstance().setSocketsType(appPreferences.getSocketType());
        checkUser();

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertMessageNoGps();
        }
    }

    void alertMessageNoGps() {
        //showDialog(DIALOG_EXIT);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.no_gps_title);
        alertDialogBuilder.setMessage(R.string.no_gps_msg);
        //null should be your on click listener
        alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog exitDialog = alertDialogBuilder.create();
        exitDialog.show();
    }

    private void checkUser() {
        if (AppPreferences.getInstance() != null) {
            String user = RestClient.getInstance().getUsername();
            String pass = RestClient.getInstance().getPassword();

            if (user == null || user.equals("") || pass == null || pass.equals("")) {
                com.stil.generated.mysql.tables.pojos.Users users = AppPreferences.getInstance().getUsers();
                if (users != null) {
                    String username = users.getUsername();
                    String password = users.getPassword();

                    if (username == null || password == null) {
                        beforeStartUserPass();
                    } else {
                        RestClient.getInstance().setAuthHeadersEncoded(username, password);
                        afterLogin(username);
                    }
                } else beforeStartUserPass();
            } else {
                String username = AppPreferences.getInstance().decrypt(user, "user_salt");
                String password = AppPreferences.getInstance().decrypt(pass, username);

                Log.i(TAG, "checkUser username:" + username + " password:" + password);

                if (username == null || password == null) {
                    beforeStartUserPass();
                } else {
                    RestClient.getInstance().setAuthHeaders(username, password);

                    afterLogin(username);
                }
            }
        }
    }

    @UiThread
    void afterLogin(String username) {
        this.user.setText(username);
        AppPreferences.getInstance().setRegions();

        //appPreferences.registerGCM(getBaseContext());

        if (!oneTime) {
            setVersion();
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

    @Background
    void beforeStartUserPass() {
        String token = AppPreferences.getInstance().getAccessToken();
        if (token != null && !token.equals("")) {
            Log.i(TAG, "already authorized token=" + token);
            if (!RestClient.getInstance().haveAuthorization()) {
                //Log.i(TAG, "AppPreferences.getInstance().getAccessToken=" + token);
                Users user = RestClient.getInstance().FacebookLogin(token);
                if (user != null) { //user already exist
                    if (user.getId() != null && user.getId() > 0) {
                        RestClient.getInstance().setAuthHeadersEncoded(user.getUsername(), user.getPassword());
                        afterLogin(user.getUsername());
                    } else startUserPass();
                } else startUserPass();
            } else {
                Users user = AppPreferences.getInstance().getUsers();
                if (user != null) afterLogin(user.getUsername());
                else Log.e(TAG, "facebook no user");
            }
        } else startUserPass();
    }

    @UiThread
    void startUserPass() {
        UserPassActivity_.intent(this).startForResult(REQUEST_USER_PASS_CODE);
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
    public static class MainDialogFragment extends android.support.v4.app.DialogFragment {
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
            TaxiApplication.setHavePlayService(true);
            // Continue
            return true;
            // Google Play services was not available for some reason
        } else if (TaxiApplication.isHavePlayService()) {
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
            //if (AppPreferences.getInstance() != null && !AppPreferences.getInstance().getAppVersion().equals(pInfo.versionName)) {
            //    AppPreferences.getInstance().setAppVersion(pInfo.versionName);
            sendVersion(pInfo.versionName, pInfo.versionCode);
            //}
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "setVersion:" + e.getMessage());
        }
    }

    @Background
    void sendVersion(String version, Integer code) {
        Boolean isActual = RestClient.getInstance().sendVersion(Applications.ANDROID_CLIENT.getCode(), version, code);
        if (isActual != null && !isActual) {
            //updateVersionDialog(); todo
            createNotification();
        } else Log.i(TAG, "You have last version");
    }

    @UiThread
    public void createNotification() {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.opentaxi.android"));
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // Build notification
        Notification noti = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.new_version))
                        //.setContentText("Version")
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pIntent)
                .addAction(R.drawable.icon, "Update", pIntent).build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
        noti.flags |= Notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(0, noti);
    }

    /*@UiThread
    void updateVersionDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Нова версия на Такси България!");
        alertDialogBuilder.setMessage("Налична е нова версия. Желаете ли да актуализирате ?");
        //null should be your on click listener
        alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadUpdate();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog updateDialog = alertDialogBuilder.create();
        updateDialog.show();
    }

    @Background
    void downloadUpdate() {
        try {
            URL url = new URL("http://taxi-bulgaria.com:8888/TaxiAndroidOpen.apk");
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
            RestClient.getInstance().setGCMRegistrationId("");

            startUpdate();
        } catch (Exception e) {
            Log.e("UpdateAPP", "Update error! " + e.getMessage());
        }
    }

    @UiThread
    void startUpdate() {
        File newVersion = new File(Environment.getExternalStorageDirectory(), "Download/update.apk");
        if (newVersion.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(newVersion), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
            startActivity(intent);
        }
    }
*/
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
                ServersActivity_.intent(this).startForResult(SERVER_CHANGE);
                return true;
            case R.id.options_help:
                HelpActivity_.intent(this).startForResult(HELP);
                return true;
            case R.id.options_exit:
                //RestClient.getInstance().clearCache();
                finish();
                return true;
            case R.id.options_send_log:

                RestClient.getInstance().clearCache();
                /*String javaTmpDir = System.getProperty("java.io.tmpdir");
                File cacheDir = new File(javaTmpDir, "DiskLruCacheDir");
                if (cacheDir.exists()) {
                    File[] files = cacheDir.listFiles();
                    if (files != null)
                        for (File file : files) {
                            file.delete();
                        }
                }*/
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
        //servicesConnected();

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
        if (bandwidth != null) {
            if (RestClient.getInstance().isHaveConnection()) {
                bandwidth.setText(RestClient.getInstance().getConnectionTypeName()); //+ " strength:" + RestClient.getInstance().getBandwidth());
            } else bandwidth.setText("no connection");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        SimpleFacebook.getInstance(this).onActivityResult(this, requestCode, resultCode, data);

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
                        TaxiApplication.setHavePlayService(false);
                        break;
                }
                break;
            case SERVER_CHANGE:
                if (resultCode == RESULT_OK) {

                }
                break;
            case REQUEST_USER_PASS_CODE:
                //Log.e(TAG, "REQUEST_USER_PASS_CODE onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
                if (resultCode == RESULT_OK) {
                    //userLogin(data);
                    setVersion();
                } else if (resultCode == RESULT_CANCELED) {
                    finish();
                    break;
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
        alertDialogBuilder.setTitle(R.string.exit);
        alertDialogBuilder.setMessage(getString(R.string.exit_confirm));
        //null should be your on click listener
        alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                new LogoutTask().execute();
                AppPreferences.getInstance().setAccessToken("");
                AppPreferences.getInstance().setLastCloudMessage(null);
                AppPreferences.getInstance().setUsers(null);
                RestClient.getInstance().removeAuthorization();
                facebookLogout();
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog exitDialog = alertDialogBuilder.create();
        exitDialog.show();
    }

    @Background
    void facebookLogout() {
        final OnLogoutListener onLogoutListener = new OnLogoutListener() {

            @Override
            public void onFail(String reason) {
                Log.w(TAG, "Failed to logout");
            }

            @Override
            public void onException(Throwable throwable) {
                Log.e(TAG, "Bad thing happened", throwable);
            }

            @Override
            public void onThinking() {
            }

            @Override
            public void onLogout() {
            }

        };
        SimpleFacebook.getInstance(this).logout(onLogoutListener);
    }

    @Click
    void requestButton() {
        //Intent msgIntent = new Intent(getBaseContext(), NewRequestActivity_.class);
        //startActivityForResult(msgIntent, REQUEST_INFO);
        RequestsActivity_.intent(this).startForResult(REQUEST_INFO);
    }

    @Click
    void mapButton() {
        BubbleOverlay_.intent(this).start();
        //LocationLibrary.forceLocationUpdate(getBaseContext());
    }

    @Click
    void newRequestButton() {
        //LocationLibrary.forceLocationUpdate(getBaseContext());
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
    /*void showToastOnUiThread(final String text) {
        if (AndroidUtil.currentThreadIsUiThread()) {
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
    }*/
}
