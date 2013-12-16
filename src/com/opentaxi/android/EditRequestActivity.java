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
import com.opentaxi.generated.mysql.tables.pojos.Groups;
import com.opentaxi.generated.mysql.tables.pojos.Regions;
import com.opentaxi.models.NewCRequest;
import com.opentaxi.models.NewRequest;
import com.opentaxi.rest.RestClient;
import org.androidannotations.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/25/13
 * Time: 10:03 AM
 * To change this template use File | Settings | File Templates.
 */
@EActivity(R.layout.new_request)
public class EditRequestActivity extends FragmentActivity {

    private static final String TAG = "NewRequestActivity";

    @Extra
    NewCRequest newCRequest;

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

    @ViewById
    Button requestSend;

    @AfterViews
    protected void afterActivity() {

        requestSend.setText("Промени заявката");
        setRegions();
        setPrices();
        setGroups();
        //address.setText("Адреса се определя автоматично според координатите ви. Моля изчакайте...");
        //addressText.setVisibility(View.GONE);
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
            int spinnerPosition = 0;
            for (Regions regionObj : regions) {
                regionsAdapter[i] = new RegionsAdapter(regionObj);
                if (newCRequest != null) {
                    if (regionObj.getId().equals(newCRequest.getRegionId())) spinnerPosition = i;
                }
                i++;
            }
            ArrayAdapter<RegionsAdapter> adapter2 = new ArrayAdapter<RegionsAdapter>(this, R.layout.spinner_layout, regionsAdapter);
            adapter2.setDropDownViewResource(R.layout.spinner_layout);
            regionsPicker.setAdapter(adapter2);
            if (newCRequest != null) regionsPicker.setSelection(spinnerPosition);
        }
        showAddress();
    }

    @Background
    void setGroups() {
        showGroups(RestClient.getInstance().getClientVisibleGroups());
    }

    @UiThread
    void showGroups(Groups[] groups) {
        if (newCRequest != null) {
            Map<String, List<Groups>> groupsMap = newCRequest.getRequestGroups();

            if (groups != null) {
                for (Groups group : groups) {
                    CheckBox cb = new CheckBox(this);
                    cb.setText(group.getDescription());
                    cb.setId(group.getGroupsId());
                    if (groupsMap.containsKey(group.getName())) cb.setChecked(true);
                    llFilters.addView(cb);
                }
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
        if (prices != null && prices.length > 0 && newCRequest != null) {
            Groups chooseGroup = null;
            Map<String, List<Groups>> groupsMap = newCRequest.getRequestGroups();
            if (groupsMap.containsKey("PRICE_GROUPS")) {
                List<Groups> priceGroups = groupsMap.get("PRICE_GROUPS");
                if (priceGroups.size() > 0) {
                    chooseGroup = priceGroups.get(0);
                    //if (priceGroup != null)
                    //selectPriceItemById(pricesPicker, priceGroup);
                }
            }

            GroupsAdapter[] groupsAdapters = new GroupsAdapter[prices.length];
            int i = 0;
            int spinnerPosition = 0;
            for (Groups group : prices) {
                groupsAdapters[i] = new GroupsAdapter(group);
                if (chooseGroup != null && chooseGroup.getGroupsId().equals(group.getGroupsId())) spinnerPosition = i;
                i++;
            }

            ArrayAdapter<GroupsAdapter> adapter2 = new ArrayAdapter<GroupsAdapter>(this, R.layout.spinner_layout, groupsAdapters);
            adapter2.setDropDownViewResource(R.layout.spinner_layout);
            pricesPicker.setAdapter(adapter2);

            pricesPicker.setSelection(spinnerPosition);

        } else Log.e(TAG, "prices=null");
    }

    void showAddress() {
        address.setText("Адрес: ");
        if (newCRequest != null) addressText.setText(newCRequest.getFullAddress());
        addressText.setVisibility(View.VISIBLE);
    }

    /*public void selectRegionsItemById(Spinner spnr, Integer regionsId) {
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
    }*/

    /*public void selectPriceItemById(Spinner spnr, Groups groups) {
        if (groups != null) {
            Log.i(TAG, "selectPriceItemById:" + groups.getDescription());
            try {
                GroupsAdapter groupsAdapter = new GroupsAdapter(groups);
                ArrayAdapter<GroupsAdapter> myAdap = (ArrayAdapter<GroupsAdapter>) spnr.getAdapter(); //cast to an ArrayAdapter
                if (myAdap != null) {
                    int spinnerPosition = myAdap.getPosition(groupsAdapter);
                    Log.i(TAG, "spinnerPosition:" + spinnerPosition);
                    //set the default according to value
                    spnr.setSelection(spinnerPosition);
                } else Log.e(TAG, "myAdap:null");
            } catch (Exception e) {
                Log.e(TAG, "Exception selectPriceItemById:" + groups.getName());
                if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            }
        }
    }*/

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
        String txt = addressText.getText().toString();
        if (txt.length() == 0) txt = address.getText().toString();

        if (txt.length() > 1 && newCRequest != null) {
            reqInfoButtonContainer.setVisibility(View.GONE);
            pbProgress.setVisibility(View.VISIBLE);

            NewRequest newRequest = new NewRequest();
            newRequest.setRequestsId(newCRequest.getRequestsId());
            RegionsAdapter regionsAdapter = (RegionsAdapter) regionsPicker.getSelectedItem();
            newRequest.setRegionId(regionsAdapter.getId());

            newRequest.setFullAddress(txt);

            List<Groups> filterGroups = new ArrayList<Groups>();
            Groups[] visibleGroups = RestClient.getInstance().getClientVisibleGroups();
            for (Groups group : visibleGroups) {
                CheckBox cb = (CheckBox) findViewById(group.getGroupsId());
                if (cb.isChecked()) filterGroups.add(group);
            }
            GroupsAdapter priceAdapter = (GroupsAdapter) pricesPicker.getSelectedItem();
            if (priceAdapter != null) filterGroups.add(priceAdapter.getGroups());

            newRequest.setRequestGroups(filterGroups);
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
        alertDialogBuilder.setTitle("Променена заявка");
        String txt = addressText.getText().toString();
        if (txt.length() == 0) txt = address.getText().toString();
        alertDialogBuilder.setMessage("Заявката " + txt + " беше променена успешно! Можете да видите актуалния и статус да я промените или откажете след като затворите този диалог или от бутона ПОРЪЧКИ на главната страница");

        alertDialogBuilder.setNeutralButton("ОК", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                RequestsActivity_.intent(EditRequestActivity.this).start();
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