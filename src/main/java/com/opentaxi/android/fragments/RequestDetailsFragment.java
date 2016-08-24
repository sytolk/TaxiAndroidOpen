package com.opentaxi.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.opentaxi.android.R;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Feedback;
import com.stil.generated.mysql.tables.pojos.Groups;
import com.stil.generated.mysql.tables.pojos.Regions;
import com.stil.generated.mysql.tables.pojos.RequestFeedback;
import com.taxibulgaria.enums.RequestStatus;
import com.taxibulgaria.enums.UsersGroupEnum;
import com.taxibulgaria.rest.models.NewCRequestDetails;
import it.sephiroth.android.library.tooltip.Tooltip;
import org.androidannotations.annotations.*;
import org.androidannotations.api.BackgroundExecutor;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 4/18/13
 * Time: 2:58 PM
 * developer STANIMIR MARINOV
 */
//@WindowFeature(Window.FEATURE_NO_TITLE)
@EFragment(R.layout.request_details)
public class RequestDetailsFragment extends BaseFragment {

    private static final String TAG = "RequestDetailsFragment";

    //@Extra
    NewCRequestDetails newCRequest;

    @ViewById
    TextView requestNumber;

    @ViewById
    TextView datecreated;

    @ViewById
    TextView address;

    @ViewById
    TextView car;

    @ViewById
    TextView price_group;

    @ViewById
    TextView chosen_group;

    @ViewById
    TextView arrive_time;

    @ViewById
    TextView remaining_time;

    @ViewById
    Button rejectButton;

    @ViewById
    Button editButton;

    @ViewById
    Button feedBackButton;

    @ViewById
    Button okButton;

    @ViewById
    TextView state;

    //private static final int CAR_DETAILS = 9;
    //private static final int EDIT_REQUEST = 10;

    private Regions[] regions;
    private boolean toolTipShown = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            newCRequest = (NewCRequestDetails) bundle.getSerializable("newCRequest");
        }
    }

    public void onPause() {
        super.onPause();
        //TaxiApplication.requestsDetailsPaused();
        BackgroundExecutor.cancelAll("cancel_sec", true);
        BackgroundExecutor.cancelAll("cancel_changes", true);
        //finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        //TaxiApplication.requestsDetailsResumed();
        scheduleChangesSec();
    }

    /*@OnActivityResult(CAR_DETAILS)
    void onResult() {
    }

    @OnActivityResult(EDIT_REQUEST)
    void onEditRequest() {

    }*/

    @AfterViews
    void afterRequestsActivity() {
        showDetails();
        scheduleChangesNow();
    }

    @Background
    void scheduleChangesNow() {
        NewCRequestDetails cRequest = RestClient.getInstance().getRequestDetails(newCRequest.getRequestsId());
        if (cRequest != null) newCRequest = cRequest;
        regions = RestClient.getInstance().getRegions(); //RegionsType.BURGAS_STATE.getCode());
    }

    @Background(delay = 2000, id = "cancel_sec")
    void scheduleChangesSec() {
        try {
            if (isVisible()) {//TaxiApplication.isRequestsDetailsVisible()) {
                NewCRequestDetails cRequest = RestClient.getInstance().getRequestDetails(newCRequest.getRequestsId());
                if (cRequest != null) newCRequest = cRequest;
                showDetails();
            }
        } catch (Exception e) {
            Log.e(TAG, "scheduleChangesSec Exception", e);
        }
    }

    @Background(delay = 10000, id = "cancel_changes")
    void scheduleChanges() {
        try {
            if (isVisible()) { //TaxiApplication.isRequestsDetailsVisible()) {
                NewCRequestDetails cRequest = RestClient.getInstance().getRequestDetails(newCRequest.getRequestsId());
                if (cRequest != null) newCRequest = cRequest;
                showDetails();
            }
        } catch (Exception e) {
            Log.e(TAG, "scheduleChanges Exception", e);
        }
    }

    @UiThread
    void showDetails() {
        if (isVisible()) { //&& TaxiApplication.isRequestsDetailsVisible()) {
            if (newCRequest != null && newCRequest.getRequestsId() != null) {
                if (requestNumber != null) {
                    requestNumber.setText(newCRequest.getRequestsId().toString());

                    if (newCRequest.getDatecreated() != null) {
                        DateFormat df = android.text.format.DateFormat.getLongDateFormat(mActivity);
                        DateFormat tf = android.text.format.DateFormat.getTimeFormat(mActivity);
                        datecreated.setText(df.format(newCRequest.getDatecreated()) + " " + tf.format(newCRequest.getDatecreated()));
                    }

                    StringBuilder adr = new StringBuilder();
                    String destination = null;
                    if (newCRequest.getDetails() != null && newCRequest.getDetails().getFromCity() != null) {
                        adr.append(newCRequest.getDetails().getFromCity()).append(" ");
                        destination = newCRequest.getDetails().getDestination();
                    }

                    //Regions regions = RestClient.getInstance().getRegionById(RegionsType.BURGAS_STATE.getCode(), newCRequest.getRegionId());
                    if (regions != null) {
                        Regions region = getRegionById(regions, newCRequest.getRegionId());
                        if (region != null) adr.append(region.getDescription()).append(" ");
                    }

                    if (newCRequest.getFullAddress() != null) adr.append(newCRequest.getFullAddress());
                    if (destination != null) adr.append(" \\").append(destination).append("\\");
                    address.setText(adr.toString());

                    if (newCRequest.getCarId() != null) {
                        if (newCRequest.getCarNumber() != null && !newCRequest.getCarNumber().isEmpty()) {
                            car.setText((newCRequest.getNotes() != null ? newCRequest.getNotes() : "") + " №" + newCRequest.getCarNumber());
                            Drawable icon = new IconDrawable(mActivity, MaterialIcons.md_info).colorRes(R.color.transparent_blue).sizeDp(30);
                            //Drawable icon = new IconicsDrawable(mActivity, GoogleMaterial.Icon.gmd_info).actionBar().colorRes(R.color.transparent_blue);
                            car.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
                            car.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    if (mListener != null) mListener.startCarDetails(newCRequest.getRequestsId());
                                    // else Log.i(TAG, "mListener=null");
                                }
                            });
                            if (!toolTipShown && newCRequest.getStatus().equals(RequestStatus.NEW_REQUEST_DONE.getCode())) {
                                toolTipShown = true;
                                Tooltip.make(mActivity,
                                        new Tooltip.Builder(102)
                                                .anchor(car, Tooltip.Gravity.BOTTOM)
                                                .closePolicy(new Tooltip.ClosePolicy()
                                                        .insidePolicy(true, false)
                                                        .outsidePolicy(true, false), 10000)
                                                //.activateDelay(1800)
                                                .showDelay(2000)
                                                .text(mActivity.getString(R.string.private_request, newCRequest.getCarNumber()))
                                                //.maxWidth(500)
                                                .withArrow(true)
                                                .withOverlay(true)
                                                //.floatingAnimation(Tooltip.AnimationBuilder.SLOW)
                                                .withStyleId(R.style.ToolTipLayoutCustomStyle)
                                                .build()
                                ).show();
                            }
                        } //else Log.i(TAG, "newCRequest.getCarNumber:" + newCRequest.getCarNumber());
                    }

                    if (newCRequest.getPriceGroup() != null && newCRequest.getPriceGroup().getDescription() != null) {
                        price_group.setText(newCRequest.getPriceGroup().getDescription().replace(" ", "\n"));
                    }

                    Map<String, List<Groups>> groupsMap = newCRequest.getRequestGroups();
                    if (groupsMap != null && groupsMap.size() > 0) {
                        StringBuilder groupChosen = new StringBuilder();
                        for (List<Groups> groups : groupsMap.values()) {
                            if (groups != null) {
                                for (Groups group : groups) {
                                    if (group.getDescription() != null && !group.getDescription().isEmpty())
                                        groupChosen.append(group.getDescription());
                                    if (UsersGroupEnum.SHARED_RIDE.getCode().equals(group.getGroupsId()) && newCRequest.getDetails() != null && newCRequest.getDetails().getPassengers() != null) {
                                        groupChosen.append(" (").append(mActivity.getString(R.string.passengers)).append(":").append(newCRequest.getDetails().getPassengers()).append(")");
                                    }
                                    groupChosen.append(", ");
                                }
                            }
                        }
                        chosen_group.setText(groupChosen.toString());
                    }

                    if (newCRequest.getDispTime() != null && newCRequest.getDispTime() > 0)
                        arrive_time.setText(newCRequest.getDispTime() + " min");
                    else arrive_time.setText(R.string.not_set);

                    remaining_time.setText(newCRequest.getExecTime());


                    if (newCRequest.getStatus() != null) {
                        String statusCode = RequestStatus.getByCode(newCRequest.getStatus()).toString();
                        int resourceID = getResources().getIdentifier(statusCode, "string", mActivity.getPackageName());
                        if (resourceID > 0) {
                            try {
                                state.setText(resourceID);
                            } catch (Resources.NotFoundException e) {
                                Log.e(TAG, "Resources.NotFoundException:" + resourceID);
                            }
                        } else state.setText(statusCode);


                        if (RequestStatus.NEW_REQUEST_DELETE.getCode().equals(newCRequest.getStatus())) {
                            rejectButton.setVisibility(View.GONE);
                            editButton.setVisibility(View.GONE);
                            feedBackButton.setVisibility(View.GONE);
                        } else if (RequestStatus.NEW_REQUEST_BEGIN.getCode().equals(newCRequest.getStatus())
                                || RequestStatus.NEW_REQUEST_DONE.getCode().equals(newCRequest.getStatus())) {
                            rejectButton.setVisibility(View.GONE);
                            editButton.setText(R.string.resend);
                            editButton.setVisibility(View.VISIBLE);
                            feedBackButton.setVisibility(View.VISIBLE);
                        } else if (RequestStatus.ERROR_NO_FREE_CARS.getCode().equals(newCRequest.getStatus())) {
                            rejectButton.setVisibility(View.VISIBLE);
                            editButton.setText(R.string.resend);
                            editButton.setVisibility(View.VISIBLE);
                            feedBackButton.setVisibility(View.GONE);
                        } else if (RequestStatus.ERROR_INVALID_REQUEST.getCode().equals(newCRequest.getStatus())) {
                            rejectButton.setVisibility(View.GONE);
                            editButton.setText(R.string.change);
                            editButton.setVisibility(View.VISIBLE);
                            feedBackButton.setVisibility(View.GONE);
                        } else {
                            rejectButton.setVisibility(View.VISIBLE);
                            editButton.setText(R.string.change);
                            editButton.setVisibility(View.VISIBLE);
                            feedBackButton.setVisibility(View.GONE);
                            scheduleChanges();
                        }
                    }
                } else scheduleChangesSec();
            } else {
                Log.e(TAG, "No newCRequest or newCRequest.getRequestsId=null");
                if (mListener != null) mListener.startHome();
                //finish(); its close after server error
            }
        }
    }

    private Regions getRegionById(Regions[] regions, Integer regionId) {
        if (regionId != null) {
            if (regions != null) {
                for (Regions region : regions) {
                    if (region != null && region.getId().equals(regionId)) return region;
                }
            }
        }
        return null;
    }

    @Click
    void okButton() {
        //TaxiApplication.requestsDetailsPaused();
        if (mListener != null) {
            //mListener.startHome();
            if (newCRequest != null && RequestStatus.NEW_REQUEST_BEGIN.getCode().equals(newCRequest.getStatus()) || RequestStatus.NEW_REQUEST_DONE.getCode().equals(newCRequest.getStatus()) || RequestStatus.NEW_REQUEST_DELETE.getCode().equals(newCRequest.getStatus())) {
                mListener.startRequests(true);
            } else mListener.startRequests(false);
        }
    }

    @Click
    void rejectButton() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        alertDialogBuilder.setTitle(mActivity.getString(R.string.request_rejection));
        alertDialogBuilder.setMessage(mActivity.getString(R.string.request_reject_confirm, newCRequest.getFullAddress()));

        // Set an EditText view to get user input
        final EditText input = new EditText(mActivity);
        alertDialogBuilder.setView(input);

        alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                rejectRequest(input.getText().toString());
                if (mListener != null) mListener.startRequests(false);
            }
        });

        alertDialogBuilder.setNeutralButton(R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog rejectDialog = alertDialogBuilder.create();
        rejectDialog.show();
    }

    @Background
    void rejectRequest(String reason) {
        //TaxiApplication.requestsDetailsPaused();
        RestClient.getInstance().rejectRequest(newCRequest.getRequestsId(), reason);
    }

    @Click
    void editButton() {
        //TaxiApplication.requestsDetailsPaused();
        if (mListener != null) {
            if (RequestStatus.ERROR_NO_FREE_CARS.getCode().equals(newCRequest.getStatus())) {
                refreshRequest(newCRequest.getRequestsId());
                //scheduleChangesSec();
                mListener.startRequests(false);
            } else if (RequestStatus.ERROR_INVALID_REQUEST.getCode().equals(newCRequest.getStatus())) {
                Log.i(TAG, "ERROR_INVALID_REQUEST:" + newCRequest.getStatus());
                mListener.startEditRequest(newCRequest);
            } else { //RequestStatus.NEW_REQUEST_BEGIN.getCode().equals(newCRequest.getStatus()) || RequestStatus.NEW_REQUEST_DONE.getCode().equals(newCRequest.getStatus()
                Log.i(TAG, "newCRequest.getStatus:" + newCRequest.getStatus());
                newCRequest.setRequestsId(null);
                mListener.startEditRequest(newCRequest);
            }
        }
        /*Intent requestsIntent = new Intent(RequestDetailsFragment.this, EditRequestActivity_.class);
        requestsIntent.putExtra("newCRequest", newCRequest);
        RequestDetailsFragment.this.startActivityForResult(requestsIntent, EDIT_REQUEST);
        finish();*/
    }

    @Background
    void refreshRequest(Integer requestsId) {
        RestClient.getInstance().refreshRequest(requestsId);
    }

    @Click
    void feedBackButton() {
        setFeedBack();
    }

    @Background
    void setFeedBack() {
        showFeedBack(RestClient.getInstance().getFeedBacks());
    }

    @UiThread
    void showFeedBack(final Feedback[] feedbacks) {
        if (feedbacks != null && newCRequest != null) {
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
            alertDialogBuilder.setTitle(mActivity.getString(R.string.feedback));
            alertDialogBuilder.setMessage(mActivity.getString(R.string.feedback_request, newCRequest.getFullAddress()));

            final LinearLayout parent = new LinearLayout(mActivity);
            parent.setOrientation(LinearLayout.VERTICAL);
            parent.setGravity(Gravity.CENTER);
            parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            for (Feedback feedback : feedbacks) {
                final TextView driverLabel = new TextView(mActivity);
                driverLabel.setText(feedback.getDescription());
                parent.addView(driverLabel);
                final RatingBar rating = new RatingBar(mActivity);
                rating.setId(feedback.getId());
                rating.setNumStars(5);
                rating.setStepSize(0.1f);
                rating.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                parent.addView(rating);
            }

            final TextView commentLabel = new TextView(mActivity);
            commentLabel.setText(mActivity.getString(R.string.your_comment));
            parent.addView(commentLabel);
            final EditText comment = new EditText(mActivity);
            parent.addView(comment);

            alertDialogBuilder.setView(parent);

            alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Map<Integer, Float> vote = new HashMap<Integer, Float>();
                    List<RequestFeedback> requestFeedbackArr = new ArrayList<>();
                    for (Feedback feedback : feedbacks) {
                        RatingBar ratingBar = (RatingBar) parent.findViewById(feedback.getId());
                        if (ratingBar != null) {
                            RequestFeedback requestFeedback = new RequestFeedback();
                            requestFeedback.setRequestId(newCRequest.getRequestsId());
                            requestFeedback.setFeedbackId(feedback.getId());
                            requestFeedback.setStars((double) ratingBar.getRating());
                            requestFeedback.setNotes(comment.getText().toString());
                            requestFeedbackArr.add(requestFeedback);
                            //vote.put(feedback.getId(), ratingBar.getRating());
                        } else Log.e(TAG, "no ratingBar found with id:" + feedback.getId());
                    }

                    sendFeedBack(requestFeedbackArr);
                    dialog.dismiss();
                    if (mListener != null) mListener.startCarDetails(newCRequest.getRequestsId());
                }
            });

            alertDialogBuilder.setNeutralButton(mActivity.getString(R.string.later), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            Dialog feedBackDialog = alertDialogBuilder.create();
            feedBackDialog.show();
        }
    }

    @Background
    void sendFeedBack(List<RequestFeedback> requestFeedbackArr) {
        //if (comment != null && !comment.equals("")) RestClient.getInstance().RequestNotes(newCRequest.getRequestsId(), comment);
        RestClient.getInstance().sendFeedBack(requestFeedbackArr.toArray(new RequestFeedback[requestFeedbackArr.size()]));
    }
}