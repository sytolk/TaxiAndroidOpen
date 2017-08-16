package com.opentaxi.android;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.ErrorDialogFragment;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.*;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.opentaxi.android.asynctask.LogoutTask;
import com.opentaxi.android.fragments.*;
import com.opentaxi.android.listeners.StatusDragListener;
import com.opentaxi.android.service.CoordinatesService;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.android.utils.ViewTools;
import com.opentaxi.models.CoordinatesLight;
import com.opentaxi.models.MapRequest;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Cars;
import com.taxibulgaria.enums.SecurityLevel;
import com.taxibulgaria.rest.models.NewCRequestDetails;
import de.greenrobot.event.EventBus;
import it.sephiroth.android.library.tooltip.Tooltip;
import net.i2p.android.ext.floatingactionbutton.FloatingActionButton;
import org.acra.ACRA;
import org.androidannotations.annotations.*;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

@RuntimePermissions
@EActivity(R.layout.main)
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnCommandListener {//, ActivityCompat.OnRequestPermissionsResultCallback  {

    private static final int REQUEST_USER_PASS_CODE = 10;
    public static final int HELP = 11;
    public static final int SERVER_CHANGE = 12;

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mTitle;

    @ViewById(R.id.action_bar)
    Toolbar toolbar;

    @ViewById(R.id.drawer_layout)
    DrawerLayout drawer;

    @ViewById(R.id.nav_view)
    NavigationView navigationView;

    @ViewById(R.id.fab)
    FloatingActionButton fab;

    @ViewById(R.id.status_drop)
    FrameLayout status_drop;

    private static final String TAG = "MainActivity";

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private ReactiveLocationProvider locationProvider;

    private Observable<Location> lastKnownLocationObservable;
    private Observable<Location> locationUpdatesObservable;
    private Subscription lastKnownLocationSubscription;
    private Subscription updatableLocationSubscription;
    private static final int REQUEST_CHECK_SETTINGS = 0;

    private float SUFFICIENT_ACCURACY = 300; //meters
    private long UPDATE_LOCATION_INTERVAL = 5000; //millis
    private long FASTEST_LOCATION_INTERVAL = 10000; //millis


    //private boolean havePlayService = true;
    //private final byte PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));

        super.onCreate(savedInstanceState);

        // Tell the activity we have menu items to contribute to the toolbar
        //setHasOptionsMenu(true);

        /*if (AndroidSupportUtil.runtimePermissionRequiredForAccessFineLocation(getApplicationContext())) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else startLocationProvider();*/
        MainActivityPermissionsDispatcher.startLocationProviderWithCheck(this);
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        //Log.i(TAG, "onRequestPermissionsResult requestCode:" + requestCode);

        if (PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION == requestCode) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                startLocationProvider();
                Log.i(TAG, "PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: PERMISSION_GRANTED");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    void startLocationProvider() {
        try {
            if (playServicesConnected()) {

                locationProvider = new ReactiveLocationProvider(getApplicationContext());

                lastKnownLocationObservable = locationProvider.getLastKnownLocation();

                final LocationRequest locationRequest = LocationRequest.create() //standard GMS LocationRequest
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(UPDATE_LOCATION_INTERVAL)
                        .setFastestInterval(FASTEST_LOCATION_INTERVAL);

                locationUpdatesObservable = locationProvider.checkLocationSettings(
                        new LocationSettingsRequest.Builder()
                                .addLocationRequest(locationRequest)
                                //.setAlwaysShow(true)
                                .build()
                )
                        .doOnNext(new Action1<LocationSettingsResult>() {
                            @Override
                            public void call(LocationSettingsResult locationSettingsResult) {
                                LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
                                if (locationSettingsStates != null && (!locationSettingsStates.isGpsPresent() || !locationSettingsStates.isGpsUsable())) {

                                    Status status = locationSettingsResult.getStatus();
                                    if (status.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                                        try {
                                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                        } catch (IntentSender.SendIntentException th) {
                                            Log.e(TAG, "Error opening settings activity.", th);
                                        }
                                    }
                                }
                            }
                        })
                        .flatMap(new Func1<LocationSettingsResult, Observable<Location>>() {
                            @Override
                            public Observable<Location> call(LocationSettingsResult locationSettingsResult) {
                                return locationProvider.getUpdatedLocation(locationRequest);
                            }
                        });
            }
        } catch (IllegalStateException e) {
            Log.e("stilActivity", "IllegalStateException", e);
        }
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_FINE_LOCATION)
    @OnNeverAskAgain(Manifest.permission.ACCESS_FINE_LOCATION)
    void showDeniedFor() {
        Snackbar.make(fab,"no permission to location",Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        } else {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            if (fm.getBackStackEntryCount() == 1) {
                //fm.popBackStackImmediate();
                supportFinishAfterTransition();
                return;
            }
        }

        super.onBackPressed();
    }

    android.support.v4.app.Fragment redirectFragment = null;

    @Override
    protected void onNewIntent(Intent newIntent) {
        Bundle extras = newIntent.getExtras();
        if (extras != null) {
            if (extras.containsKey("mapRequest")) {
                MapRequest mapRequest = extras.getParcelable("mapRequest");
                if (mapRequest != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("mapRequest", mapRequest);
                    redirectFragment = NewRequestFragment_.builder().build();
                    redirectFragment.setArguments(bundle);
                }
            } else if (extras.containsKey("startRequestDetails")) {
                NewCRequestDetails requestDetails = (NewCRequestDetails) extras.getSerializable("startRequestDetails");
                if (requestDetails != null) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("newCRequest", requestDetails);
                    redirectFragment = RequestDetailsFragment_.builder().build();
                    redirectFragment.setArguments(bundle);
                }
            }
            /*else if (extras.containsKey("startNewRequest") && extras.getBoolean("startNewRequest")) {
                redirectFragment = NewRequestFragment_.builder().build();
            }*/
        }
        super.onNewIntent(newIntent);
    }

    /**
     * Called when the activity is first created.
     */
    @AfterViews
    void afterMain() {

        setSupportActionBar(toolbar);

        View titleView = ViewTools.findActionBarTitle(toolbar); //getWindow().getDecorView());
        if (titleView != null) {
            titleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    drawer.openDrawer(Gravity.LEFT);
                }
            });
        } //else Log.w("MainActivity", "ActionBar's not found.");

        mDrawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                closeKeyboard();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }
        };
        drawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        navigationView.setItemIconTintList(null);

        Menu navMenu = navigationView.getMenu();
        if (navMenu != null) {
            MenuItem navHome = navMenu.findItem(R.id.nav_home);
            if (navHome != null)
                navHome.setIcon(new IconDrawable(this, MaterialIcons.md_home).colorRes(R.color.material_deep_orange_700).sizeDp(30));
            //navHome.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_home).actionBar().colorRes(R.color.material_deep_orange_700));

            MenuItem navMap = navMenu.findItem(R.id.nav_map);
            if (navMap != null)
                navMap.setIcon(new IconDrawable(this, MaterialIcons.md_map).colorRes(R.color.label_color).sizeDp(30));
            //navMap.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_map).actionBar().colorRes(R.color.app_primary_dark));

            MenuItem navRequest = navMenu.findItem(R.id.nav_request);
            if (navRequest != null)
                navRequest.setIcon(new IconDrawable(this, MaterialIcons.md_local_taxi).colorRes(R.color.timebase_color).sizeDp(30));
            //navRequest.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_local_taxi).actionBar().colorRes(R.color.timebase_color));

            MenuItem navHistory = navMenu.findItem(R.id.nav_history);
            if (navHistory != null)
                navHistory.setIcon(new IconDrawable(this, MaterialIcons.md_history).colorRes(R.color.app_primary).sizeDp(30));
            //navHistory.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_history).actionBar().colorRes(R.color.app_primary));

            MenuItem navServers = navMenu.findItem(R.id.nav_servers);
            if (navServers != null)
                navServers.setIcon(new IconDrawable(this, MaterialIcons.md_cloud).colorRes(R.color.transparent_blue).sizeDp(30));
            //navServers.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_cloud).actionBar().colorRes(R.color.transparent_blue));

            MenuItem navHelp = navMenu.findItem(R.id.options_help);
            if (navHelp != null)
                navHelp.setIcon(new IconDrawable(this, MaterialIcons.md_help).colorRes(R.color.app_primary_dark).sizeDp(30));
            //navHelp.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_help).actionBar().colorRes(R.color.app_primary_dark));

            MenuItem navFeedBack = navMenu.findItem(R.id.options_feedback);
            if (navFeedBack != null)
                navFeedBack.setIcon(new IconDrawable(this, MaterialIcons.md_contacts).colorRes(R.color.blue_color).sizeDp(30));

            MenuItem navLog = navMenu.findItem(R.id.nav_send_log);
            if (navLog != null)
                navLog.setIcon(new IconDrawable(this, MaterialIcons.md_bug_report).colorRes(R.color.red_color).sizeDp(30));
            // navLog.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_bug_report).actionBar().colorRes(R.color.red_color));

            MenuItem navExit = navMenu.findItem(R.id.nav_exit);
            if (navExit != null)
                navExit.setIcon(new IconDrawable(this, MaterialIcons.md_exit_to_app).colorRes(R.color.black_color).sizeDp(30));
            //navExit.setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_exit_to_app).actionBar().colorRes(R.color.black_color));

            reloadMenu();
        }

        fab.setIconDrawable(new IconDrawable(this, MaterialIcons.md_local_taxi).colorRes(R.color.label_color).sizeDp(45));
        //fab.setIconDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_local_taxi).sizeDp(35).colorRes(R.color.label_color));
        status_drop.setOnDragListener(new StatusDragListener());

        AppPreferences appPreferences = AppPreferences.getInstance(this);
        RestClient.getInstance().setSocketsType(appPreferences.getSocketType());
        //checkUser();
        //playServicesConnected();

        //if (!checkUserLogin()) checkFbLogin();
    }

    @LongClick(R.id.fab)
    void statusLongClick(View clickedView) {
        //Log.i(TAG,"LONG click");
        clickedView.startDrag(null, new View.DragShadowBuilder(clickedView), fab, 0);
        fab.setVisibility(View.INVISIBLE);
    }


    /*private void checkUser() {
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
    }*/

    @Override
    public void onStart() {
        super.onStart();
        //EventBus.getDefault().register(this);

        if (lastKnownLocationObservable != null) {
            lastKnownLocationSubscription = lastKnownLocationObservable
                    .subscribe(new Action1<Location>() {
                        @Override
                        public void call(Location location) {
                            doObtainedLocation(location);
                        }
                    }, new ErrorHandler());
        }
        if (locationUpdatesObservable != null) {
            updatableLocationSubscription = locationUpdatesObservable
                    .doOnError(new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            String message = "Error on location update: " + throwable.getMessage();
                            Log.e("updateLocation", message, throwable);
                            //Crashlytics.logException(throwable);
                        }
                    })
                    .onErrorReturn(new Func1<Throwable, Location>() {
                        @Override
                        public Location call(Throwable throwable) {
                            //locationUnSubscribe();
                            return null;
                        }
                    }).filter(new Func1<Location, Boolean>() {
                        @Override
                        public Boolean call(Location location) {
                            return location != null && location.getAccuracy() < SUFFICIENT_ACCURACY;
                        }
                    })
                    .subscribe(new Action1<Location>() {
                        @Override
                        public void call(Location location) {
                            doObtainedLocation(location);
                        }
                    }, new ErrorHandler());
        }
    }

    @Override
    public void onStop() {
        //EventBus.getDefault().unregister(this);

        if (updatableLocationSubscription != null) updatableLocationSubscription.unsubscribe();
        if (lastKnownLocationSubscription != null) lastKnownLocationSubscription.unsubscribe();

        super.onStop();
    }

    /**
     * greenEvent after user login
     */
    /*public void onEvent(Users users) {
        //setServers();
    }*/

    /*@Background
    void beforeStartUserPass() {
        //String token = AppPreferences.getInstance().getAccessToken();
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            //if (token != null && !token.equals("")) {
            //Log.i(TAG, "already authorized fb token=" + accessToken.getToken());
            if (!RestClient.getInstance().haveAuthorization()) {
                //Log.i(TAG, "AppPreferences.getInstance().getAccessToken=" + token);
                Users user = RestClient.getInstance().FacebookLogin(accessToken.getToken());
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
    }*/

    /*@Background
    void gcmRegister() {
        if (playServicesConnected()) {

            if (TaxiApplication.getGCMRegistrationId() == null) {
                GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                try {
                    String regId = gcm.register(RestClient.getInstance().getGCMsenderIds());
                    if (regId != null && regId.length() > 0) {
                        //String oldRegid = RestClient.getInstance().getGCMRegistrationId();
                        //if (oldRegid == null || !oldRegid.equals(regId)) {
                        Boolean isRegister = RestClient.getInstance().gcmRegister(regId);
                        if (isRegister){
                            Log.i("gcmRegister", "gcmRegister successful");
                            TaxiApplication.setGCMRegistrationId(regId);
                        }
                        else {
                            Log.e("gcmRegister", "gcmRegister not registered");
                        }
                        // }
                    }
                } catch (IOException e) {
                    Log.e("gcmRegister", "gcmRegister IOException", e);
                }
            }
        } else {
            Log.i("playServicesConnected", "No valid Google Play Services APK found.");
        }
    }*/
    public boolean playServicesConnected() {
        try {
            // Check that Google Play services is available
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            int result = googleAPI.isGooglePlayServicesAvailable(this);
            // If Google Play services is available
            if (ConnectionResult.SUCCESS == result) {
                // Continue
                return true;

            } else if (googleAPI.isUserResolvableError(result)) {
                setPlayServicesResolutionRequest(result);
            } else Log.e("playServicesConnected", "result:" + result);
        } catch (IllegalStateException e) {
            Log.e("playServicesConnected", "IllegalStateException", e);
        } catch (Exception e) {
            Log.e("playServicesConnected", "Exception", e);
        }

        return false;
    }

    @UiThread
    void setPlayServicesResolutionRequest(int result) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        Dialog errorDialog = googleAPI.getErrorDialog(this, result, PLAY_SERVICES_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            try {
                ErrorDialogFragment.newInstance(errorDialog).show(getFragmentManager(), "Play Service");
            } catch (Exception e) {
                Log.e("playServicesConnected", "Exception", e);
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startHome();
        } else if (id == R.id.nav_map) {
            BubbleOverlay_.intent(this).start();
        } else if (id == R.id.nav_request) {
            startRequests(false);
            //RequestsActivity_.intent(this).startForResult(REQUEST_INFO);
        } else if (id == R.id.nav_history) {
            startRequests(true);
            //TaxiApplication.requestsHistory(true);
            //RequestsActivity_.intent(this).startForResult(REQUEST_INFO);
        } else if (id == R.id.nav_servers) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ServersFragment_.builder().build())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
            //ServersActivity_.intent(this).startForResult(SERVER_CHANGE);
        } //else if (id == R.id.nav_book_taxi) NewRequestActivity_.intent(this).start();
        else if (id == R.id.nav_send_log) {
            buildAlertDebug();
        } else if (id == R.id.options_help) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, HelpFragment_.builder().build())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } else if (id == R.id.options_feedback) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ContactsFragment_.builder().build())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        } else if (id == R.id.nav_exit) {
            exitButton();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void buildAlertDebug() {
        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setMessage(R.string.send_log_title)
                .setCancelable(true)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        RestClient.getInstance().clearCache();
                        ACRA.getErrorReporter().handleSilentException(new Exception("Client Report"));
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.dismiss();
                    }
                });
        final android.support.v7.app.AlertDialog alert = builder.create();
        alert.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPositive = alert.getButton(Dialog.BUTTON_POSITIVE);
                btnPositive.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);

                Button btnNegative = alert.getButton(Dialog.BUTTON_NEGATIVE);
                btnNegative.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
            }
        });
        alert.show();
        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
    }

    // Define a DialogFragment that displays the error dialog
    /*public static class MainDialogFragment extends android.support.v4.app.DialogFragment {
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
    }*/

    /*@UiThread
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
        noti.defaults |= Notification.DEFAULT_SOUND;
        noti.defaults |= Notification.DEFAULT_VIBRATE;

        notificationManager.notify(0, noti);
    }*/

    private void doObtainedLocation(Location location) {
        try {
            EventBus.getDefault().postSticky(location);

            CoordinatesLight coordinates = new CoordinatesLight();
            coordinates.setN(location.getLatitude());
            coordinates.setE(location.getLongitude());
            coordinates.setT(location.getTime());
            Intent i = new Intent(this, CoordinatesService.class);
            i.putExtra("coordinates", coordinates);
            startService(i);

            //Log.i("doObtainedLocation", "onReceive:" + location);

            /*if (AppPreferences.getInstance() != null) {

                Date now = new Date();
                AppPreferences.getInstance().setNorth(location.getLatitude());
                AppPreferences.getInstance().setEast(location.getLongitude());
                AppPreferences.getInstance().setCurrentLocationTime(location.getTime());
                AppPreferences.getInstance().setGpsLastTime(now.getTime());
            }
            Log.i("doObtainedLocation", "onReceive: received location update:" + location.getLatitude() + ", " + location.getLongitude());*/
        } catch (Exception e) {
            Log.e("doObtainedLocation", "onReceive:" + e.getMessage());
        }
    }

    private class ErrorHandler implements Action1<Throwable> {
        @Override
        public void call(Throwable throwable) {
            Log.d("MainActivity", "Error occurred", throwable);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        //menu.findItem(R.id.options_server).setIcon(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_3d_rotation).actionBar().color(Color.BLACK));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            /*case R.id.options_server:
                ServersActivity_.intent(this).startForResult(SERVER_CHANGE);
                return true;*/
           /* case R.id.options_help:
                HelpActivity_.intent(this).startForResult(HELP);
                return true;*/
            case R.id.options_exit:
                //RestClient.getInstance().clearCache();
                finish();
                return true;
            /*case R.id.options_send_log:

                RestClient.getInstance().clearCache();
                ACRA.getErrorReporter().handleSilentException(new Exception("Developer Report"));
                return true;*/
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        // Check device for Play Services APK.
        //servicesConnected();
        startHome();
    }

    /**
     * @return true if User is login false is not
     */
    private boolean checkUserLogin() {

        String user = AppPreferences.getInstance(getApplicationContext()).getUsers().getUsername();
        String pass = AppPreferences.getInstance(getApplicationContext()).getUsers().getPassword();

        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            return false;
        } else {
            /*Log.i(TAG, "user:" + user);
            Log.i(TAG, "pass:" + pass);
            Log.i(TAG, "getCookieexpire:" + AppPreferences.getInstance().getUsers().getCookieexpire());*/
            if (!RestClient.getInstance().haveAuthorization()) { //autologin
                try {
                    /*if (SecurityLevel.HIGH.getCode().equals(AppPreferences.getInstance().getUsers().getCookieexpire()))
                        RestClient.getInstance().setAuthHeadersEncoded(user, pass);
                    else */
                    if (SecurityLevel.LOW.getCode().equals(AppPreferences.getInstance().getUsers().getCookieexpire()))
                        RestClient.getInstance().setAuthHeaders(user, pass);
                    else return false;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return true;
        }
    }

    /*@Background
    void checkFbLogin() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            //if (token != null && !token.equals("")) {
            //Log.i(TAG, "already authorized fb token=" + accessToken.getToken());
            //if (!RestClient.getInstance().haveAuthorization()) {
            //Log.i(TAG, "AppPreferences.getInstance().getAccessToken=" + token);
            Users user = RestClient.getInstance().FacebookLogin(accessToken.getToken());
            if (user != null) { //user already exist
                if (user.getId() != null && user.getId() > 0) {
                    RestClient.getInstance().setAuthHeadersEncoded(user.getUsername(), user.getPassword());
                    startHomeUI();
                }
            }
        }
    }

    @UiThread
    void startHomeUI() {
        startHome();
        reloadMenu();
    }*/

    @Override
    public void startHome() {
        Log.i(TAG, "startHome");
        android.support.v4.app.Fragment fragment = null;

        if (checkUserLogin()) {
            //showNewRequest(true);

            if (redirectFragment != null) {
                fragment = redirectFragment;
                redirectFragment = null;
            } else {
                //getSupportFragmentManager().popBackStack(null, android.support.v4.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
                fragment = HomeFragment_.builder().build();
            }
        } else {
            //checkFbLogin();
            // Log.i(TAG, "startHome no user");
            reloadMenu();
            fragment = UserPassFragment_.builder().build();
        }

        if (fragment != null && !isFinishing()) {
            //Log.i("startHome", fragment.toString());
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    //.disallowAddToBackStack()
                    .commitAllowingStateLoss();
        } /*else if (redirectFragment != null && !isFinishing()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, redirectFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
            redirectFragment = null;*/ else Log.i("startHome", "no fragment isFinishing=" + isFinishing());
    }

    @UiThread
    void showNewRequest(boolean show) {
        Log.i(TAG, "showNewRequest");
        if (fab != null) {
            if (show) {
                if (fab.getVisibility() == View.INVISIBLE) {
                    fab.setVisibility(View.VISIBLE);
                    Tooltip.make(this,
                            new Tooltip.Builder(101)
                                    .anchor(fab, Tooltip.Gravity.LEFT)
                                    .closePolicy(new Tooltip.ClosePolicy()
                                            .insidePolicy(true, false)
                                            .outsidePolicy(true, false), 60000)
                                    //.activateDelay(1800)
                                    .showDelay(2000)
                                    .text(getResources(), R.string.taxi_tooltip) //"Поръчай такси с едно кликване"
                                    //.maxWidth(500)
                                    .withArrow(true)
                                    .withOverlay(true)
                                    //.floatingAnimation(Tooltip.AnimationBuilder.SLOW)
                                    .withStyleId(R.style.ToolTipLayoutCustomStyle)
                                    .build()
                    ).show();
                }
            } else {
                fab.setVisibility(View.INVISIBLE);
                Tooltip.remove(this, 101);
            }
        } else Log.e(TAG, "fab=null");
    }

    @Override
    public void fabVisible(boolean isVisible) {
        showNewRequest(isVisible);
    }

    @Override
    public void closeKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void startNewRequest(Cars cars) {
        if (!isFinishing()) {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            NewRequestFragment fragment = NewRequestFragment_.builder().build();
            if (cars != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("cars", cars);
                fragment.setArguments(bundle);
            }
// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);

// Commit the transaction
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void startCarDetails(Integer requestId) {
        if (!isFinishing() && requestId != null) {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            CarDetailsFragment fragment = CarDetailsFragment_.builder().build();
            Bundle bundle = new Bundle();
            bundle.putInt("requestId", requestId);
            fragment.setArguments(bundle);
// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);

// Commit the transaction
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void startRequestDetails(NewCRequestDetails newRequest) {
        if (!isFinishing()) {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            RequestDetailsFragment fragment = RequestDetailsFragment_.builder().build();
            Bundle bundle = new Bundle();
            bundle.putSerializable("newCRequest", newRequest);
            fragment.setArguments(bundle);
// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);

// Commit the transaction
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void startEditRequest(NewCRequestDetails newCRequest) {
        if (!isFinishing()) {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            NewRequestFragment fragment = NewRequestFragment_.builder().build();
            Bundle bundle = new Bundle();
            bundle.putSerializable("newCRequest", newCRequest);
            fragment.setArguments(bundle);
// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);

// Commit the transaction
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void startRequests(boolean history) {
        if (!isFinishing()) {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            RequestsHistoryFragment fragment = RequestsHistoryFragment_.builder().build();
            Bundle bundle = new Bundle();
            bundle.putBoolean("history", history);
            fragment.setArguments(bundle);
// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack so the user can navigate back
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);

// Commit the transaction
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void setBarTitle(String title) {
        setTitle(title);
    }

    /**
     * call it from UI
     */
    @Override
    public void reloadMenu() {

        if (navigationView != null) {
            Menu navMenu = navigationView.getMenu();
            if (navMenu != null) {
                MenuItem navRequest = navMenu.findItem(R.id.nav_request);
                MenuItem navHistory = navMenu.findItem(R.id.nav_history);
                MenuItem navFeedBack = navMenu.findItem(R.id.options_feedback);
                if (checkUserLogin()) {
                    if (navRequest != null) navRequest.setVisible(true);
                    if (navHistory != null) navHistory.setVisible(true);
                    if (navFeedBack != null) navFeedBack.setVisible(true);
                } else {
                    if (navRequest != null) navRequest.setVisible(false);
                    if (navHistory != null) navHistory.setVisible(false);
                    if (navFeedBack != null) navFeedBack.setVisible(false);
                }
            }
        }
    }

    @Override
    public void startLostPassword() {
        if (!isFinishing()) {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            LostPasswordFragment fragment = LostPasswordFragment_.builder().build();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    public void startNewClient() {
        if (!isFinishing()) {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            NewClientFragment fragment = NewClientFragment_.builder().build();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.addToBackStack(null);
            transaction.commitAllowingStateLoss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        //SimpleFacebook.getInstance(this).onActivityResult(this, requestCode, resultCode, data);

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
                        //TaxiApplication.setHavePlayService(false);
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
                    //setVersion();
                } else if (resultCode == RESULT_CANCELED) {
                    finish();
                    break;
                }
                //checkUser();
                break;
            default:
                Log.e(TAG, "onActivityResult requestCode:" + requestCode + " resultCode:" + resultCode);
                break;
        }
    }

    //@Click
    private void exitButton() {
        //showDialog(DIALOG_EXIT);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.exit);
        alertDialogBuilder.setMessage(getString(R.string.exit_confirm));
        //null should be your on click listener
        alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                new LogoutTask().execute();
                //AppPreferences.getInstance().setAccessToken("");
                AppPreferences.getInstance().setLastCloudMessage(null);
                AppPreferences.getInstance().setUsers(null);
                RestClient.getInstance().removeAuthorization();

                LoginManager.getInstance().logOut();
                //facebookLogout();
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

   /* @Background
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
    }*/

    /*@Click
    void requestButton() {
        //Intent msgIntent = new Intent(getBaseContext(), NewRequestActivity_.class);
        //startActivityForResult(msgIntent, REQUEST_INFO);
        RequestsActivity_.intent(this).startForResult(REQUEST_INFO);
    }*/

    /*@Click
    void mapButton() {
        BubbleOverlay_.intent(this).start();
        //LocationLibrary.forceLocationUpdate(getBaseContext());
    }*/

    /*@Click
    void newRequestButton() {
        //LocationLibrary.forceLocationUpdate(getBaseContext());
        NewRequestActivity_.intent(this).start();
    }*/

    @Click
    void fab() {
        startNewRequest(null);
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
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            return wm.getConnectionInfo().getLinkSpeed();
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getNetworkType();
        }
        return 0;
    }


    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mTitle);
            //actionBar.setIcon(R.drawable.icon);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mDrawerToggle != null)
            mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        if (mDrawerToggle != null)
            mDrawerToggle.onConfigurationChanged(newConfig);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        return super.onPrepareOptionsMenu(menu);
    }
}
