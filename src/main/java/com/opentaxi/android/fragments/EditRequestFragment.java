/*
package com.opentaxi.android.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.opentaxi.android.R;
import com.opentaxi.android.adapters.RegionsAdapter;
import com.opentaxi.android.adapters.TaxiClientPricesAdapter;
import com.opentaxi.models.NewCRequestDetails;
import com.opentaxi.models.NewRequestDetails;
import com.opentaxi.rest.RestClient;
import com.opentaxi.rest.RestClientBase;
import com.stil.generated.mysql.tables.pojos.*;
import com.taxibulgaria.enums.RegionsType;
import com.taxibulgaria.enums.RequestSource;
import org.androidannotations.annotations.*;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

*/
/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/25/13
 * Time: 10:03 AM
 * To change this template use File | Settings | File Templates.
 *//*

@EFragment(R.layout.new_request)
public class EditRequestFragment extends Fragment {

    private static final String TAG = "EditRequestFragment";

    //@Extra
    NewCRequestDetails newCRequest;

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

    @ViewById(R.id.destLayout)
    LinearLayout destLayout;

    @ViewById(R.id.priceLayout)
    LinearLayout priceLayout;

    //private static final int SHOW_ADDRESS_ON_MAP = 990;

    Activity mActivity;

    OnCommandListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            try {
                mListener = (OnCommandListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnRequestEventsListener");
            }
            mActivity = (Activity) context;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            newCRequest = (NewCRequestDetails) bundle.getSerializable("newCRequest");
        }
    }

    @AfterViews
    protected void afterActivity() {

        requestSend.setText(getString(R.string.change_request));
        //showCities("Бургас");

        String[] cities = new String[]{
                "Бургас", "София", "Варна", "Пловдив", "Burgas", "Sofia", "Varna", "Plovdiv", "Несебър", "Nesebar", "Слънчев бряг", "Sunny beach", "Приморско", "Primorsko", "Царево", "Carevo", "Созопол", "Sozopol", "Разград", "Razgrad", "Монтана", "Montana", "Враца", "Vratsa", "Добрич", "Dobrich", "Русе", "Ruse", "Плевен", "Pleven", "Перник", "Pernik", "Пазарджик", "Pazardzhik", "Ловеч", "Lovech", "Хасково", "Haskovo", "Благоевград", "Blagoevgrad", "Габрово", "Gabrovo", "Кърджали", "Kurdzhali", "Кюстендил", "Kyustendil", "Шумен", "Shumen", "Силистра", "Silistra", "Сливен", "Sliven", "Смолян", "Smolyan", "Стара Загора", "Stara Zagora", "Търговище", "Turgovishte", "Велико Търново", "Veliko Turnovo", "Видин", "Vidin", "Ямбол", "Yambol"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, R.layout.spinner_layout, cities);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        citiesPicker.setAdapter(adapter);

        addressImage.setVisibility(View.INVISIBLE);

        if (newCRequest != null) {
            if (newCRequest.getDetails() != null) {
                if (newCRequest.getDetails().getFromCity() != null)
                    citiesPicker.setText(newCRequest.getDetails().getFromCity());
                else setCities();

                if (newCRequest.getDetails().getFromGnId() != null) {
                    setRegionsByGN(newCRequest.getDetails().getFromGnId());
                } else setRegions();

                if (newCRequest.getDetails().getDestination() != null) {
                    destination.setText(newCRequest.getDetails().getDestination());
                    destLayout.setVisibility(View.VISIBLE);
                }

            } else {
                setCities();
                setRegions();
            }
            showAddress(newCRequest.getFullAddress());
        } else {
            setCities();
            showAddress("");
            setRegions();
        }
        //setPrices();
        setGroups();
        //address.setText("Адреса се определя автоматично според координатите ви. Моля изчакайте...");
        //addressText.setVisibility(View.GONE);
    }

    */
/*@OnActivityResult(SHOW_ADDRESS_ON_MAP)
    void onResult(int resultCode, Intent data) {
        //Log.i(TAG, "" + resultCode + " " + data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                this.newCRequest = (NewCRequest) extras.getSerializable("newCRequest");
                showAddress();
            }
        } else Log.e(TAG, "" + resultCode);
    }*//*


    @Background
    void setCities() {
        showCities(RestClient.getInstance().getAddress());
    }

    @UiThread
    void showCities(Contactaddress address) {
            */
/*CitiesAdapter[] citiesAdapter = new CitiesAdapter[1];
            citiesAdapter[0] = new CitiesAdapter(supported);
            ArrayAdapter<CitiesAdapter> adapter2 = new ArrayAdapter<CitiesAdapter>(this, R.layout.spinner_layout, citiesAdapter);
            adapter2.setDropDownViewResource(R.layout.spinner_layout);*//*

        if (address != null) {
            citiesPicker.setText(address.getCity());
            setPrices(address.getCity());
        }
        //citiesPicker.setOnTouchListener(Spinner_OnTouch);
    }

    */
/*private View.OnTouchListener Spinner_OnTouch = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                notSupporderDialog();
            }
            return true;
        }
    };*//*


    @Background
    void setRegions() {
        showRegions(RestClient.getInstance().getRegions(RegionsType.BURGAS_STATE.getCode()));
    }

    @Background
    void setRegionsByGN(Integer gnId) {
        showRegions(RestClient.getInstance().getRegionsByGN(gnId));
    }

    //todo @FocusChange on city getRegionsByCityName

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
            ArrayAdapter<RegionsAdapter> adapter2 = new ArrayAdapter<RegionsAdapter>(mActivity, R.layout.spinner_layout, regionsAdapter);
            adapter2.setDropDownViewResource(R.layout.spinner_layout);
            regionsPicker.setAdapter(adapter2);
        }
        //showAddress();
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
                    CheckBox cb = new CheckBox(mActivity);
                    cb.setText(group.getDescription());
                    cb.setId(group.getGroupsId());
                    if (groupsMap != null && groupsMap.containsKey(group.getName())) cb.setChecked(true);
                    llFilters.addView(cb);
                }
            }
        }
    }

    @AfterTextChange(R.id.citiesPicker)
    void afterTextChangedOncitiesPicker(Editable text, TextView hello) {
        setPrices(text.toString());
    }

    @Background
    void setPrices(String city) {
        //Log.i(TAG, "getPrices");
        if (city != null) showPrices(RestClient.getInstance().getPrices(city));
    }

    @UiThread
    void showPrices(TaxiClientPrices[] prices) {
        TaxiClientPricesAdapter[] pricesAdapters;
        if (prices != null && prices.length > 0) {
            priceLayout.setVisibility(View.VISIBLE);
            pricesAdapters = new TaxiClientPricesAdapter[prices.length + 1];

            TaxiClientPrices all_prices = new TaxiClientPrices();
            all_prices.setGroupsId(0);
            all_prices.setShortname(getString(R.string.all_prices));
            pricesAdapters[0] = new TaxiClientPricesAdapter(all_prices);

            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(0);
            df.setGroupingUsed(false);

            int i = 1;
            for (TaxiClientPrices price : prices) {
                StringBuilder priceString = new StringBuilder();
                if (price.getShortname() != null) priceString.append(price.getShortname());
                if (price.getDayPrice() != null)
                    priceString.append(" ").append(getString(R.string.day_price)).append(":").append(df.format(price.getDayPrice().setScale(2, RoundingMode.HALF_UP)));
                if (price.getNightPrice() != null)
                    priceString.append(" ").append(getString(R.string.night_price)).append(":").append(df.format(price.getNightPrice().setScale(2, RoundingMode.HALF_UP)));
                if (price.getStartPrice() != null)
                    priceString.append(" ").append(getString(R.string.start_price)).append(":").append(df.format(price.getStartPrice().setScale(2, RoundingMode.HALF_UP)));
                if (price.getStayPrice() != null)
                    priceString.append(" ").append(getString(R.string.stay_price)).append(":").append(df.format(price.getStayPrice().setScale(2, RoundingMode.HALF_UP)));
                price.setShortname(priceString.toString());
                pricesAdapters[i] = new TaxiClientPricesAdapter(price);
                i++;
            }
        } else {
            priceLayout.setVisibility(View.GONE);
            Log.e(TAG, "prices=null");
            pricesAdapters = new TaxiClientPricesAdapter[1];

            TaxiClientPrices all_prices = new TaxiClientPrices();
            all_prices.setGroupsId(0);
            all_prices.setShortname(getString(R.string.all_prices));
            pricesAdapters[0] = new TaxiClientPricesAdapter(all_prices);
        }

        ArrayAdapter<TaxiClientPricesAdapter> adapter2 = new ArrayAdapter<TaxiClientPricesAdapter>(mActivity, R.layout.spinner_layout, pricesAdapters);
        adapter2.setDropDownViewResource(R.layout.spinner_layout);
        pricesPicker.setAdapter(adapter2);
    }

    void showAddress(String adr) {
        address.setText(R.string.address);
        addressText.setText(adr);
        addressText.setVisibility(View.VISIBLE);
    }

    */
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
    }*//*


    */
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
    }*//*


    */
/*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
           *//*
*/
/* if (requestCode == REJECTION) {
                finish();
            }*//*
*/
/*
        }
    }*//*


    */
/*@Override
    public void onPause() {
        super.onPause();
        finish();
    }*//*


    */
/*@Override
    public void finish() {
        if (getParent() == null) {
            setResult(Activity.RESULT_OK);
        } else {
            getParent().setResult(Activity.RESULT_OK);
        }
        super.finish();
    }*//*


    @Click
    void requestSend() {
        String txt = null;
        if (addressText.getVisibility() == View.VISIBLE) txt = addressText.getText().toString();
        else txt = address.getText().toString();

        String city = citiesPicker.getText().toString().trim();
        if (city == null || city.isEmpty()) {
            if (citiesPicker.getVisibility() == View.VISIBLE) citiesPicker.setError(getString(R.string.required_field));
            else
                Toast.makeText(mActivity, getString(R.string.required_field) + ": " + getString(R.string.city), Toast.LENGTH_SHORT).show();
        } else if (txt != null && txt.length() > 1 && newCRequest != null) {
            reqInfoButtonContainer.setVisibility(View.GONE);
            pbProgress.setVisibility(View.VISIBLE);

            NewRequestDetails newRequest = new NewRequestDetails();
            newRequest.setRequestsId(newCRequest.getRequestsId());

            RequestsDetails requestsDetails = new RequestsDetails();
            requestsDetails.setFromCity(citiesPicker.getText().toString());

            if (destination.getText() != null && !destination.getText().toString().isEmpty())
                requestsDetails.setDestination(destination.getText().toString());
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
            if (visibleGroups != null) {
                for (Groups group : visibleGroups) {
                    if (group != null && group.getGroupsId() != null) {
                        CheckBox cb = (CheckBox) mActivity.findViewById(group.getGroupsId());
                        if (cb.isChecked()) filterGroups.add(group);
                    } else RestClient.getInstance().clearCache(RestClientBase.getVisibleGroupsKey);
                }
            }
            TaxiClientPricesAdapter priceAdapter = (TaxiClientPricesAdapter) pricesPicker.getSelectedItem();
            if (priceAdapter != null) {
                Groups priceGroup = new Groups();
                priceGroup.setGroupsId(priceAdapter.getTaxiClientPrices().getGroupsId());
                filterGroups.add(priceGroup);
            }

            newRequest.setRequestGroups(filterGroups);
            newRequest.setSource(RequestSource.ANDROID.getCode());

            sendRequest(newRequest);
        } else {
            if (addressText.getVisibility() == View.VISIBLE) addressText.setError(getString(R.string.required_field));
            else
                Toast.makeText(mActivity, getString(R.string.required_field) + ": " + getString(R.string.address), Toast.LENGTH_SHORT).show();
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
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(addressText, InputMethodManager.SHOW_IMPLICIT);
    }

    */
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
    }*//*


    @UiThread
    void SuccessDialog() {
        reqInfoButtonContainer.setVisibility(View.VISIBLE);
        pbProgress.setVisibility(View.GONE);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        alertDialogBuilder.setTitle(getString(R.string.changed_request));
        String txt = addressText.getText().toString();
        if (txt.length() == 0) txt = address.getText().toString();
        alertDialogBuilder.setMessage(getString(R.string.request_changed_successful, txt));

        alertDialogBuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                //RequestsActivity_.intent(EditRequestFragment.this).start();
                if (mListener != null) mListener.startRequests(false);
                //finish();
            }
        });

        Dialog successDialog = alertDialogBuilder.create();
        successDialog.show();
    }

    @UiThread
    void ErrorDialog() {
        reqInfoButtonContainer.setVisibility(View.VISIBLE);
        pbProgress.setVisibility(View.GONE);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
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
}*/
