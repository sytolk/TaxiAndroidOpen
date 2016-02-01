package com.opentaxi.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.opentaxi.android.R;
import com.opentaxi.android.TaxiApplication;
import com.opentaxi.android.adapters.RequestPagingAdapter;
import com.opentaxi.models.NewCRequest;
import com.opentaxi.models.RequestCView;
import com.opentaxi.rest.RestClient;
import com.paging.listview.PagingListView;
import com.stil.generated.mysql.tables.pojos.Regions;
import com.taxibulgaria.enums.RequestStatus;
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
public class RequestsHistoryFragment extends Fragment {

    private static final String TAG = "RequestsHistoryFragment";
    //private static final int REQUEST_DETAILS = 100;

    @ViewById(R.id.list_title)
    TextView title;

    @ViewById(R.id.paging_list_view)
    PagingListView listView;

    RequestPagingAdapter adapter;
    private int pager = 0;

    /*@ViewById
    android.widget.ProgressBar pbProgress;*/

    @ViewById(R.id.buttonPanel)
    LinearLayout buttonPanel;

    Activity mActivity;

    OnCommandListener mListener;

    private boolean history = false;

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
            history = bundle.getBoolean("history", false);
        }
    }

    @AfterViews
    void afterView() {
        //pbProgress.setVisibility(View.VISIBLE);

        Button btnTag = new Button(mActivity);
        btnTag.setLayoutParams(new ViewGroup.LayoutParams(ViewPager.LayoutParams.WRAP_CONTENT, ViewPager.LayoutParams.WRAP_CONTENT));
        btnTag.setText(R.string.history);
        btnTag.setId(1234);
        btnTag.setTag(history);
        btnTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean historyClick = (Boolean) v.getTag();
                if (historyClick) history = false;
                else history = true;
                setAdapter();
                //if (mListener != null) mListener.startRequests(true);
            }
        });
        buttonPanel.addView(btnTag);
        setAdapter();
    }

    @Background
    void setAdapter() {
        setAdapterUI(RestClient.getInstance().getRegions());
    }

    @UiThread
    void setAdapterUI(Regions[] regions) {
        adapter = new RequestPagingAdapter(mActivity, regions);

        listView.setAdapter(adapter);
        listView.setHasMoreItems(true);
        listView.setPagingableListener(new PagingListView.Pagingable() {
            @Override
            public void onLoadMoreItems() {
                loadMoreItems();
            }
        });
    }

    @Background
    void loadMoreItems() {
        RequestCView requestView = new RequestCView();
        requestView.setPage(pager);
        requestView.setMy(true);
        if (history) {
            title.setText(mActivity.getString(R.string.request_history));
            requestView.setRequestStatus(RequestStatus.NEW_REQUEST_DONE.getCode());
        } else {
            title.setText(mActivity.getString(R.string.active_requests));
            //todo update items
        }
        showItems(RestClient.getInstance().getRequests(requestView));
    }

    @UiThread
    void showItems(RequestCView newItems) {
        //pbProgress.setVisibility(View.GONE);
        if (newItems != null) {
            List<NewCRequest> newCRequest = newItems.getGridModel();
            if (newCRequest != null && newCRequest.size() > 0) {
                pager++;
                listView.onFinishLoading(true, newCRequest);
            } else listView.onFinishLoading(false, null);
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
    }

    @UiThread
    void setActivityTile(String title) {

        if (mListener != null) mListener.setBarTitle(title);
    }

    @Click
    void okButton() {
        if (mListener != null) mListener.startHome();
    }
}