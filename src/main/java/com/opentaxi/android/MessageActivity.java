package com.opentaxi.android;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Messages;
import com.taxibulgaria.enums.RequestAcceptStatus;
import com.taxibulgaria.enums.RequestStatus;
import com.taxibulgaria.rest.models.NewCRequestDetails;
import com.taxibulgaria.rest.models.NewRequestDetails;
import org.androidannotations.annotations.*;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/25/13
 * Time: 10:03 AM
 * To change this template use File | Settings | File Templates.
 */
@EActivity(R.layout.msg_layout)
public class MessageActivity extends Activity {


    @ViewById
    TextView msgText;

    @ViewById
    Button okButton;

    @ViewById
    Button yesButton;

    @Extra
    Messages messages;

    @Extra
    NewRequestDetails requestDetails;

    @AfterViews
    void afterViews() {
        if (messages != null) {

            msgText.setText(messages.getMsg());

            /*if (messages.getUsersFromId() != null) {
                UsersDetails userFrom = AppPreferences.getInstance(getApplicationContext()).getUserById(messages.getUsersFromId());
                if (userFrom != null) {
                    TextView fromView = (TextView) findViewById(R.id.requestGroups);
                    StringBuilder fromTxt = new StringBuilder();
                    fromTxt.append("От: ").append(userFrom.getUsername());
                    if (userFrom.getContact() != null)
                        fromTxt.append(" (").append(userFrom.getContact().getFirstname()).append(" ").append(userFrom.getContact().getLastname()).append(")");
                    fromView.setText(fromTxt);
                }
            }*/
            okButton.setText(R.string.okbutton);
            yesButton.setVisibility(View.GONE);

            dimDisplay();
        } else if (requestDetails != null) {

            if (RequestStatus.NEW_REQUEST_DELETE.getCode().equals(requestDetails.getStatus())) { //DELETE REQUEST
                msgText.setText(getString(R.string.request_rejected, requestDetails.getRequestsId(), requestDetails.getFullAddress()));

            } else if (RequestStatus.NEW_REQUEST_EDIT.getCode().equals(requestDetails.getStatus())) { //EDIT REQUEST
                msgText.setText(getString(R.string.request_edited, requestDetails.getRequestsId(), requestDetails.getFullAddress()));

            } else if (RequestStatus.NEW_REQUEST_APPROVED.getCode().equals(requestDetails.getStatus())) { //APPROVED REQUEST
                msgText.setText(getString(R.string.request_aproved, requestDetails.getRequestsId(), requestDetails.getFullAddress(), getTaxiNumber()));

            } else if (RequestStatus.ERROR_INVALID_REQUEST.getCode().equals(requestDetails.getStatus())) { //INVALID REQUEST
                msgText.setText(getString(R.string.request_invalid, requestDetails.getRequestsId(), requestDetails.getFullAddress()));

            } else if (RequestAcceptStatus.RI_PRIVATE_REQUEST.getCode().equals(requestDetails.getAcceptType())) { //Private rejected
                msgText.setText(getString(R.string.private_request_rejected, requestDetails.getRequestsId(), requestDetails.getFullAddress(), getTaxiNumber()));

                okButton.setText(R.string.no);
                yesButton.setVisibility(View.VISIBLE);

            /*} else if (RequestAcceptStatus.RI_TRANSFER_SUCCESS.getCode().equals(request.getAcceptType())) { //TRANSFER SUCCESSFUL
                //deleteRequest(request.getRequestsId(), context.getString(R.string.transfer_success, request.getFullAddress()));
            } else if (RequestAcceptStatus.RI_TRANSFER.getCode().equals(request.getAcceptType())) { //TRANSFER FAILED
                //messages.setMsg("Трансфера е неуспешен! Моля изпълнете заявката (" + AppPreferences.getInstance().getRequestText(request) + ")");*/
            } else {
                Log.i("MessageActivity", "unknown requestDetails id:" + requestDetails.getRequestsId());
                finish();
            }
        } else finish();
    }

    private String getTaxiNumber() {
        if (requestDetails.getDetails() != null && requestDetails.getDetails().getCondition() != null) {
            return getString(R.string.from_taxi, requestDetails.getDetails().getCondition());
        }
        return "";
    }

    @Override
    public void onAttachedToWindow() {

        super.onAttachedToWindow();
        addFlags();
    }

    private void addFlags() {
        //Screen On
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    private void clearFlags() {
        //Don't forget to clear the flags at some point in time.
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @UiThread(delay = 15000)
    void dimDisplay() {
        clearFlags();
        /*if (!isFinishing() && TaxiApplication.isMsgVisible()) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.screenBrightness = 0.0f;// i needed to dim the display
            getWindow().setAttributes(params);
        }*/
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        if (hasFocus) {
            TaxiApplication.msgResumed();
        } else {
            TaxiApplication.msgPaused();
            clearFlags();
        }

        super.onWindowFocusChanged(hasFocus);
    }

    @Click
    void okButton() {
        clearFlags();
        //container();
        finish();
        //Intent intent = new Intent(MessageActivity.this, StilActivity.class);
        //MessageActivity.this.startActivityIfNeeded(intent, -1);
    }

    @Click
    void yesButton() {
        clearFlags();
        //container();
        finish();

        refreshRequest(requestDetails.getRequestsId());

        NewCRequestDetails newCRequestDetails = new NewCRequestDetails();
        newCRequestDetails.setRequestsId(requestDetails.getRequestsId());
        newCRequestDetails.setRegionId(requestDetails.getRegionId());
        newCRequestDetails.setFullAddress(requestDetails.getFullAddress());
        newCRequestDetails.setDatecreated(requestDetails.getDatecreated());
        //newCRequestDetails.setRequestGroups(requestDetails.getRequestGroups());
        newCRequestDetails.setCarId(requestDetails.getCarId());
        //newCRequestDetails.setCarNumber(requestDetails.getCarNumber());
        newCRequestDetails.setStatus(requestDetails.getStatus());

        Intent i = new Intent(this, MainActivity_.class);
        i.putExtra("startRequestDetails", newCRequestDetails);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    @Background
    void refreshRequest(Integer requestsId) {
        RestClient.getInstance().refreshRequest(requestsId);
    }

    /*@Touch
    void container() {
        //Log.i("container","Click");
        if (!isFinishing() && TaxiApplication.isMsgVisible()) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.screenBrightness = 1f;
            getWindow().setAttributes(params);
        }
    }*/
}