package com.opentaxi.android.fragments;

import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.google.gson.JsonSyntaxException;
import com.opentaxi.android.R;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Servers;
import com.taxibulgaria.enums.UsersGroupEnum;
import org.androidannotations.annotations.*;
import org.androidannotations.api.BackgroundExecutor;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 4/17/13
 * Time: 10:18 AM
 * developer STANIMIR MARINOV
 */
//@WindowFeature(Window.FEATURE_NO_TITLE)
@EFragment(R.layout.request_servers)
public class ServersFragment extends BaseFragment {

    private static final String TAG = "ServersActivity";

    @ViewById(R.id.serversContent)
    LinearLayout serversContent;

    @AfterViews
    void afterServers() {
        RestClient.getInstance().cleanSocketCheck();
        showServers(true);
    }

    @Background(delay = 2000, id = "timeout_check")
    void scheduleTimeoutCheck() {
        if (isVisible()) showServers(false);
    }

    @UiThread
    void showServers(boolean testing) {
        //Log.i(TAG, "showServers testing:" + testing);
        if (serversContent != null) {
            serversContent.removeAllViews();

            List<Servers> sockets = RestClient.getInstance().getSockets();
            String currSocket = RestClient.getInstance().getCurrentSocket();

            final RadioButton[] rb = new RadioButton[sockets.size()];
            RadioGroup rg = new RadioGroup(mActivity); //create the RadioGroup
            rg.setOrientation(RadioGroup.VERTICAL);
            int i = 0;
            for (final Servers server : sockets) {
                if (server.getDescription() != null) {
                    //Log.i(TAG, socket.getServerHost() + " " + socket.getDescription());
                    if (server.getDescription().toLowerCase().contains("cloud")) {
                        if (!isAdmin()) continue;
                    } //else Log.i(TAG, "not contains " + socket.getDescription());

                    rb[i] = new RadioButton(mActivity);
                    rg.addView(rb[i]); //the RadioButtons are added to the radioGroup instead of the layout

                    //CheckBox ch = new CheckBox(this);
                    StringBuilder title = new StringBuilder();
                    title.append(server.getDescription()).append(" "); //.append(socket.getServerHost());
                    if (server.getRecordstatus()) {
                        title.append("UP");
                        rb[i].setBackgroundColor(ContextCompat.getColor(mActivity, R.color.label_color));
                    } else {
                        title.append("DOWN");
                        rb[i].setBackgroundColor(ContextCompat.getColor(mActivity, R.color.red_color));
                    }
                    rb[i].setText(title.toString());

                    String hostSecure = server.getServerDomain() + (server.getSecurePort() != null ? ":" + server.getSecurePort() : "");
                    String host = server.getServerDomain() + (server.getServerPort() != null ? ":" + server.getServerPort() : "");

                    if (currSocket.equals(hostSecure) || currSocket.equals(host) || currSocket.equals(server.getServerHost())) {
                        rb[i].setChecked(true);
                    }

                    rb[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Integer oldType = AppPreferences.getInstance(mActivity).getSocketType();
                            AppPreferences.getInstance(mActivity).setSocketType(server.getServerType());
                            if (RestClient.getInstance().changeServerSockets(server)) {
                                if (oldType == null || !oldType.equals(server.getServerType())) login();
                                showServers(false);
                            }
                        }
                    });

                    if (testing && server.getServerType().equals(AppPreferences.getInstance(mActivity).getSocketType()))
                        testServer(server.getServerHost());

                    i++;
                }
            }
            serversContent.addView(rg);
            scheduleTimeoutCheck();
        }
        //serversContent.invalidate();
    }

    @Background
    void testServer(String socket) {
        Log.i(TAG, "testing:" + socket);
        RestClient.getInstance().testSocketConnection(socket);
    }

    @Override
    public void onPause() {
        super.onPause();
        BackgroundExecutor.cancelAll("timeout_check", true);
    }

    private boolean isAdmin() {
        try {
            String strUsersGroup = AppPreferences.getInstance().getUsers().getUrllogin();
            if (strUsersGroup != null && !strUsersGroup.equals("") && AppPreferences.getInstance() != null) {
                try {
                    Integer[] usersGroup = RestClient.getInstance().getObjectMapper().readValue(strUsersGroup, Integer[].class); //fromJson
                    if (usersGroup != null) {
                        if (Arrays.asList(usersGroup).contains(UsersGroupEnum.ADMINISTRATORS.getCode())) return true;
                    }
                } catch (JsonSyntaxException e) {//IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Click
    void cancelButton() {
        mListener.startHome();
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
        com.taxibulgaria.rest.models.Users user = RestClient.getInstance().Login();

        if (user != null) {
            if (user.getId() != null && user.getId() > 0) {
                AppPreferences.getInstance(mActivity).setUsers(user);
                startHome();
            } else loginError("Грешно потребителско име или парола!");
            // Toast.makeText(UserPassActivity.this, "Грешно потребителско име или парола!", Toast.LENGTH_LONG).show();

        } else loginError("Грешка! Сигурни ли сте че имате връзка с интернет?");
    }

    @UiThread
    void startHome() {
        if (mListener != null) {
            mListener.reloadMenu();
            mListener.startHome();
        }
    }

    @UiThread
    void loginError(String error) {
        Toast.makeText(mActivity, error, Toast.LENGTH_LONG).show();
    }
}