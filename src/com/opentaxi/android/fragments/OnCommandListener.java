package com.opentaxi.android.fragments;

import com.stil.generated.mysql.tables.pojos.Cars;
import com.taxibulgaria.rest.models.NewCRequestDetails;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 11/2/15
 * Time: 2:37 PM
 * developer STANIMIR MARINOV
 * <p/>
 * Container Activity must implement this interface
 */
public interface OnCommandListener {

    void startHome();

    void fabVisible(boolean isVisible);

    void closeKeyboard();

    void startNewRequest(Cars cars);

    void startCarDetails(Integer requestId);

    void startRequestDetails(NewCRequestDetails newRequest);

    void startEditRequest(NewCRequestDetails newCRequest);

    void startRequests(boolean history);

    boolean playServicesConnected();

    void setBarTitle(String string);

    void reloadMenu();

    void startLostPassword();

    void startNewClient();
}
