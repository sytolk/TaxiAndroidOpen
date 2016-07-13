package com.opentaxi.android.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import com.opentaxi.android.R;
import com.opentaxi.android.TaxiApplication;
import com.opentaxi.android.adapters.RequestPagingAdapter;
import com.opentaxi.rest.RestClient;
import com.paging.listview.PagingListView;
import com.stil.generated.mysql.tables.pojos.Regions;
import com.taxibulgaria.enums.RequestStatus;
import com.taxibulgaria.rest.models.NewCRequest;
import com.taxibulgaria.rest.models.NewCRequestDetails;
import com.taxibulgaria.rest.models.RequestCView;
import org.androidannotations.annotations.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 4/18/13
 * Time: 2:58 PM
 * developer STANIMIR MARINOV
 */
//@WindowFeature(Window.FEATURE_NO_TITLE)
//@Fullscreen
@EFragment(R.layout.paging_list)
public class RequestsHistoryFragment extends BaseFragment {

    private static final String TAG = "RequestsHistoryFragment";
    //private static final int REQUEST_DETAILS = 100;

    @ViewById(R.id.list_title)
    TextView title;

    @ViewById(R.id.paging_list_view)
    PagingListView listView;

    RequestPagingAdapter adapter;
    private int pager = 1;

    /*@ViewById
    android.widget.ProgressBar pbProgress;*/

    @ViewById(R.id.history)
    Button buttonHistory;

    private boolean history = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            history = bundle.getBoolean("history", false);
        }
    }

    @AfterViews
    void afterView() {
        //pbProgress.setVisibility(View.VISIBLE);
        if (history) buttonHistory.setText(R.string.active_requests);
        else buttonHistory.setText(R.string.history);

        setAdapter();
    }

    @Background
    void setAdapter() {
        setAdapterUI(RestClient.getInstance().getRegions());
    }

    @UiThread
    void setAdapterUI(Regions[] regions) {
        if (isVisible()) {
            title.setVisibility(View.VISIBLE);
            /*if (!listView.hasMoreItems())*/
            listView.setHasMoreItems(true);

            adapter = new RequestPagingAdapter(mActivity, regions, history);
            listView.setAdapter(adapter);
            listView.setPagingableListener(new PagingListView.Pagingable() {
                @Override
                public void onLoadMoreItems() {
                    loadMoreItems();
                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Log.i(TAG, "setOnItemClickListener:" + parent.getItemAtPosition(position));
                    if (mListener != null) {
                        NewCRequestDetails newCRequestDetails = new NewCRequestDetails();
                        newCRequestDetails.setRequestsId((int) id);
                        if (adapter != null) {
                            NewCRequest newCRequest = adapter.getRequest(position);
                            newCRequestDetails.setRegionId(newCRequest.getRegionId());
                            newCRequestDetails.setFullAddress(newCRequest.getFullAddress());
                            newCRequestDetails.setDatecreated(newCRequest.getDatecreated());
                            newCRequestDetails.setRequestGroups(newCRequest.getRequestGroups());
                            newCRequestDetails.setCarId(newCRequest.getCarId());
                            newCRequestDetails.setCarNumber(newCRequest.getCarNumber());
                            newCRequestDetails.setStatus(newCRequest.getStatus());
                            //Log.i(TAG, "setOnItemClickListener:" + newCRequest);
                        }

                        mListener.startRequestDetails(newCRequestDetails);
                    }
                }
            });
            listView.setItemsCanFocus(true);
        } //else Log.i(TAG,"setAdapterUI not visible");
    }

    @Background
    void loadMoreItems() {
        RequestCView requestView = new RequestCView();
        requestView.setPage(pager);
        requestView.setMy(true);
        if (history) {
            //title.setText(mActivity.getString(R.string.request_history));
            setActivityTile(mActivity.getString(R.string.request_history));
            requestView.setRequestStatus(RequestStatus.NEW_REQUEST_DONE.getCode());
        } else {
            setActivityTile(mActivity.getString(R.string.active_requests));
            //title.setText(mActivity.getString(R.string.active_requests));
            //todo update items
        }
        showItems(RestClient.getInstance().getRequests(requestView));
    }

    @UiThread
    void showItems(RequestCView newItems) {
        //pbProgress.setVisibility(View.GONE);
        if (newItems != null && listView != null) {
            //Log.i(TAG, "newItems page:" + pager);
            List<NewCRequest> newCRequest = newItems.getGridModel();
            if (newCRequest != null && newCRequest.size() > 0) {
                pager++;
                boolean hasMoreItems = newCRequest.size() == newItems.getRows();
                listView.onFinishLoading(hasMoreItems, newCRequest);
                title.setVisibility(View.GONE);
            } else {
                //Log.i(TAG, "newCRequest:" + newCRequest + " size=0");
                listView.onFinishLoading(false, null);
            }
        } //else Log.i(TAG, "newItems==null");
    }

    @Override
    public void onPause() {
        super.onPause();
        TaxiApplication.requestsPaused();
    }

    @Override
    public void onResume() {
        super.onResume();
        pager = 1;
        TaxiApplication.requestsResumed();
    }

    @UiThread
    void setActivityTile(String title) {

        if (mListener != null) mListener.setBarTitle(title);
    }

    @Click
    void okButton() {
        if (mListener != null) mListener.startHome();
    }

    @Click
    void history() {
        if (history) {
            history = false;
            if (buttonHistory != null) buttonHistory.setText(R.string.history);

        } else {
            history = true;
            if (buttonHistory != null) buttonHistory.setText(R.string.active_requests);
        }
        pager = 1;
        setAdapter();
    }
}