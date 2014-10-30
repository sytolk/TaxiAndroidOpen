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
import com.opentaxi.android.adapters.GroupsAdapter;
import com.opentaxi.android.adapters.RegionsAdapter;
import com.opentaxi.models.NewCRequest;
import com.opentaxi.models.NewRequestDetails;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Contactaddress;
import com.stil.generated.mysql.tables.pojos.Groups;
import com.stil.generated.mysql.tables.pojos.Regions;
import com.stil.generated.mysql.tables.pojos.RequestsDetails;
import com.taxibulgaria.enums.RegionsType;
import com.taxibulgaria.enums.RequestSource;
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
public class EditRequestActivity extends Activity {

    private static final String TAG = "EditRequestActivity";

    @Extra
    NewCRequest newCRequest;

    @ViewById(R.id.addressImage)
    ToggleButton addressImage;

    @ViewById(R.id.pricesPicker)
    Spinner pricesPicker;

    @ViewById(R.id.citiesPicker)
    AutoCompleteTextView citiesPicker;

    @ViewById(R.id.address)
    TextView address;

    @ViewById(R.id.addressText)
    EditText addressText;

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

    @ViewById
    Button requestSend;

    private static final int SHOW_ADDRESS_ON_MAP = 990;

    @AfterViews
    protected void afterActivity() {

        requestSend.setText(getString(R.string.change_request));
        //showCities("Бургас");

        String[] cities = new String[]{
                "Бургас", "София", "Варна", "Пловдив", "Burgas", "Sofia", "Varna", "Plovdiv", "Несебър", "Nesebar", "Слънчев бряг", "Sunny beach", "Приморско", "Primorsko", "Царево", "Carevo", "Созопол", "Sozopol", "Разград", "Razgrad", "Монтана", "Montana", "Враца", "Vratsa", "Добрич", "Dobrich", "Русе", "Ruse", "Плевен", "Pleven", "Перник", "Pernik", "Пазарджик", "Pazardzhik", "Ловеч", "Lovech", "Хасково", "Haskovo", "Благоевград", "Blagoevgrad", "Габрово", "Gabrovo", "Кърджали", "Kurdzhali", "Кюстендил", "Kyustendil", "Шумен", "Shumen", "Силистра", "Silistra", "Сливен", "Sliven", "Смолян", "Smolyan", "Стара Загора", "Stara Zagora", "Търговище", "Turgovishte", "Велико Търново", "Veliko Turnovo", "Видин", "Vidin", "Ямбол", "Yambol"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, cities);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        citiesPicker.setAdapter(adapter);

        addressImage.setVisibility(View.INVISIBLE);

        setCities();
        setRegions();
        setPrices();
        setGroups();
        //address.setText("Адреса се определя автоматично според координатите ви. Моля изчакайте...");
        //addressText.setVisibility(View.GONE);
    }

    @OnActivityResult(SHOW_ADDRESS_ON_MAP)
    void onResult(int resultCode, Intent data) {
        //Log.i(TAG, "" + resultCode + " " + data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                this.newCRequest = (NewCRequest) extras.getSerializable("newCRequest");
                showAddress();
            }
        } else Log.e(TAG, "" + resultCode);
    }

    @Background
    void setCities() {
        showCities(RestClient.getInstance().getAddress());
    }

    @UiThread
    void showCities(Contactaddress address) {
            /*CitiesAdapter[] citiesAdapter = new CitiesAdapter[1];
            citiesAdapter[0] = new CitiesAdapter(supported);
            ArrayAdapter<CitiesAdapter> adapter2 = new ArrayAdapter<CitiesAdapter>(this, R.layout.spinner_layout, citiesAdapter);
            adapter2.setDropDownViewResource(R.layout.spinner_layout);*/
        if (address != null) citiesPicker.setText(address.getCity());
        //citiesPicker.setOnTouchListener(Spinner_OnTouch);
    }

    /*private View.OnTouchListener Spinner_OnTouch = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                notSupporderDialog();
            }
            return true;
        }
    };*/

    @Background
    void setRegions() {
        showRegions(RestClient.getInstance().getRegions(RegionsType.BURGAS_STATE.getCode()));
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
                if (newCRequest != null) {
                    if (regionObj.getId().equals(newCRequest.getRegionId()))
                        regionsPicker.setText(regionObj.getDescription());
                }
                i++;
            }
            ArrayAdapter<RegionsAdapter> adapter2 = new ArrayAdapter<RegionsAdapter>(this, R.layout.spinner_layout, regionsAdapter);
            adapter2.setDropDownViewResource(R.layout.spinner_layout);
            regionsPicker.setAdapter(adapter2);
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
        GroupsAdapter[] groupsAdapters;
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

            groupsAdapters = new GroupsAdapter[prices.length + 1];
            Groups all_prices = new Groups();
            all_prices.setGroupsId(0);
            all_prices.setDescription(getString(R.string.all_prices));
            groupsAdapters[0] = new GroupsAdapter(all_prices);
            int i = 1;
            int spinnerPosition = 0;
            for (Groups group : prices) {
                groupsAdapters[i] = new GroupsAdapter(group);
                if (chooseGroup != null && chooseGroup.getGroupsId().equals(group.getGroupsId())) spinnerPosition = i;
                i++;
            }
            pricesPicker.setSelection(spinnerPosition);

        } else {
            Log.e(TAG, "prices=null");
            groupsAdapters = new GroupsAdapter[1];
            Groups group = new Groups();
            group.setGroupsId(0);
            group.setDescription(getString(R.string.all_prices)); //.no_free_cars));
            groupsAdapters[0] = new GroupsAdapter(group);
            requestSend.setVisibility(View.VISIBLE); //.GONE);
        }

        ArrayAdapter<GroupsAdapter> adapter2 = new ArrayAdapter<GroupsAdapter>(this, R.layout.spinner_layout, groupsAdapters);
        adapter2.setDropDownViewResource(R.layout.spinner_layout);
        pricesPicker.setAdapter(adapter2);
    }

    void showAddress() {
        address.setText(R.string.address);
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
        String txt = null;
        if (addressText.getVisibility() == View.VISIBLE) txt = addressText.getText().toString();
        else txt = address.getText().toString();

        String city = citiesPicker.getText().toString().trim();
        if (city == null || city.isEmpty()) {
            if (citiesPicker.getVisibility() == View.VISIBLE) citiesPicker.setError(getString(R.string.required_field));
            else
                Toast.makeText(this, getString(R.string.required_field) + ": " + getString(R.string.city), Toast.LENGTH_SHORT).show();
        } else if (txt != null && txt.length() > 1 && newCRequest != null) {
            reqInfoButtonContainer.setVisibility(View.GONE);
            pbProgress.setVisibility(View.VISIBLE);

            NewRequestDetails newRequest = new NewRequestDetails();
            newRequest.setRequestsId(newCRequest.getRequestsId());

            RequestsDetails requestsDetails = new RequestsDetails();
            requestsDetails.setFromCity(citiesPicker.getText().toString());
            newRequest.setDetails(requestsDetails);

            if (regionsPicker.getText() != null && !regionsPicker.getText().toString().isEmpty()) {
                Integer regionsId = null;

                if (city.equalsIgnoreCase("бургас") || city.equalsIgnoreCase("burgas") || city.equalsIgnoreCase("bourgas")) {
                    Regions[] regions = RestClient.getInstance().getRegions(RegionsType.BURGAS_STATE.getCode());
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
            for (Groups group : visibleGroups) {
                CheckBox cb = (CheckBox) findViewById(group.getGroupsId());
                if (cb.isChecked()) filterGroups.add(group);
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
            SuccessDialog();
        } else {
            Log.e(TAG, "sendRequest error");
            ErrorDialog();
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

    /*@Click
    void addressImage() {
        if (addressText.getVisibility() == View.VISIBLE) {
            if (addressText.getText() != null) {
                String txt = addressText.getText().toString();
                if (txt != null && txt.length() > 0 && this.newCRequest != null) this.newCRequest.setFullAddress(txt);
            }
        }

        Intent intent = new Intent(this, LongPressMapEditAction_.class);
        if (this.newCRequest != null) intent.putExtra("newRequest", this.newCRequest);
        startActivityForResult(intent, SHOW_ADDRESS_ON_MAP);
    }*/

    @UiThread
    void SuccessDialog() {
        reqInfoButtonContainer.setVisibility(View.VISIBLE);
        pbProgress.setVisibility(View.GONE);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.changed_request));
        String txt = addressText.getText().toString();
        if (txt.length() == 0) txt = address.getText().toString();
        alertDialogBuilder.setMessage(getString(R.string.request_changed_successful, txt));

        alertDialogBuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                RequestsActivity_.intent(EditRequestActivity.this).start();
                finish();
            }
        });

        Dialog successDialog = alertDialogBuilder.create();
        successDialog.show();
    }

    @UiThread
    void ErrorDialog() {
        reqInfoButtonContainer.setVisibility(View.VISIBLE);
        pbProgress.setVisibility(View.GONE);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.send_request));
        String txt = addressText.getText().toString();
        if (txt.length() == 0) txt = address.getText().toString();
        alertDialogBuilder.setMessage(getString(R.string.request_send_error, txt));

        alertDialogBuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog errorDialog = alertDialogBuilder.create();
        errorDialog.show();
    }
}