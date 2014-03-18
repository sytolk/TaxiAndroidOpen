package com.opentaxi.android;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.generated.mysql.tables.pojos.Servers;
import com.opentaxi.models.Users;
import com.opentaxi.rest.RestClient;
import org.androidannotations.annotations.*;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 4/17/13
 * Time: 10:18 AM
 * developer STANIMIR MARINOV
 */
@WindowFeature(Window.FEATURE_NO_TITLE)
@EActivity(R.layout.request_servers)
public class ServersActivity extends Activity {

    private static final String TAG = "ServersActivity";
    //private static final int REQUEST_CHOOSE_CAR_CODE = 11;

    private ScheduledExecutorService refreshScheduler;

    @ViewById(R.id.cancelButton)
    Button cancelButton;

    @ViewById(R.id.serversContent)
    LinearLayout serversContent;

    @AfterViews
    void afterServers() {
        RestClient.getInstance().cleanSocketCheck();
        showServers(true);
        refreshScheduler = Executors.newSingleThreadScheduledExecutor();
        refreshScheduler.scheduleWithFixedDelay(new Runnable() {

            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        showServers(false);
                    }
                });
            }
        }, 2, 2, TimeUnit.SECONDS);
    }

    @UiThread
    void showServers(boolean testing) {
        //Log.i(TAG, "showServers testing:" + testing);
        serversContent.removeAllViews();

        List<Servers> sockets = RestClient.getInstance().getSockets();
        String currSocket = RestClient.getInstance().getCurrentSocket();

        final RadioButton[] rb = new RadioButton[sockets.size()];
        RadioGroup rg = new RadioGroup(this); //create the RadioGroup
        rg.setOrientation(RadioGroup.VERTICAL);
        int i = 0;
        for (final Servers socket : sockets) {
            //Log.i(TAG, socket.getServerHost() + " " + socket.getDescription());

            rb[i] = new RadioButton(this);
            rg.addView(rb[i]); //the RadioButtons are added to the radioGroup instead of the layout

            //CheckBox ch = new CheckBox(this);
            StringBuilder title = new StringBuilder();
            title.append(socket.getDescription()).append(" "); //.append(socket.getServerHost());
            if (socket.getRecordstatus()) {
                title.append("UP");
                rb[i].setBackgroundColor(getResources().getColor(R.color.label_color));
            } else {
                title.append("DOWN");
                rb[i].setBackgroundColor(getResources().getColor(R.color.red_color));
            }
            rb[i].setText(title.toString());

            if (currSocket.equals(socket.getServerHost())) {
                rb[i].setChecked(true);
            }

            rb[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer oldType = AppPreferences.getInstance().getSocketType();
                    Log.i(TAG, "onClick:" + socket.getServerType() + " oldType:" + oldType);
                    AppPreferences.getInstance().setSocketType(socket.getServerType());
                    if (RestClient.getInstance().changeServerSockets(socket)) {
                        if (oldType == null || !oldType.equals(socket.getServerType())) login();
                        showServers(false);
                    } else Log.i(TAG, "ServerSockets is not changed");
                }
            });

            if (testing && socket.getServerType().equals(AppPreferences.getInstance().getSocketType()))
                testServer(socket.getServerHost());

            i++;
        }
        serversContent.addView(rg);
    }

    @Background
    void testServer(String socket) {
        Log.i(TAG, "testing:" + socket);
        RestClient.getInstance().testSocketConnection(socket);
    }
    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.request_servers);

        RadioButton productionButton = (RadioButton) findViewById(R.id.radioButtonProduction);
        RadioButton cloudButton = (RadioButton) findViewById(R.id.radioButtonCloud);
        RadioButton testButton = (RadioButton) findViewById(R.id.radioButtonTesting);

        String socket = AppPreferences.getInstance().getCurrentSocket();
        if (RestClient.getInstance().getProductionSockets().containsKey(socket)) {
            productionButton.setChecked(true);
        } else if (RestClient.getInstance().getCloudSockets().containsKey(socket)) {
            cloudButton.setChecked(true);
        } else if (RestClient.getInstance().getTestingSockets().containsKey(socket)) {
            testButton.setChecked(true);
        } else {
            Log.w(TAG, "Unknown BaseURL:" + socket + " Maybe you run the application for the first time");
            productionButton.setChecked(true);
        }
    }*/

    @Override
    public void onPause() {
        super.onPause();
        if (refreshScheduler != null) {
            refreshScheduler.shutdown();
            refreshScheduler = null;
        }
        finish();
    }

    @Override
    public void finish() {
        if (getParent() == null) {
            setResult(Activity.RESULT_OK);
        } else {
            getParent().setResult(Activity.RESULT_OK);
        }
        super.finish();
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CHOOSE_CAR_CODE) {
                if (data.hasExtra(Users.class.getName())) {
                    Users users = (Users) data.getSerializableExtra(Users.class.getName());
                    if (users != null) {
                        AppPreferences.getInstance().setUsers(users);
                        RestClient.getInstance().setAuthHeaders(users.getUsername(), users.getPassword());
                    }
                }
            }
        }
    }*/

   /* public void onClickProduction(View view) {
        RadioButton cloudButton = (RadioButton) findViewById(R.id.radioButtonCloud);
        cloudButton.setChecked(false);
        RadioButton testButton = (RadioButton) findViewById(R.id.radioButtonTesting);
        testButton.setChecked(false);
        if (RestClient.getInstance() != null) {
            RestClient.getInstance().setProductionSockets();
            AppPreferences.getInstance().setCurrentSocket(RestClient.PRODUCTION_URL1); //persist it
        }
        Login();
    }

    public void onClickCloud(View view) {
        RadioButton productionButton = (RadioButton) findViewById(R.id.radioButtonProduction);
        productionButton.setChecked(false);
        RadioButton testButton = (RadioButton) findViewById(R.id.radioButtonTesting);
        testButton.setChecked(false);
        if (RestClient.getInstance() != null) {
            RestClient.getInstance().setCloudSockets();
            AppPreferences.getInstance().setCurrentSocket(RestClient.CLOUD_URL1);
        }
        Login();
    }

    public void onClickTesting(View view) {
        RadioButton productionButton = (RadioButton) findViewById(R.id.radioButtonProduction);
        productionButton.setChecked(false);
        RadioButton cloudButton = (RadioButton) findViewById(R.id.radioButtonCloud);
        cloudButton.setChecked(false);
        if (RestClient.getInstance() != null) {
            RestClient.getInstance().setTestingSockets();
            AppPreferences.getInstance().setCurrentSocket(RestClient.TESTING_URL);
        }
        Login();
    }*/

    @Click
    void cancelButton() {
        finish();
    }

    @Click
    void refreshButton() {
        updateServers();
    }

    @Background
    void updateServers() {
        Servers[] servers = RestClient.getInstance().getServers();
        if (servers != null) {
            RestClient.getInstance().setServerSockets(servers);
            RestClient.getInstance().cleanSocketCheck();
            showServers(true);
        } else Log.e(TAG, "updateServers servers = null");
    }

    @Background
    void login() {
        Log.i(TAG, "login");
        Users user = RestClient.getInstance().Login();

        if (user != null) {
            if (user.getId() != null && user.getId() > 0) {
                //Toast.makeText(UserPassActivity.this, "Влязохте в системата успешно!", Toast.LENGTH_LONG).show();
                //AppPreferences.getInstance().setUsers(user);
                //finish();
            } else loginError("Грешно потребителско име или парола!");
            // Toast.makeText(UserPassActivity.this, "Грешно потребителско име или парола!", Toast.LENGTH_LONG).show();

        } else loginError("Грешка! Сигурни ли сте че имате връзка с интернет?");
        //Toast.makeText(UserPassActivity.this, "Грешка! Сигурни ли сте че имате връзка с интернет?", Toast.LENGTH_LONG).show();
    }

    @UiThread
    void loginError(String error) {
        Toast.makeText(ServersActivity.this, error, Toast.LENGTH_LONG).show();
    }
}
