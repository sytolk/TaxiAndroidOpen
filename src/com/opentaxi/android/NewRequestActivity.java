package com.opentaxi.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.opentaxi.android.adapters.GroupsAdapter;
import com.opentaxi.android.adapters.RegionsAdapter;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.android.utils.MyGeocoder;
import com.opentaxi.models.MapRequest;
import com.opentaxi.models.NewRequestDetails;
import com.opentaxi.rest.RestClient;
import com.opentaxi.rest.RestClientBase;
import com.stil.generated.mysql.tables.pojos.*;
import com.taxibulgaria.enums.RegionsType;
import com.taxibulgaria.enums.RequestSource;
import org.androidannotations.annotations.*;

import java.io.IOException;
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

    MapRequest mapRequest;

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

    //LocationInfo latestInfo;

    @AfterViews
    protected void afterActivity() {
        this.mapRequest = null;
        if (cars == null) setTitle(getString(R.string.taxi_request));
        else setTitle(getString(R.string.taxi_request_to_car, cars.getNumber()));

        String[] cities = new String[]{
                "Бургас", "София", "Варна", "Пловдив", "Burgas", "Sofia", "Varna", "Plovdiv", "Несебър", "Nesebar", "Слънчев бряг", "Sunny beach", "Приморско", "Primorsko", "Царево", "Carevo", "Созопол", "Sozopol", "Разград", "Razgrad", "Монтана", "Montana", "Враца", "Vratsa", "Добрич", "Dobrich", "Русе", "Ruse", "Плевен", "Pleven", "Перник", "Pernik", "Пазарджик", "Pazardzhik", "Ловеч", "Lovech", "Хасково", "Haskovo", "Благоевград", "Blagoevgrad", "Габрово", "Gabrovo", "Кърджали", "Kurdzhali", "Кюстендил", "Kyustendil", "Шумен", "Shumen", "Силистра", "Silistra", "Сливен", "Sliven", "Смолян", "Smolyan", "Стара Загора", "Stara Zagora", "Търговище", "Turgovishte", "Велико Търново", "Veliko Turnovo", "Видин", "Vidin", "Ямбол", "Yambol"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, cities);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        citiesPicker.setAdapter(adapter);

        setCities();
        setAddress();
        //showCities("Бургас");
        setPrices();
        setGroups();
        address.setText(R.string.wait_address);

        addressText.setVisibility(View.GONE);
        //latestInfo = new LocationInfo(getBaseContext());
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, resultCode + " " + resultCode);
        if (requestCode == SHOW_ADDRESS_ON_MAP) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    com.stil.generated.mysql.tables.pojos.NewRequest newRequest = (com.stil.generated.mysql.tables.pojos.NewRequest) extras.getSerializable("newRequest");
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
                this.mapRequest = extras.getParcelable("mapRequest");
                if (this.mapRequest != null)
                    showAddress(this.mapRequest.getCity(), this.mapRequest.getRegion(), this.mapRequest.getAddress());
                else Log.e(TAG, "mapRequest=null");
            }
        } else Log.e(TAG, "" + resultCode);
    }

    @Background
    void setCities() {
        if (this.mapRequest == null) {
            Contactaddress contactAddress = RestClient.getInstance().getAddress();
            if (contactAddress != null) {
                showCities(contactAddress);
                if (contactAddress.getCountryinfogeonamesid() != null) {
                    showRegions(RestClient.getInstance().getRegionsByGN(contactAddress.getCountryinfogeonamesid()));
                } else showRegions(RestClient.getInstance().getRegions(RegionsType.BURGAS_STATE.getCode()));
            } else showRegions(RestClient.getInstance().getRegions(RegionsType.BURGAS_STATE.getCode()));
        }
    }

    @UiThread
    void showCities(Contactaddress contactAddress) {
            /*CitiesAdapter[] citiesAdapter = new CitiesAdapter[1];
            citiesAdapter[0] = new CitiesAdapter(supported);
            ArrayAdapter<CitiesAdapter> adapter2 = new ArrayAdapter<CitiesAdapter>(this, R.layout.spinner_layout, citiesAdapter);
            adapter2.setDropDownViewResource(R.layout.spinner_layout);*/
        if (this.mapRequest == null && contactAddress != null && contactAddress.getCity() != null) {
            //Log.d(TAG, "Contactaddress:" + address.getCity() + ":" + address.getCountryinfogeonamesid());
            citiesPicker.setText(contactAddress.getCity());
            address.setFocusable(true);
            address.setFocusableInTouchMode(true);
            address.requestFocus();
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

    /*@Background
    void setRegions(Integer parentId) {
        showRegions(RestClient.getInstance().getRegions(parentId));
    }*/

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

            regionsLayout.setVisibility(View.VISIBLE);
            destLayout.setVisibility(View.GONE);
        } else {
            regionsLayout.setVisibility(View.GONE);
            destLayout.setVisibility(View.VISIBLE);
        }
        //setAddress();
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
            groupsAdapters = new GroupsAdapter[prices.length + 1];
            Groups all_prices = new Groups();
            all_prices.setGroupsId(0);
            all_prices.setDescription(getString(R.string.all_prices));
            groupsAdapters[0] = new GroupsAdapter(all_prices);

            int i = 1;
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
            group.setDescription(getString(R.string.all_prices)); //.no_free_cars));
            groupsAdapters[0] = new GroupsAdapter(group);
            requestSend.setVisibility(View.VISIBLE); //.GONE);
        }

        ArrayAdapter<GroupsAdapter> adapter2 = new ArrayAdapter<GroupsAdapter>(this, R.layout.spinner_layout, groupsAdapters);
        adapter2.setDropDownViewResource(R.layout.spinner_layout);
        pricesPicker.setAdapter(adapter2);
    }

    @Background
    void setAddress() {
        //Log.i(TAG, "setAddress");
        if (this.mapRequest == null) {
            if (AppPreferences.getInstance() != null && AppPreferences.getInstance().getNorth() != null && AppPreferences.getInstance().getEast() != null) {
                Date now = new Date();
                if (AppPreferences.getInstance().getGpsLastTime() > (now.getTime() - 600000)) {  //if last coordinates time is from 5 min interval
                    com.stil.generated.mysql.tables.pojos.NewRequest address = RestClient.getInstance().getAddressByCoordinates(AppPreferences.getInstance().getNorth().floatValue(), AppPreferences.getInstance().getEast().floatValue());
                    if (address != null) {
                        this.mapRequest = new MapRequest();
                        this.mapRequest.setNorth(AppPreferences.getInstance().getNorth());
                        this.mapRequest.setEast(AppPreferences.getInstance().getEast());
                        this.mapRequest.setCity(getString(R.string.burgas));
                        Regions regions = RestClient.getInstance().getRegionById(RegionsType.BURGAS_STATE.getCode(), address.getRegionId());
                        if (regions != null) {
                            showRegions(RestClient.getInstance().getRegions(RegionsType.BURGAS_STATE.getCode()));
                            this.mapRequest.setRegion(regions.getDescription());
                        }
                        this.mapRequest.setAddress(address.getFullAddress());

                        showAddress(this.mapRequest.getCity(), this.mapRequest.getRegion(), this.mapRequest.getAddress());
                        return;
                    } else Log.i(TAG, "address=null or no coordinates");
                } else
                    Log.i(TAG, "GpsLastTime " + AppPreferences.getInstance().getGpsLastTime() + " > " + (now.getTime() - 600000) + " min");

                List<Address> addresses = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                    try {
                        Geocoder geocoder = new Geocoder(this);
                        addresses = geocoder.getFromLocation(AppPreferences.getInstance().getNorth(), AppPreferences.getInstance().getEast(), 1);
                        if (addresses == null || addresses.isEmpty())
                            addresses = MyGeocoder.getFromLocation(AppPreferences.getInstance().getNorth(), AppPreferences.getInstance().getEast(), 1);
                    } catch (IOException e) {
                        addresses = MyGeocoder.getFromLocation(AppPreferences.getInstance().getNorth(), AppPreferences.getInstance().getEast(), 1);
                        Log.e(TAG, "IOException:" + e.getMessage());
                    }
                } else {
                    addresses = MyGeocoder.getFromLocation(AppPreferences.getInstance().getNorth(), AppPreferences.getInstance().getEast(), 1);
                    Log.i(TAG, "Geocoder not present");
                }

                if (addresses != null && !addresses.isEmpty()) {
                    // Get the first address
                    Address address = addresses.get(0);
                    if (address.getLocality() != null) {
                        Log.i(TAG, address.toString());
                        //Address[addressLines=[0:"улица „Елин Пелин“ 6",1:"8142 Chernomorets",2:"Bulgaria"],feature=6,admin=Burgas,sub-admin=Sozopol,locality=Chernomorets,thoroughfare=улица „Елин Пелин“,postalCode=null,countryCode=BG,countryName=Bulgaria,hasLatitude=true,latitude=42.4429078,hasLongitude=true,longitude=27.6423008,phone=null,url=null,extras=null]

                        this.mapRequest = new MapRequest();
                        this.mapRequest.setNorth(address.getLatitude());
                        this.mapRequest.setEast(address.getLongitude());
                        this.mapRequest.setCity(address.getLocality());
                        if (address.getMaxAddressLineIndex() >= 0) {
                            String adr = address.getAddressLine(0);
                            if (adr != null && !adr.equals("Unnamed Rd"))
                                this.mapRequest.setAddress(address.getAddressLine(0));
                        }
                        showAddress(this.mapRequest.getCity(), null, this.mapRequest.getAddress());
                        return;
                    }
                }
            }

            showAddress(null, null, null);
        }
    }

    @UiThread
    void showAddress(String city, String region, String adr) {
        Log.i(TAG, city + " " + region + " " + adr);
        if (city != null) citiesPicker.setText(city);
        if (region != null) {
            regionsPicker.setText(region);
            regionsLayout.setVisibility(View.VISIBLE);
            destLayout.setVisibility(View.GONE);
        } else {
            regionsLayout.setVisibility(View.GONE);
            destLayout.setVisibility(View.VISIBLE);
        }

        if (adr != null && !adr.equals("Unnamed Rd")) {
            address.setText(adr);
            addressChange.setVisibility(View.VISIBLE);
            addressText.setVisibility(View.GONE);
        } else { //unknown address
            address.setText(R.string.address);
            addressText.setVisibility(View.VISIBLE);
        }
    }

    /*@Background
    void showAddressByRequest(MapRequest newRequest) {
        Regions region = RestClient.getInstance().getRegionById(newRequest.getRegionId());
        if (region != null) {
            if (region.getParentId() != null && !region.getParentId().equals(region.getId())) {
                Regions parent = RestClient.getInstance().getRegionById(region.getParentId());
                if (parent != null) {
                    showAddress(parent.getDescription(), region.getDescription(), newRequest.getFullAddress());
                } else showAddress(region.getDescription(), null, newRequest.getFullAddress());
            } else showAddress(region.getDescription(), null, newRequest.getFullAddress());
        } else showAddress(null, null, newRequest.getFullAddress());
    }*/

    /*@UiThread
    void showAddress(com.stil.generated.mysql.tables.pojos.NewRequest adr) {
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
    }*/

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
            if (this.mapRequest != null) {
                newRequest.setNorth(this.mapRequest.getNorth());
                newRequest.setEast(this.mapRequest.getEast());
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
                    Regions[] regions = RestClient.getInstance().getRegions(RegionsType.BURGAS_STATE.getCode()); //todo in background
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
                        CheckBox cb = (CheckBox) findViewById(group.getGroupsId());
                        if (cb.isChecked()) filterGroups.add(group);
                    } else RestClient.getInstance().clearCache(RestClientBase.getVisibleGroupsKey);
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

    @Click
    void addressImage() {
        if (this.mapRequest == null) this.mapRequest = new MapRequest();
        if (addressText.getVisibility() == View.VISIBLE) {
            if (citiesPicker.getText() != null) {
                String txt = citiesPicker.getText().toString();
                if (txt != null && txt.length() > 0) this.mapRequest.setCity(txt);
            }
            if (regionsPicker.getText() != null) {
                String txt = regionsPicker.getText().toString();
                if (txt != null && txt.length() > 0) this.mapRequest.setRegion(txt);
            }
            if (addressText.getText() != null) {
                String txt = addressText.getText().toString();
                if (txt != null && txt.length() > 0) this.mapRequest.setAddress(txt);
            }
        }

        Intent intent = new Intent(this, LongPressMapAction_.class);
        if (this.mapRequest != null) intent.putExtra("mapRequest", this.mapRequest);
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