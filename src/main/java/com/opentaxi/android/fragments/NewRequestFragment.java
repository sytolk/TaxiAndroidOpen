package com.opentaxi.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.opentaxi.android.LongPressMapAction_;
import com.opentaxi.android.R;
import com.opentaxi.android.TaxiApplication;
import com.opentaxi.android.adapters.CitiesAdapter;
import com.opentaxi.android.adapters.RegionsAdapter;
import com.opentaxi.android.adapters.TaxiClientPricesAdapter;
import com.opentaxi.android.utils.MyGeocoder;
import com.opentaxi.models.MapRequest;
import com.opentaxi.rest.RestClient;
import com.opentaxi.rest.RestClientBase;
import com.stil.generated.mysql.tables.pojos.*;
import com.taxibulgaria.enums.RegionsType;
import com.taxibulgaria.enums.RequestSource;
import com.taxibulgaria.enums.UsersGroupEnum;
import com.taxibulgaria.rest.models.NewCRequestDetails;
import com.taxibulgaria.rest.models.NewRequestDetails;
import de.greenrobot.event.EventBus;
import org.androidannotations.annotations.*;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/25/13
 * Time: 10:03 AM
 * To change this template use File | Settings | File Templates.
 */
@EFragment(R.layout.new_request)
public class NewRequestFragment extends BaseFragment {

    private static final String TAG = "NewRequestFragment";
    private static final int SHOW_ADDRESS_ON_MAP = 999;

    //@Extra
    Cars cars;

    NewCRequestDetails newCRequest;

    MapRequest mapRequest;

    @ViewById(R.id.pricesPicker)
    Spinner pricesPicker;

    /*@ViewById(R.id.address)
    TextView address;
*/
    @ViewById(R.id.addressText)
    EditText addressText;

    @ViewById(R.id.citiesPicker)
    AutoCompleteTextView citiesPicker;

    /*@ViewById(R.id.region)
    TextView region;*/

    @ViewById(R.id.regionsPicker)
    AutoCompleteTextView regionsPicker;

    @ViewById(R.id.destination)
    AutoCompleteTextView destination;

    /*@ViewById(R.id.addressChange)
    Button addressChange;*/

    @ViewById(R.id.llFilters)
    LinearLayout llFilters;

    /*@ViewById(R.id.reqInfoButtonContainer)
    LinearLayout reqInfoButtonContainer;*/

    @ViewById(R.id.pbProgress)
    ProgressBar pbProgress;

    @ViewById(R.id.requestSend)
    Button requestSend;

    @ViewById(R.id.personsNumber)
    EditText personsNumber;

    /*@ViewById(R.id.destLayout)
    LinearLayout destLayout;*/

    /*@ViewById(R.id.priceLayout)
    LinearLayout priceLayout;*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            cars = (Cars) bundle.getSerializable("cars");
            newCRequest = (NewCRequestDetails) bundle.getSerializable("newCRequest");
            mapRequest = bundle.getParcelable("mapRequest");
        }
        /*if (mListener != null && mListener.playServicesConnected()) {

            ReactiveLocationProvider locationProvider = new ReactiveLocationProvider(mActivity.getApplicationContext());

            lastKnownLocationObservable = locationProvider.getLastKnownLocation(); //todo .switchIfEmpty(locationProvider.getUpdatedLocation(req));  https://github.com/mcharmas/Android-ReactiveLocation/issues/65
        }*/
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().registerSticky(this);
        /*if (lastKnownLocationObservable != null) {
            lastKnownLocationSubscription = lastKnownLocationObservable
                    .subscribe(new Action1<Location>() {
                        @Override
                        public void call(Location location) {
                            setAddress(location);
                        }
                    }, new ErrorHandler());
            startAddressTimer();
        } else setAddress(null);*/
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
        /*if (lastKnownLocationSubscription != null)
            lastKnownLocationSubscription.unsubscribe();*/
    }

    /**
     * greenEvent
     *
     * @param location
     */
    public void onEvent(Location location) {
        //Log.i(TAG, "Location:" + location);
        setAddress(location);
        //if (location != null) BackgroundExecutor.cancelAll("addressTimer", true);
    }

    /*@Background(delay = 2000, id = "addressTimer")
    void addressTimer() {
        startAddressTimer();
    }

    @UiThread
    void startAddressTimer() {
        if (addressText != null && addressText.getText() != null && addressText.getHint().toString().equals(mActivity.getString(R.string.wait_address))) {
            setAddress(null);
        }
    }*/

    @AfterViews
    protected void afterActivity() {
        //this.mapRequest = null;
        if (mListener != null) {
            if (cars == null) mListener.setBarTitle(mActivity.getString(R.string.taxi_request));
            else mListener.setBarTitle(mActivity.getString(R.string.taxi_request_to_car, cars.getNumber()));
        }

        if (newCRequest != null) {
            if (newCRequest.getRequestsId() != null) requestSend.setText(R.string.change_request);

            if (newCRequest.getDetails() != null) {
                if (newCRequest.getDetails().getFromCity() != null)
                    citiesPicker.setText(newCRequest.getDetails().getFromCity());

                setCitiesRegions(newCRequest.getDetails().getFromGnId(), newCRequest.getRegionId());

                if (newCRequest.getDetails().getDestination() != null) {
                    destination.setText(newCRequest.getDetails().getDestination());
                    destination.setVisibility(View.VISIBLE);
                }

            } else {
                setCitiesRegions(null, newCRequest.getRegionId());
            }
            addressText.setText(newCRequest.getFullAddress());
        } else if (this.mapRequest != null) {
            showAddress(this.mapRequest.getCity(), this.mapRequest.getRegion(), this.mapRequest.getAddress());
        } else {
            setCitiesRegions(null, null);
        }

        //setAddress();
        //showCities("Бургас");
        //setPrices();
        setGroups();

        if (mListener != null) mListener.fabVisible(false);
    }

    @Background
    void setRegionsByGN(Integer gnId, Integer selRegionId) {
        showRegions(RestClient.getInstance().getRegionsByGN(gnId), selRegionId);
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

    /*@OnActivityResult(SHOW_ADDRESS_ON_MAP)
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
    }*/

    @Background
    void setCitiesRegions(Integer selCityGNId, Integer selRegionId) {
        setCitiesAdapter(RestClient.getInstance().getAdmin1Cities(), false);
        //if (this.mapRequest == null) {

        if (selCityGNId != null) {
            showRegions(RestClient.getInstance().getRegionsByGN(selCityGNId), selRegionId);
        } else { //default city
            Contactaddress contactAddress = RestClient.getInstance().getAddress();
            if (contactAddress != null) {
                showCities(contactAddress.getCity());
                if (contactAddress.getCountryinfogeonamesid() != null) {
                    showRegions(RestClient.getInstance().getRegionsByGN(contactAddress.getCountryinfogeonamesid()), selRegionId);
                } else
                    showRegions(RestClient.getInstance().getRegions(), selRegionId);//RegionsType.BURGAS_STATE.getCode()));
            }
        }
        //}
    }

    private boolean isAscii = false;

    @UiThread
    void setCitiesAdapter(GeonameAdmin1[] admin1Cities, boolean isAscii) {
        if (admin1Cities != null && citiesPicker != null) {
            if (citiesPicker.getAdapter() == null || this.isAscii != isAscii) {
                this.isAscii = isAscii;
        /*String[] cities = new String[]{
                "Бургас", "София", "Варна", "Пловдив", "Несебър", "Слънчев бряг", "Приморско", "Царево", "Созопол", "Разград", "Монтана", "Враца", "Добрич", "Русе", "Плевен", "Перник", "Пазарджик", "Ловеч", "Хасково", "Благоевград", "Габрово", "Кърджали", "Кюстендил", "Шумен", "Силистра", "Сливен", "Смолян", "Стара Загора", "Търговище", "Велико Търново", "Видин", "Ямбол",
                "Burgas", "Sofia", "Varna", "Plovdiv", "Nesebar", "Sunny beach", "Primorsko", "Carevo", "Sozopol", "Razgrad", "Montana", "Vratsa", "Dobrich", "Ruse", "Pleven", "Pernik", "Pazardzhik", "Lovech", "Haskovo", "Blagoevgrad", "Gabrovo", "Kurdzhali", "Kyustendil", "Shumen", "Silistra", "Sliven", "Smolyan", "Stara Zagora", "Turgovishte", "Veliko Turnovo", "Vidin", "Yambol"
        };*/
                CitiesAdapter[] citiesAdapters = new CitiesAdapter[admin1Cities.length];
                int i = 0;
                for (GeonameAdmin1 geonameAdmin1 : admin1Cities) {
                    citiesAdapters[i] = new CitiesAdapter(geonameAdmin1, isAscii);
                    i++;
                }
                ArrayAdapter<CitiesAdapter> adapter = new ArrayAdapter<>(mActivity, R.layout.spinner_layout, citiesAdapters); //android.R.layout.simple_dropdown_item_1line
                citiesPicker.setAdapter(adapter);
            }
        }
    }

    @Background
    void setRegions(Integer selRegionId) {
        showRegions(RestClient.getInstance().getRegions(), selRegionId);
    }

    @UiThread
    void showCities(String city) {
        if (city != null && citiesPicker != null) {
            if (citiesPicker.getText().length() == 0) citiesPicker.setText(city);
        }
        if (addressText != null) {
            addressText.setFocusable(true);
            addressText.setFocusableInTouchMode(true);
            addressText.requestFocus();
        }
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
    void showRegions(Regions[] regions, Integer selRegionId) {
        //Log.i(TAG, "showRegions");
        if (regionsPicker != null) {
            if (regions != null && regions.length > 0) {
                RegionsAdapter[] regionsAdapter = new RegionsAdapter[regions.length];// + 1];
            /*Regions emptyRegion = new Regions();
            emptyRegion.setId(0);
            emptyRegion.setDescription("");
            regionsAdapter[0] = new RegionsAdapter(emptyRegion);*/
                boolean cityChanged = true;
                String selRegion = "";
                int i = 0;
                for (Regions regionObj : regions) {
                    regionsAdapter[i] = new RegionsAdapter(regionObj);
                    i++;
                    if (regionsPicker.getText() != null) {
                        if (regionObj.getDescription().equals(regionsPicker.getText().toString())) cityChanged = false;
                    }
                    if (regionObj.getId().equals(selRegionId)) selRegion = regionObj.getDescription();
                }
                if (cityChanged) regionsPicker.setText(selRegion);
                ArrayAdapter<RegionsAdapter> adapter = new ArrayAdapter<>(mActivity, R.layout.spinner_layout, regionsAdapter); //android.R.layout.simple_dropdown_item_1line
                regionsPicker.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                //adapter2.setDropDownViewResource(R.layout.spinner_layout);

                regionsPicker.setVisibility(View.VISIBLE);

                //destination.setVisibility(View.GONE);
            } else {
                regionsPicker.setVisibility(View.GONE);
                //destination.setVisibility(View.VISIBLE);
            }
        }
        //setAddress();
    }

    @FocusChange(R.id.citiesPicker)
    void citiesPicker(View view, boolean hasFocus) {
        if (isVisible() && citiesPicker != null) {
            if (hasFocus) citiesPicker.showDropDown();
            //else cityChanged(citiesPicker.getText().toString());
        }
    }

    @Click(R.id.citiesPicker)
    void citiesPickerClick() {
        if (isVisible() && citiesPicker != null)
            citiesPicker.showDropDown();
    }

    @FocusChange(R.id.regionsPicker)
    void regionsPicker(View view, boolean hasFocus) {
        Log.i(TAG, "FocusChange regionsPicker:" + hasFocus);
        if (hasFocus && isVisible() && regionsPicker != null)
            regionsPicker.showDropDown();
    }

    @Click(R.id.regionsPicker)
    void regionsPickerClick() {
        if (isVisible() && regionsPicker != null)
            regionsPicker.showDropDown();
    }

    @Background
    void setGroups() {
        showGroups(RestClient.getInstance().getClientVisibleGroups());
    }

    @UiThread
    void showGroups(Groups[] groups) {
        if (groups != null) {
            Map<String, List<Groups>> groupsMap = null;
            if (newCRequest != null) {
                groupsMap = newCRequest.getRequestGroups();
                //for (String key : groupsMap.keySet()) Log.i(TAG, "key:" + key);
            }

            for (Groups group : groups) {
                //Log.i(TAG, "showGroups:" + group.getGroupsId() + " " + group.getDescription());
                if (group.getGroupsId() != null && group.getDescription() != null) {
                    CheckBox cb = new CheckBox(mActivity);
                    int groupID = mActivity.getResources().getIdentifier(group.getName(), "string", mActivity.getPackageName());
                    if (groupID > 0) { //localized string
                        cb.setText(mActivity.getString(groupID));
                    } else cb.setText(group.getDescription());

                    cb.setId(group.getGroupsId());
                    if (groupsMap != null && containsGroup(groupsMap, group.getGroupsId())) {
                        cb.setChecked(true);
                        //Log.i(TAG, "checked:" + group.getName());
                    } //else Log.i(TAG, "groupsMap:" + groupsMap + " not contains:" + group.getName());
                    if (UsersGroupEnum.SHARED_RIDE.getCode().equals(group.getGroupsId())) {
                        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (personsNumber != null)
                                    if (isChecked) {
                                        personsNumber.setVisibility(View.VISIBLE);
                                    } else {
                                        personsNumber.setVisibility(View.GONE);
                                    }
                            }
                        });
                    }
                    if (llFilters != null)
                        llFilters.addView(cb);
                }
            }
        }
    }

    private boolean containsGroup(Map<String, List<Groups>> groupsMap, Integer groupsId) {
        for (List<Groups> groups : groupsMap.values()) {
            if (groups != null) {
                for (Groups group : groups) {
                    if (group.getGroupsId().equals(groupsId)) return true;
                }
            }
        }
        return false;
    }

    @AfterTextChange(R.id.citiesPicker)
    void afterTextChangedOnCitiesPicker(Editable text, TextView city) {
        cityChanged(text.toString());
        //setPrices(text.toString());
    }

    @Background
    void cityChanged(String city) {
        Log.i(TAG, "cityChanged:" + city);
        if (city != null && city.length() > 0) {
            GeonameAdmin1[] admin1Cities = RestClient.getInstance().getAdmin1Cities();
            if (admin1Cities != null) {
                for (GeonameAdmin1 geonameAdmin1 : admin1Cities) {
                    if (geonameAdmin1.getName().toLowerCase(Locale.getDefault()).contains(city.toLowerCase(Locale.getDefault()))) {
                        showRegions(RestClient.getInstance().getRegionsByGN(geonameAdmin1.getGeonameid()), null);
                        showPrices(RestClient.getInstance().getPrices(geonameAdmin1.getGeonameid()));
                        setCitiesAdapter(admin1Cities, false);
                        break;
                    } else if (geonameAdmin1.getNameascii().toLowerCase(Locale.getDefault()).contains(city.toLowerCase(Locale.getDefault()))) {
                        showRegions(RestClient.getInstance().getRegionsByGN(geonameAdmin1.getGeonameid()), null);
                        showPrices(RestClient.getInstance().getPrices(geonameAdmin1.getGeonameid()));
                        setCitiesAdapter(admin1Cities, true);
                        break;
                    }
                }
            }
        }
    }

    /*@Background
    void setPrices(String city) {
        Log.i(TAG, "getPrices city:" + city);
        if (city != null) showPrices(RestClient.getInstance().getPrices(city));
    }*/

    @UiThread
    void showPrices(TaxiClientPrices[] prices) {
        if (pricesPicker != null) {
            TaxiClientPricesAdapter[] pricesAdapters;
            if (prices != null && prices.length > 0) {
                pricesPicker.setVisibility(View.VISIBLE);
                pricesAdapters = new TaxiClientPricesAdapter[prices.length + 1];

                TaxiClientPrices all_prices = new TaxiClientPrices();
                all_prices.setGroupsId(0);
                all_prices.setShortname(mActivity.getString(R.string.all_prices));
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
                        priceString.append(" ").append(mActivity.getString(R.string.day_price)).append(":").append(df.format(price.getDayPrice().setScale(2, RoundingMode.HALF_UP)));
                    if (price.getNightPrice() != null)
                        priceString.append(" ").append(mActivity.getString(R.string.night_price)).append(":").append(df.format(price.getNightPrice().setScale(2, RoundingMode.HALF_UP)));
                    if (price.getStartPrice() != null)
                        priceString.append(" ").append(mActivity.getString(R.string.start_price)).append(":").append(df.format(price.getStartPrice().setScale(2, RoundingMode.HALF_UP)));
                    if (price.getStayPrice() != null)
                        priceString.append(" ").append(mActivity.getString(R.string.stay_price)).append(":").append(df.format(price.getStayPrice().setScale(2, RoundingMode.HALF_UP)));
                    price.setShortname(priceString.toString());
                    pricesAdapters[i] = new TaxiClientPricesAdapter(price);
                    i++;
                }
            } else {
                pricesPicker.setVisibility(View.GONE);
                Log.e(TAG, "prices=null");
                pricesAdapters = new TaxiClientPricesAdapter[1];

                TaxiClientPrices all_prices = new TaxiClientPrices();
                all_prices.setGroupsId(0);
                all_prices.setShortname(mActivity.getString(R.string.all_prices));
                pricesAdapters[0] = new TaxiClientPricesAdapter(all_prices);
            }

            ArrayAdapter<TaxiClientPricesAdapter> adapter2 = new ArrayAdapter<TaxiClientPricesAdapter>(mActivity, R.layout.spinner_layout, pricesAdapters);
            adapter2.setDropDownViewResource(R.layout.multiline_spinner_layout);
            pricesPicker.setAdapter(adapter2);
        }
    }

    @Background
    void setAddress(Location location) {
        //Log.i(TAG, "setAddress");
        if (this.mapRequest == null) { //to change address only first time
            if (location != null) {
                // Date now = new Date();
                //if (AppPreferences.getInstance().getGpsLastTime() > (now.getTime() - 600000)) {  //if last coordinates time is from 5 min interval
                com.stil.generated.mysql.tables.pojos.NewRequest newRequest = RestClient.getInstance().getAddressByCoordinates(location.getLatitude(), location.getLongitude());
                if (newRequest != null) { //todo wrong for varna addresses
                    this.mapRequest = new MapRequest();
                    this.mapRequest.setNorth(location.getLatitude());
                    this.mapRequest.setEast(location.getLongitude());
                    this.mapRequest.setCity(mActivity.getString(R.string.burgas));
                    Regions regions = RestClient.getInstance().getRegionById(RegionsType.BURGAS_STATE.getCode(), newRequest.getRegionId());
                    if (regions != null) {
                        showRegions(RestClient.getInstance().getRegions(RegionsType.BURGAS_STATE.getCode()), null);
                        this.mapRequest.setRegion(regions.getDescription());
                    }
                    this.mapRequest.setAddress(newRequest.getFullAddress());

                    showAddress(this.mapRequest.getCity(), this.mapRequest.getRegion(), this.mapRequest.getAddress());
                    return;
                } else Log.i(TAG, "address=null or no coordinates");
                //} else Log.i(TAG, "GpsLastTime " + AppPreferences.getInstance().getGpsLastTime() + " > " + (now.getTime() - 600000) + " min");

                List<Address> addresses = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                    try {
                        Geocoder geocoder = new Geocoder(mActivity);
                        addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addresses == null || addresses.isEmpty())
                            addresses = MyGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    } catch (IOException e) {
                        addresses = MyGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        Log.e(TAG, "IOException:" + e.getMessage());
                    }
                } else {
                    addresses = MyGeocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
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
        //Log.i(TAG, city + " " + region + " " + adr);
        if (city != null && citiesPicker != null) citiesPicker.setText(city);
        if (region != null && regionsPicker != null) {
            regionsPicker.setText(region);
            regionsPicker.setVisibility(View.VISIBLE);
            //destination.setVisibility(View.GONE);
        } /*else {
            //regionsPicker.setVisibility(View.GONE);
            //destination.setVisibility(View.VISIBLE);
        }*/
        if (addressText != null) {
            if (adr != null && !adr.equals("Unnamed Rd")) {
                addressText.setText(adr);
                // addressText.setVisibility(View.GONE);
            } else { //unknown address
                addressText.setHint(""); //R.string.address);
                //addressText.setVisibility(View.VISIBLE);
            }
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
    public void finish() {
        if (getParent() == null) {
            setResult(Activity.RESULT_OK);
        } else {
            getParent().setResult(Activity.RESULT_OK);
        }
        super.finish();
    }*/

    @Override
    public void onPause() {
        super.onPause();
        if (mListener != null) mListener.fabVisible(true);
    }

    @Click
    void history() {
        if (mListener != null) mListener.startRequests(true);
    }

    @Click
    void requestSend() {
        String txt = addressText.getText().toString();
        /*if (addressText.getVisibility() == View.VISIBLE) txt = addressText.getText().toString();
        else txt = addressText.getText().toString();*/

        String city = citiesPicker.getText().toString().trim();
        if (city.isEmpty()) {
            if (citiesPicker.getVisibility() == View.VISIBLE)
                citiesPicker.setError(mActivity.getString(R.string.required_field));
            else
                Toast.makeText(mActivity, mActivity.getString(R.string.required_field) + ": " + mActivity.getString(R.string.city), Toast.LENGTH_SHORT).show();
        } else if (txt.length() > 1 && !txt.equals(mActivity.getString(R.string.wait_address))) {
            //reqInfoButtonContainer.setVisibility(View.GONE);
            pbProgress.setVisibility(View.VISIBLE);

            NewRequestDetails newRequest = new NewRequestDetails();
            if (newCRequest != null) newRequest.setRequestsId(newCRequest.getRequestsId()); //edit

            if (this.mapRequest != null) {
                newRequest.setNorth(this.mapRequest.getNorth());
                newRequest.setEast(this.mapRequest.getEast());
            }
            if (cars != null) newRequest.setCarId(cars.getId());
            RequestsDetails requestsDetails = new RequestsDetails();

            requestsDetails.setFromCity(city);
            if (destination.getText() != null && !destination.getText().toString().isEmpty())
                requestsDetails.setDestination(destination.getText().toString());

            if (personsNumber != null && personsNumber.getText() != null) {
                Byte persons = 1;
                try {
                    persons = Byte.parseByte(personsNumber.getText().toString());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "NumberFormatException persons:", e);
                }
                if (persons <= 0) persons = 1;
                requestsDetails.setPassengers(persons);
            }

            newRequest.setDetails(requestsDetails);

            //RegionsAdapter regionsAdapter = (RegionsAdapter) regionsPicker.getSelectedItem();
            if (regionsPicker.getText() != null && !regionsPicker.getText().toString().isEmpty()) {
                Integer regionsId = null;

                //if (city.equalsIgnoreCase("бургас") || city.equalsIgnoreCase("burgas") || city.equalsIgnoreCase("bourgas")) {
                boolean foundRegion = false;
                Regions[] regions = RestClient.getInstance().getRegions();//RegionsType.BURGAS_STATE.getCode()); //todo in background
                if (regions != null) {
                    for (Regions regionObj : regions) {
                        if (regionObj.getDescription() != null && regionObj.getDescription().equalsIgnoreCase(regionsPicker.getText().toString().trim())) {
                            regionsId = regionObj.getId();
                            foundRegion = true;
                            break;
                        } else if (regionObj.getParentId() != null) regionsId = regionObj.getParentId();
                    }
                } else Log.e(TAG, "no regions");


                if (regionsId != null) {
                    newRequest.setRegionId(regionsId);
                    if (!foundRegion) txt = regionsPicker.getText().toString() + " " + txt;
                } else {
                    txt = regionsPicker.getText().toString() + " " + txt;
                }
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
            if (newCRequest != null) newRequest.setStatus(newCRequest.getStatus());

            sendRequest(newRequest);
        } else {
            if (addressText.getVisibility() == View.VISIBLE)
                addressText.setError(mActivity.getString(R.string.required_field));
            else
                Toast.makeText(mActivity, mActivity.getString(R.string.required_field) + ": " + mActivity.getString(R.string.address), Toast.LENGTH_SHORT).show();
        }
    }

    @Background
    void sendRequest(NewRequestDetails newRequest) {
        Integer requestId = RestClient.getInstance().sendNewRequest(newRequest);
        if (requestId != null && requestId > 0) {
            TaxiApplication.setLastRequestId(requestId);
            if (newRequest.getRequestsId() == null) {
                newRequest.setRequestsId(requestId);
                SuccessDialog(R.string.request_send_successful, newRequest);
            } else SuccessDialog(R.string.request_changed_successful, newRequest);
        } else {
            Log.e(TAG, "sendRequest error");
            ErrorDialog();
        }
    }

    /*@Click
    void addressChange() {
        addressText.setText(address.getText());
        address.setText(R.string.address);
        addressText.setVisibility(View.VISIBLE);
        addressChange.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(addressText, InputMethodManager.SHOW_IMPLICIT);
    }*/

    @Click
    void addressImage() {
        if (addressText.getVisibility() == View.VISIBLE) {
            if (citiesPicker.getText() != null) {
                String txt = citiesPicker.getText().toString();
                if (txt.length() > 0) {
                    if (this.mapRequest == null) this.mapRequest = new MapRequest();
                    this.mapRequest.setCity(txt);
                }
            }
            if (regionsPicker.getText() != null) {
                String txt = regionsPicker.getText().toString();
                if (txt.length() > 0) {
                    if (this.mapRequest == null) this.mapRequest = new MapRequest();
                    this.mapRequest.setRegion(txt);
                }
            }
            if (addressText.getText() != null) {
                String txt = addressText.getText().toString();
                if (txt.length() > 0) {
                    if (this.mapRequest == null) this.mapRequest = new MapRequest();
                    this.mapRequest.setAddress(txt);
                }
            }
        }

        Intent intent = new Intent(mActivity, LongPressMapAction_.class);
        if (this.mapRequest != null) intent.putExtra("mapRequest", this.mapRequest);
        startActivityForResult(intent, SHOW_ADDRESS_ON_MAP);
        //LongPressMapAction_.intent(this).startForResult(SHOW_ADDRESS_ON_MAP);
    }

    @UiThread
    void SuccessDialog(int titleRes, final NewRequestDetails newRequest) {
        //reqInfoButtonContainer.setVisibility(View.VISIBLE);
        pbProgress.setVisibility(View.GONE);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        alertDialogBuilder.setTitle(mActivity.getString(R.string.send_request));
        String txt = addressText.getText().toString();
        //if (txt.length() == 0) txt = address.getText().toString();
        alertDialogBuilder.setMessage(mActivity.getString(titleRes, txt));

        alertDialogBuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (mListener != null) {
                    mListener.closeKeyboard();

                    NewCRequestDetails newCRequestDetails = new NewCRequestDetails();
                    newCRequestDetails.setRequestsId(newRequest.getRequestsId());
                    newCRequestDetails.setRegionId(newRequest.getRegionId());
                    newCRequestDetails.setFullAddress(newRequest.getFullAddress());
                    newCRequestDetails.setDatecreated(newRequest.getDatecreated());
                    newCRequestDetails.setCarId(newRequest.getCarId());
                    //newCRequestDetails.setRequestGroups(newRequest.getRequestGroups());
                    newCRequestDetails.setStatus(newRequest.getStatus());
                    mListener.startRequestDetails(newCRequestDetails);
                }
                //RequestsActivity_.intent(NewRequestFragment.this).start();
                //finish();
            }
        });

        Dialog successDialog = alertDialogBuilder.create();
        //if (!isFinishing()) { //fix for http://stackoverflow.com/questions/9529504/unable-to-add-window-token-android-os-binderproxy-is-not-valid-is-your-activ
        successDialog.show();
        //}
    }

    @UiThread
    void ErrorDialog() {
        //reqInfoButtonContainer.setVisibility(View.VISIBLE);
        pbProgress.setVisibility(View.GONE);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        alertDialogBuilder.setTitle(mActivity.getString(R.string.send_request));
        String txt = addressText.getText().toString();
        //if (txt.length() == 0) txt = address.getText().toString();
        alertDialogBuilder.setMessage(mActivity.getString(R.string.request_send_error, txt));

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