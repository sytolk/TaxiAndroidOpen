package com.opentaxi.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import com.opentaxi.generated.mysql.tables.pojos.Cars;
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
public class CarDetailsActivity extends FragmentActivity {

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
        alertDialogBuilder.setTitle("Нова заявка");
        if (cars != null && (cars.getCurrState().equals(CarState.STATE_FREE.getCode()) || cars.getCurrState().equals(CarState.STATE_BUSY.getCode()))) {
            alertDialogBuilder.setMessage("Сигурни ли сте че искате да направите лична заявка до автомобил " + carNumber + " ?");
            alertDialogBuilder.setPositiveButton("ДА", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                    Intent requestsIntent = new Intent(CarDetailsActivity.this, NewRequestActivity_.class);
                    requestsIntent.putExtra("cars", cars);
                    CarDetailsActivity.this.startActivity(requestsIntent);

                    finish();
                }
            });

            alertDialogBuilder.setNeutralButton("НЕ", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else {
            alertDialogBuilder.setMessage("Съжаляваме но автомобил " + carNumber + " в момента не работи");
            alertDialogBuilder.setNeutralButton("OK", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        Dialog requestDialog = alertDialogBuilder.create();

        if (requestDialog != null) {
            try {
                // Create a new DialogFragment for the error dialog
                MainDialogFragment errorFragment = new MainDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(requestDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(), "requestRequest");
            } catch (Exception e) {
                if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            }
        }
    }

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
}