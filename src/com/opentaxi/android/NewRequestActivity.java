package com.opentaxi.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.opentaxi.android.adapters.GroupsAdapter;
import com.opentaxi.android.adapters.RegionsAdapter;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.generated.mysql.tables.pojos.*;
import com.opentaxi.models.NewRequestDetails;
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
public class NewRequestActivity extends Activity {

    private static final String TAG = "NewRequestActivity";
    private static final int SHOW_ADDRESS_ON_MAP = 999;

    @Extra
    Cars cars;

    com.opentaxi.generated.mysql.tables.pojos.NewRequest newRequest;

    @ViewById(R.id.pricesPicker)
    Spinner pricesPicker;

    @ViewById(R.id.address)
    TextView address;

    @ViewById(R.id.addressText)
    EditText addressText;

    @ViewById(R.id.citiesPicker)
    AutoCompleteTextView citiesPicker;

    @ViewById(R.id.region)
    TextView region;

    @ViewById(R.id.regionsPicker)
    AutoCompleteTextView regionsPicker;

    @ViewById(R.id.destination)
    AutoCompleteTextView destination;

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

    @ViewById(R.id.regionsLayout)
    LinearLayout regionsLayout;

    @ViewById(R.id.destLayout)
    LinearLayout destLayout;

    LocationInfo latestInfo;

    @AfterViews
    protected void afterActivity() {
        if (cars == null) setTitle(getString(R.string.taxi_request));
        else setTitle(getString(R.string.taxi_request_to_car, cars.getNumber()));

        String[] cities = new String[]{
                "Бургас", "София", "Варна", "Пловдив", "Burgas", "Sofia", "Varna", "Plovdiv", "Несебър", "Nesebar", "Слънчев бряг", "Sunny beach", "Приморско", "Primorsko", "Царево", "Carevo", "Созопол", "Sozopol"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, cities);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        citiesPicker.setAdapter(adapter);

        setCities();
        //showCities("Бургас");
        setRegions();
        setPrices();
        setGroups();
        address.setText(R.string.wait_address);

        addressText.setVisibility(View.GONE);
        latestInfo = new LocationInfo(getBaseContext());
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, resultCode + " " + resultCode);
        if (requestCode == SHOW_ADDRESS_ON_MAP) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    com.opentaxi.generated.mysql.tables.pojos.NewRequest newRequest = (com.opentaxi.generated.mysql.tables.pojos.NewRequest) extras.getSerializable("newRequest");
                    showAddress(newRequest);
                }
            } else Log.e(TAG, "" + resultCode);
        }
    }*/

    @OnActivityResult(SHOW_ADDRESS_ON_MAP)
    void onResult(int resultCode, Intent data) {
        //Log.i(TAG, "" + resultCode + " " + data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                this.newRequest = (com.opentaxi.generated.mysql.tables.pojos.NewRequest) extras.getSerializable("newRequest");
                showAddress(newRequest);
            }
        } else Log.e(TAG, "" + resultCode);
    }

    @Background
    void setCities() {
        showCities(RestClient.getInstance().getAddress());
    }

    @UiThread
    void showCities(Contactaddress contactAddress) {
            /*CitiesAdapter[] citiesAdapter = new CitiesAdapter[1];
            citiesAdapter[0] = new CitiesAdapter(supported);
            ArrayAdapter<CitiesAdapter> adapter2 = new ArrayAdapter<CitiesAdapter>(this, R.layout.spinner_layout, citiesAdapter);
            adapter2.setDropDownViewResource(R.layout.spinner_layout);*/
        if (contactAddress != null && contactAddress.getCity() != null) {
            //Log.d(TAG, "Contactaddress:" + address.getCity() + ":" + address.getCountryinfogeonamesid());

            citiesPicker.setText(contactAddress.getCity());
            address.setFocusable(true);
            address.setFocusableInTouchMode(true);
            address.requestFocus();

            if (contactAddress.getCountryinfogeonamesid() != null && contactAddress.getCountryinfogeonamesid().equals(732770)) { //Burgas
                regionsLayout.setVisibility(View.VISIBLE);
                destLayout.setVisibility(View.GONE);
            } else {
                regionsLayout.setVisibility(View.GONE);
                destLayout.setVisibility(View.VISIBLE);
            }
        } //else Log.d(TAG, "Contactaddress=null");
        //citiesPicker.setOnTouchListener(Spinner_OnTouch);
    }

    /*private View.OnTouchListener Spinner_OnTouch = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                notSupportedDialog();
            }
            return true;
        }
    };*/

    @Background
    void setRegions() {
        showRegions(RestClient.getInstance().getRegions());
    }

    @UiThread
    void showRegions(Regions[] regions) {
        if (regions != null) {
            RegionsAdapter[] regionsAdapter = new RegionsAdapter[regions.length + 1];
            Regions emptyRegion = new Regions();
            emptyRegion.setId(0);
            emptyRegion.setDescription("");
            regionsAdapter[0] = new RegionsAdapter(emptyRegion);
            int i = 1;
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
                //Log.i(TAG, "showGroups:" + group.getGroupsId() + " " + group.getDescription());
                if (group.getGroupsId() != null && group.getDescription() != null) {
                    CheckBox cb = new CheckBox(this);
                    cb.setText(group.getDescription());
                    cb.setId(group.getGroupsId());
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
            group.setDescription(getString(R.string.no_free_cars));
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
        if (this.newRequest == null && AppPreferences.getInstance() != null) {
            com.opentaxi.generated.mysql.tables.pojos.NewRequest address = null;
            Date now = new Date();
            if (AppPreferences.getInstance().getGpsLastTime() > (now.getTime() - 600000)) {  //if last coordinates time is from 5 min interval
                address = RestClient.getInstance().getAddressByCoordinates(AppPreferences.getInstance().getNorth().floatValue(), AppPreferences.getInstance().getEast().floatValue());
            } else if (latestInfo != null && latestInfo.lastLocationUpdateTimestamp > (now.getTime() - 600000)) {
                latestInfo.refresh(getBaseContext());
                address = RestClient.getInstance().getAddressByCoordinates(latestInfo.lastLat, latestInfo.lastLong);
            } else {
                Log.i(TAG, "GpsLastTime " + AppPreferences.getInstance().getGpsLastTime() + " > " + (now.getTime() - 600000) + " min latestInfo:" + latestInfo);
            }

            if (address != null) showAddress(address);
            else {
                //todo make it solr geonames api
                //address = GoogleApiRestClient.getInstance().getAddress(AppPreferences.getInstance().getNorth(), AppPreferences.getInstance().getEast());
                //showAddress(address);
                showAddress(null);
            }
        }
    }

    @UiThread
    void showAddress(com.opentaxi.generated.mysql.tables.pojos.NewRequest adr) {
        if (adr != null) {
            // selectRegionsItemById(regionsPicker, adr.getRegionId());
            Regions region = RestClient.getInstance().getRegionById(adr.getRegionId());
            if (region != null) regionsPicker.setText(region.getDescription());
            address.setText(adr.getFullAddress());
            addressChange.setVisibility(View.VISIBLE);
            addressText.setVisibility(View.GONE);
        } else {
            Log.e(TAG, "address=null");
            address.setText(R.string.address);
            addressText.setVisibility(View.VISIBLE);
        }
    }

    /*public void selectRegionsItemById(AutoCompleteTextView spnr, Integer regionsId) {
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

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
           *//* if (requestCode == REJECTION) {
                finish();
            }*//*
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
    }*/

    @Click
    void requestSend() {
        String txt = null;
        if (addressText.getVisibility() == View.VISIBLE) txt = addressText.getText().toString();
        else txt = address.getText().toString();

        String city = citiesPicker.getText().toString().trim();
        if (city == null || city.isEmpty()) {
            if (citiesPicker.getVisibility() == View.VISIBLE) citiesPicker.setError(getString(R.string.required_field));
            else
                Toast.makeText(this, getString(R.string.required_field) + ": " + getString(R.string.city), Toast.LENGTH_SHORT).show();
        } else if (txt != null && txt.length() > 1 && !txt.equals(getString(R.string.wait_address))) {
            reqInfoButtonContainer.setVisibility(View.GONE);
            pbProgress.setVisibility(View.VISIBLE);

            NewRequestDetails newRequest = new NewRequestDetails();
            if (this.newRequest != null) {
                newRequest.setNorth(this.newRequest.getNorth());
                newRequest.setEast(this.newRequest.getEast());
            }
            if (cars != null) newRequest.setCarId(cars.getId());
            RequestsDetails requestsDetails = new RequestsDetails();

            requestsDetails.setFromCity(city);
            if (destination.getText() != null && !destination.getText().toString().isEmpty())
                requestsDetails.setDestination(destination.getText().toString());
            newRequest.setDetails(requestsDetails);

            //RegionsAdapter regionsAdapter = (RegionsAdapter) regionsPicker.getSelectedItem();
            if (regionsPicker.getText() != null && !regionsPicker.getText().toString().isEmpty()) {
                Integer regionsId = null;

                if (city.equalsIgnoreCase("бургас") || city.equalsIgnoreCase("burgas") || city.equalsIgnoreCase("bourgas")) {
                    Regions[] regions = RestClient.getInstance().getRegions();
                    if (regions != null) {
                        for (Regions regionObj : regions) {
                            if (regionObj.getDescription() != null && regionObj.getDescription().equalsIgnoreCase(regionsPicker.getText().toString())) {
                                regionsId = regionObj.getId();
                                break;
                            }
                        }
                    }
                }

                if (regionsId != null) newRequest.setRegionId(regionsId);
                else txt = regionsPicker.getText().toString() + " " + txt;
            }

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
            if (addressText.getVisibility() == View.VISIBLE) addressText.setError(getString(R.string.required_field));
            else
                Toast.makeText(this, getString(R.string.required_field) + ": " + getString(R.string.address), Toast.LENGTH_SHORT).show();
        }
    }

    @Background
    void sendRequest(NewRequestDetails newRequest) {
        Integer requestId = RestClient.getInstance().sendNewRequest(newRequest);
        if (requestId != null && requestId > 0) {
            TaxiApplication.setLastRequestId(requestId);
            SuccessDialog();
        }
    }

    @Click
    void addressChange() {
        addressText.setText(address.getText());
        address.setText(R.string.address);
        addressText.setVisibility(View.VISIBLE);
        addressChange.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(addressText, InputMethodManager.SHOW_IMPLICIT);
    }

    @Click
    void addressImage() {
        if (addressText.getVisibility() == View.VISIBLE) {
            if (addressText.getText() != null) {
                String txt = addressText.getText().toString();
                if (txt != null && txt.length() > 0 && this.newRequest != null) this.newRequest.setFullAddress(txt);
            }
        }

        Intent intent = new Intent(this, LongPressMapAction_.class);
        if (this.newRequest != null) intent.putExtra("newRequest", this.newRequest);
        startActivityForResult(intent, SHOW_ADDRESS_ON_MAP);
        //LongPressMapAction_.intent(this).startForResult(SHOW_ADDRESS_ON_MAP);
    }

    @UiThread
    void SuccessDialog() {
        reqInfoButtonContainer.setVisibility(View.VISIBLE);
        pbProgress.setVisibility(View.GONE);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.send_request));
        String txt = addressText.getText().toString();
        if (txt.length() == 0) txt = address.getText().toString();
        alertDialogBuilder.setMessage(getString(R.string.request_send_successful, txt));

        alertDialogBuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                RequestsActivity_.intent(NewRequestActivity.this).start();
                finish();
            }
        });

        Dialog successDialog = alertDialogBuilder.create();
        successDialog.show();
    }

    /*@UiThread
    void notSupportedDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Информация");
        alertDialogBuilder.setMessage("Съжаляваме но услугата за момента се предлага за град Бургас. Ще бъдете известени по имейл когато услугата стане достъпна за други градове.");

        alertDialogBuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog successDialog = alertDialogBuilder.create();
        successDialog.show();
    }*/
}