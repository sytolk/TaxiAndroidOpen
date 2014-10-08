package com.opentaxi.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import com.stil.generated.mysql.tables.pojos.Cars;
import com.opentaxi.rest.RestClient;
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
@EActivity(R.layout.car_details)
public class CarDetailsActivity extends Activity {

    private static final String TAG = "CarDetailsActivity";

    @Extra
    String carNumber;

    @ViewById
    TextView carNumberView;

    @ViewById
    TextView driver;

    @ViewById
    RatingBar rating;

    @ViewById
    Button requestButton;

    private Cars cars;

    @AfterViews
    void afterRequestsActivity() {
        setDetails();
    }

    @Background
    void setDetails() {
        showDetails(RestClient.getInstance().getCarsInfo(carNumber));
    }

    @UiThread
    void showDetails(Cars cars) {
        if (cars != null) {
            carNumberView.setText(cars.getNumber());
            requestButton.append(" " + cars.getNumber());
            driver.setText(cars.getAuthKey());
            if (cars.getDescription() != null && !cars.getDescription().equals("")) {
               /* rating.setFocusable(false);
                rating.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });*/
                try {
                    rating.setRating(Float.parseFloat(cars.getDescription()));
                } catch (NumberFormatException e) {
                    Log.e(TAG, "setRating");
                }
            }
            this.cars = cars;
        }
    }

    @Click
    void okButton() {
        finish();
    }

    @Click
    void requestButton() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.new_request);
        if (cars != null && (cars.getCurrState().equals(CarState.STATE_FREE.getCode()) || cars.getCurrState().equals(CarState.STATE_BUSY.getCode()))) {
            alertDialogBuilder.setMessage(getString(R.string.private_request_question, carNumber));
            alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    Intent requestsIntent = new Intent(CarDetailsActivity.this, NewRequestActivity_.class);
                    requestsIntent.putExtra("cars", cars);
                    CarDetailsActivity.this.startActivity(requestsIntent);

                    finish();
                }
            });

            alertDialogBuilder.setNeutralButton(R.string.no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else {
            alertDialogBuilder.setMessage(getString(R.string.not_working, carNumber));
            alertDialogBuilder.setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        Dialog requestDialog = alertDialogBuilder.create();
        requestDialog.show();
    }
}