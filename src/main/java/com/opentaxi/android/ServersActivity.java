/*
package com.opentaxi.android;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.models.Users;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Servers;
import org.androidannotations.annotations.*;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WindowFeature(Window.FEATURE_NO_TITLE)
@EActivity(R.layout.request_servers)
public class ServersActivity extends Activity {

    private static final String TAG = "ServersActivity";

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
            if (socket != null) {
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

                if (testing && socket.getServerType() != null && AppPreferences.getInstance() != null && socket.getServerType().equals(AppPreferences.getInstance().getSocketType()))
                    testServer(socket.getServerHost());

                i++;
            } else {
                updateServers();
                break;
            }
        }
        serversContent.addView(rg);
    }

    @Background
    void testServer(String socket) {
        Log.i(TAG, "testing:" + socket);
        RestClient.getInstance().testSocketConnection(socket);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (refreshScheduler != null) {
            refreshScheduler.shutdown();
            refreshScheduler = null;
        }
        //finish();
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
*/
