package com.opentaxi.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import com.opentaxi.android.R;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Cars;
import com.taxibulgaria.enums.CarState;
import org.androidannotations.annotations.*;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 4/18/13
 * Time: 2:58 PM
 * developer STANIMIR MARINOV
 */
//@WindowFeature(Window.FEATURE_NO_TITLE)
@EFragment(R.layout.car_details)
public class CarDetailsFragment extends BaseFragment {

    private static final String TAG = "CarDetailsFragment";

    //@Extra
    int requestId;

    @ViewById
    TextView carNumberView;

    @ViewById
    TextView driver;

    @ViewById
    LinearLayout carRatingLayout;

    @ViewById
    LinearLayout driverRatingLayout;

    @ViewById
    RatingBar carRating;

    @ViewById
    RatingBar driverRating;

    @ViewById
    Button requestButton;

    private Cars cars;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            requestId = bundle.getInt("requestId");
        }
    }

    @AfterViews
    void afterView() {
        setDetails();
    }

    @Background
    void setDetails() {
        showDetails(RestClient.getInstance().getCarsInfo(requestId));
    }

    @UiThread
    void showDetails(Cars cars) {
        if (cars != null) {
            carNumberView.setText(cars.getNumber());
            //requestButton.append(" " + cars.getNumber());
            driver.setText(cars.getAuthKey());
            if (cars.getCurrPosNorth() != null) {
                carRating.setRating(cars.getCurrPosNorth().floatValue());
                carRatingLayout.setVisibility(View.VISIBLE);
            }
            if (cars.getCurrPosEast() != null) {
                driverRating.setRating(cars.getCurrPosEast().floatValue());
                driverRatingLayout.setVisibility(View.VISIBLE);
            }
            this.cars = cars;
        } else Log.i(TAG, "cars = null requestId:" + requestId);
    }

    @Click
    void okButton() {
        if (mListener != null) mListener.startRequests(true);
    }

    @Click
    void requestButton() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        alertDialogBuilder.setTitle(R.string.NEW_REQUEST_NEW);
        String carNumber = carNumberView != null ? carNumberView.getText().toString() : "";

        if (cars != null && (cars.getCurrState().equals(CarState.STATE_FREE.getCode()) || cars.getCurrState().equals(CarState.STATE_BUSY.getCode()))) {

            alertDialogBuilder.setMessage(mActivity.getString(R.string.private_request_question, carNumber));
            alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    if (mListener != null) mListener.startNewRequest(cars);
                   /* Intent requestsIntent = new Intent(CarDetailsFragment.this, NewRequestActivity_.class);
                    requestsIntent.putExtra("cars", cars);
                    CarDetailsFragment.this.startActivity(requestsIntent);*/

                    //finish();
                }
            });

            alertDialogBuilder.setNeutralButton(R.string.no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else {
            alertDialogBuilder.setMessage(mActivity.getString(R.string.not_working, carNumber));
            alertDialogBuilder.setNegativeButton(mActivity.getString(R.string.no), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialogBuilder.setPositiveButton(mActivity.getString(R.string.yes), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (mListener != null) mListener.startNewRequest(null);
                }
            });
        }

        Dialog requestDialog = alertDialogBuilder.create();
        requestDialog.show();
    }
}