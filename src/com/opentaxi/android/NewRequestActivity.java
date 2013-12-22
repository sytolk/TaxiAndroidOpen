package com.opentaxi.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.opentaxi.android.adapters.GroupsAdapter;
import com.opentaxi.android.adapters.RegionsAdapter;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.generated.mysql.tables.pojos.Groups;
import com.opentaxi.generated.mysql.tables.pojos.Regions;
import com.opentaxi.models.NewRequest;
import com.opentaxi.rest.RestClient;
import com.taxibulgaria.enums.RequestSource;
import org.androidannotations.annotations.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/25/13
 * Time: 10:03 AM
 * To change this template use File | Settings | File Templates.
 */
@EActivity(R.layout.new_request)
public class NewRequestActivity extends FragmentActivity {

    private static final String TAG = "NewRequestActivity";

    @ViewById(R.id.pricesPicker)
    Spinner pricesPicker;

    @ViewById(R.id.address)
    TextView address;

    @ViewById(R.id.addressText)
    EditText addressText;

    @ViewById(R.id.region)
    TextView region;

    @ViewById(R.id.regionsPicker)
    Spinner regionsPicker;

    @ViewById(R.id.addressChange)
    Button addressChange;

    @ViewById(R.id.llFilters)
    LinearLayout llFilters;

    @ViewById(R.id.reqInfoButtonContainer)
    LinearLayout reqInfoButtonContainer;

    @ViewById(R.id.pbProgress)
    ProgressBar pbProgress;

    @ViewById(R.id.requestSend)
    Button requestSend;

    @AfterViews
    protected void afterActivity() {

        setRegions();
        setPrices();
        setGroups();
        address.setText("Адреса се определя автоматично според координатите ви. Моля изчакайте...");
        addressText.setVisibility(View.GONE);
    }

    @Background
    void setRegions() {
        showRegions(RestClient.getInstance().getRegions());
    }

    @UiThread
    void showRegions(Regions[] regions) {
        if (regions != null) {
            RegionsAdapter[] regionsAdapter = new RegionsAdapter[regions.length];
            int i = 0;
            for (Regions regionObj : regions) {
                regionsAdapter[i] = new RegionsAdapter(regionObj);
                i++;
            }
            ArrayAdapter<RegionsAdapter> adapter2 = new ArrayAdapter<RegionsAdapter>(this, R.layout.spinner_layout, regionsAdapter);
            adapter2.setDropDownViewResource(R.layout.spinner_layout);
            regionsPicker.setAdapter(adapter2);
        }
        setAddress();
    }

    @Background
    void setGroups() {
        showGroups(RestClient.getInstance().getClientVisibleGroups());
    }

    @UiThread
    void showGroups(Groups[] groups) {
        if (groups != null) {
            for (Groups group : groups) {
                CheckBox cb = new CheckBox(this);
                cb.setText(group.getDescription());
                cb.setId(group.getGroupsId());
                llFilters.addView(cb);
            }
        }
    }

    @Background
    void setPrices() {
        Log.i(TAG, "getPrices");
        showPrices(RestClient.getInstance().getPrices());
    }

    @UiThread
    void showPrices(Groups[] prices) {
        GroupsAdapter[] groupsAdapters;
        if (prices != null && prices.length > 0) {
            groupsAdapters = new GroupsAdapter[prices.length];
            int i = 0;
            for (Groups group : prices) {
                groupsAdapters[i] = new GroupsAdapter(group);
                i++;
            }
            requestSend.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, "prices=null");
            groupsAdapters = new GroupsAdapter[1];
            Groups group = new Groups();
            group.setGroupsId(0);
            group.setDescription("Няма свободни коли. Моля опитайте по късно");
            groupsAdapters[0] = new GroupsAdapter(group);
            requestSend.setVisibility(View.GONE);
        }

        ArrayAdapter<GroupsAdapter> adapter2 = new ArrayAdapter<GroupsAdapter>(this, R.layout.spinner_layout, groupsAdapters);
        adapter2.setDropDownViewResource(R.layout.spinner_layout);
        pricesPicker.setAdapter(adapter2);
    }

    @Background
    void setAddress() {
        Log.i(TAG, "setAddress");
        if (AppPreferences.getInstance() != null) {
            Date now = new Date();
            if (AppPreferences.getInstance().getGpsLastTime() > (now.getTime() - 600000)) {  //if last coordinates time is from 5 min interval
                com.opentaxi.generated.mysql.tables.pojos.NewRequest address = RestClient.getInstance().getAddressByCoordinates(AppPreferences.getInstance().getNorth().floatValue(), AppPreferences.getInstance().getEast().floatValue());
                if (address != null) showAddress(address);
                else {
                    //todo make it solr geonames api
                    //address = GoogleApiRestClient.getInstance().getAddress(AppPreferences.getInstance().getNorth(), AppPreferences.getInstance().getEast());
                    //showAddress(address);
                    showAddress(null);
                }
            } else {
                showAddress(null);
                Log.i(TAG, "GpsLastTime " + AppPreferences.getInstance().getGpsLastTime() + " > " + (now.getTime() - 600000) + " min");
            }
        }
    }

    @UiThread
    void showAddress(com.opentaxi.generated.mysql.tables.pojos.NewRequest adr) {
        if (adr != null) {
            selectRegionsItemById(regionsPicker, adr.getRegionId());
            address.setText(adr.getFullAddress());
            addressChange.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, "address=null");
            address.setText("Адрес: ");
            addressText.setVisibility(View.VISIBLE);
        }
    }

    public void selectRegionsItemById(Spinner spnr, Integer regionsId) {
        Log.i(TAG, "selectRegionsItemById:" + regionsId);
        if (regionsId != null) {
            try {
                RegionsAdapter regionsAdapter = new RegionsAdapter();
                regionsAdapter.setId(regionsId);
                ArrayAdapter<RegionsAdapter> myAdap = (ArrayAdapter<RegionsAdapter>) spnr.getAdapter(); //cast to an ArrayAdapter
                if (myAdap != null) {
                    int spinnerPosition = myAdap.getPosition(regionsAdapter);
                    Log.i(TAG, "spinnerPosition:" + spinnerPosition);
                    //set the default according to value
                    spnr.setSelection(spinnerPosition);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception selectRegionsItemById:" + regionsId);
                if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
           /* if (requestCode == REJECTION) {
                finish();
            }*/
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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

    @Click
    void requestSend() {
        String txt = null;
        if (addressText.getVisibility() == View.VISIBLE) txt = addressText.getText().toString();
        else txt = address.getText().toString();

        if (txt != null && txt.length() > 1) {
            reqInfoButtonContainer.setVisibility(View.GONE);
            pbProgress.setVisibility(View.VISIBLE);

            NewRequest newRequest = new NewRequest();
            RegionsAdapter regionsAdapter = (RegionsAdapter) regionsPicker.getSelectedItem();
            newRequest.setRegionId(regionsAdapter.getId());

            newRequest.setFullAddress(txt);

            List<Groups> filterGroups = new ArrayList<Groups>();
            Groups[] visibleGroups = RestClient.getInstance().getClientVisibleGroups();
            if (visibleGroups != null) {
                for (Groups group : visibleGroups) {
                    CheckBox cb = (CheckBox) findViewById(group.getGroupsId());
                    if (cb.isChecked()) filterGroups.add(group);
                }
            }
            GroupsAdapter priceAdapter = (GroupsAdapter) pricesPicker.getSelectedItem();
            if (priceAdapter != null) filterGroups.add(priceAdapter.getGroups());

            newRequest.setRequestGroups(filterGroups);
            newRequest.setSource(RequestSource.ANDROID.getCode());

            sendRequest(newRequest);
        } else {
            if (addressText.getVisibility() == View.VISIBLE) addressText.setError("Задължително поле");
            else Toast.makeText(this, "Адреса е задължително поле", Toast.LENGTH_SHORT).show();
        }
    }

    @Background
    void sendRequest(NewRequest newRequest) {
        Integer requestId = RestClient.getInstance().sendRequest(newRequest);
        if (requestId != null && requestId > 0) {
            SuccessDialog();
        }
    }

    @Click
    void addressChange() {
        addressText.setText(address.getText());
        address.setText("Адрес: ");
        addressText.setVisibility(View.VISIBLE);
        addressChange.setVisibility(View.GONE);
    }

    @UiThread
    void SuccessDialog() {
        reqInfoButtonContainer.setVisibility(View.VISIBLE);
        pbProgress.setVisibility(View.GONE);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Изпратена заявка");
        String txt = addressText.getText().toString();
        if (txt.length() == 0) txt = address.getText().toString();
        alertDialogBuilder.setMessage("Заявката " + txt + " беше изпратена успешно! Можете да видите актуалния и статус да я промените или откажете след като затворите този диалог или от бутона ПОРЪЧКИ на главната страница");

        alertDialogBuilder.setNeutralButton("ОК", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                RequestsActivity_.intent(NewRequestActivity.this).start();
                finish();
            }
        });

        Dialog successDialog = alertDialogBuilder.create();

        if (successDialog != null) {
            try {
                // Create a new DialogFragment for the error dialog
                MainDialogFragment errorFragment = new MainDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(successDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(), "newRequestSuccess");
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