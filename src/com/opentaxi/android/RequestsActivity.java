package com.opentaxi.android;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.opentaxi.generated.mysql.tables.pojos.Regions;
import com.opentaxi.models.NewCRequest;
import com.opentaxi.models.RequestCView;
import com.opentaxi.rest.RestClient;
import com.taxibulgaria.enums.RequestStatus;
import org.androidannotations.annotations.*;

import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 4/18/13
 * Time: 2:58 PM
 * developer STANIMIR MARINOV
 */
@WindowFeature(Window.FEATURE_NO_TITLE)
//@Fullscreen
@EActivity(R.layout.requests)
public class RequestsActivity extends Activity {

    private static final String TAG = "RequestsActivity";
    private static final int REQUEST_DETAILS = 100;

    private boolean isFromHistory = false;
    private long lastShowRequests = 0;

    @ViewById
    android.widget.TableLayout requests_table;

    @ViewById
    android.widget.ProgressBar pbProgress;

    @AfterViews
    void afterRequestsActivity() {
        if (requests_table != null) {
            isFromHistory = false;
            requests_table.setVisibility(View.INVISIBLE);
            pbProgress.setVisibility(View.VISIBLE);
            getRequests();
        } else {
            Log.e(TAG, "requests_table=null");
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        TaxiApplication.requestsPaused();
    }

    @Override
    public void onResume() {
        super.onResume();
        TaxiApplication.requestsResumed();
        scheduleRequestSec();
    }

    @OnActivityResult(REQUEST_DETAILS)
    void onResult() {
    }

    @Background(delay = 1000)
    void scheduleRequestSec() {
        if (TaxiApplication.isRequestsVisible()) {
            if (isFromHistory) getRequestHistory();
            else getRequests();
        }
    }

    @Background(delay = 10000)
    void scheduleRequest() {
        if (TaxiApplication.isRequestsVisible()) {
            if (isFromHistory) getRequestHistory();
            else getRequests();
        }
    }

    @Background
    void getRequests() {
        if (new Date().getTime() > (lastShowRequests + 5000)) {
            RequestCView requestView = new RequestCView();
            requestView.setPage(1);
        /*requestView.setFilters(filters);  todo set pages
        requestView.setRows(rows);

        requestView.setSord(sord);
        requestView.setSidx(sidx);
        requestView.setSearchField(searchField);
        requestView.setSearchString(searchString);
        requestView.setSearchOper(searchOper);*/
            requestView.setMy(true);
            showRequests(RestClient.getInstance().getRequests(requestView));
        }
        scheduleRequest();
    }

    @Background
    void getRequestHistory() {
        RequestCView requestView = new RequestCView();
        requestView.setPage(1);
        requestView.setMy(true);
        requestView.setRequestStatus(RequestStatus.NEW_REQUEST_DONE.getCode());
        showRequests(RestClient.getInstance().getRequests(requestView));
    }

    @UiThread
    void showRequests(RequestCView requests) {
        synchronized (this) {
            long currDate = new Date().getTime();
            if (TaxiApplication.isRequestsVisible() && currDate > (lastShowRequests + 5000)) {
                lastShowRequests = currDate;
                requests_table.setVisibility(View.VISIBLE);
                pbProgress.setVisibility(View.GONE);
                TableRow row;
                if (requests != null && requests.getGridModel() != null) {

                    TextView time, address, car, state;

                    row = new TableRow(this);

            /*id = new TextView(this);
            id.setPadding(2, 2, 2, 2);
            id.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));*/

            /*date = new TextView(this);
            date.setPadding(2, 2, 2, 2);
            date.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));*/

                    address = new TextView(this);
                    address.setPadding(2, 2, 2, 2);
                    address.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));

                    time = new TextView(this);
                    time.setPadding(2, 2, 2, 2);
                    time.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f));

                    car = new TextView(this);
                    car.setPadding(2, 2, 2, 2);
                    car.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));

                    state = new TextView(this);
                    state.setPadding(2, 2, 2, 2);
                    state.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));

                    //id.setText("Номер");
                    //date.setText("Дата и час");
                    address.setText("Адрес");
                    time.setText("Време за изпълнение");
                    car.setText("Автомобил");
                    state.setText("Статус");

                    //row.addView(id);
                    //row.addView(date);
                    row.addView(address);
                    row.addView(time);
                    row.addView(car);
                    row.addView(state);

                    if (requests_table != null) {
                        requests_table.removeAllViews();
                        requests_table.addView(row, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        for (final NewCRequest newCRequest : requests.getGridModel()) {

                            row = new TableRow(this);

               /* id = new TextView(this);
                id.setPadding(2, 2, 2, 2);
                id.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));

                date = new TextView(this);
                date.setPadding(2, 2, 2, 2);
                date.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));*/

                            address = new TextView(this);
                            address.setPadding(2, 2, 2, 2);
                            address.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));

                            time = new TextView(this);
                            time.setPadding(2, 2, 2, 2);
                            time.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.3f));

                            car = new TextView(this);
                            car.setPadding(2, 2, 2, 2);
                            car.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));

                            state = new TextView(this);
                            state.setPadding(2, 2, 2, 2);
                            state.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));

                            //id.setText(requestMap.get("id").toString());
                            //date.setText(requestMap.get("datecreated").toString()); //DateFormat.getDateInstance(DateFormat.SHORT).format(requestMap.get("datecreated")));
                            Regions regions = RestClient.getInstance().getRegionById(newCRequest.getRegionId());
                            if (regions != null) {
                                address.setText(regions.getDescription() + " " + newCRequest.getFullAddress());
                            } else address.setText(newCRequest.getFullAddress());
                            time.setText(newCRequest.getDispTime() + " мин.");
                            car.setText(newCRequest.getCarNumber());
                            Integer status = newCRequest.getStatus();
                            state.setText(RequestStatus.getByCode(status).toString());

                            row.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    // RequestDetailsActivity_.intent(RequestsActivity.this).myDateExtra().
                                    //InquiryQuestionActivity_.intent(InquiryActivity.this).myDataExtra().startForResult();
                                    Intent requestsIntent = new Intent(RequestsActivity.this, RequestDetailsActivity_.class);
                                    requestsIntent.putExtra("newCRequest", newCRequest);
                                    // proposalIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    // proposalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    RequestsActivity.this.startActivityForResult(requestsIntent, REQUEST_DETAILS);
                                }
                            });
                   /* row.setBackgroundColor(getResources().getColor(R.color.red_color));*/


                            //row.addView(id);
                            //row.addView(date);
                            row.addView(address);
                            row.addView(time);
                            row.addView(car);
                            row.addView(state);

                            requests_table.addView(row, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        }
                    }

                } else {
                    Log.e(TAG, "requests_table=null");
                    row = new TableRow(this);
                    TextView txt = new TextView(this);
                    txt.setPadding(2, 2, 2, 2);
                    txt.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                    txt.setText("Нямате заявки.");
                    row.addView(txt);
                    requests_table.addView(row, new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
            }
        }
    }

    @Click
    void backButton() {
        if (isFromHistory) {
            isFromHistory = false;
            getRequests();
        } else {
            TaxiApplication.requestsPaused();
            finish();
        }
    }

    @Click
    void newRequests() {
        isFromHistory = false;
        TaxiApplication.requestsPaused();
        NewRequestActivity_.intent(this).start();
    }

    @Click
    void requestsHistory() {
        isFromHistory = true;
        requests_table.setVisibility(View.INVISIBLE);
        pbProgress.setVisibility(View.VISIBLE);
        getRequestHistory();
    }
}