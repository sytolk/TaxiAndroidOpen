package com.opentaxi.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import com.opentaxi.generated.mysql.tables.pojos.Feedback;
import com.opentaxi.generated.mysql.tables.pojos.Groups;
import com.opentaxi.generated.mysql.tables.pojos.Regions;
import com.opentaxi.models.NewCRequest;
import com.opentaxi.rest.RestClient;
import com.taxibulgaria.enums.RequestStatus;
import org.androidannotations.annotations.*;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 4/18/13
 * Time: 2:58 PM
 * developer STANIMIR MARINOV
 */
@WindowFeature(Window.FEATURE_NO_TITLE)
@EActivity(R.layout.request_details)
public class RequestDetailsActivity extends Activity {

    private static final String TAG = "RequestDetailsActivity";

    @Extra
    NewCRequest newCRequest;

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
    TextView state;

    private static final int CAR_DETAILS = 9;

    public void onPause() {
        super.onPause();
        TaxiApplication.requestsDetailsPaused();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        TaxiApplication.requestsDetailsResumed();
        scheduleChangesSec();
    }

    @OnActivityResult(CAR_DETAILS)
    void onResult() {
    }

    @AfterViews
    void afterRequestsActivity() {
        showDetails();
    }

    @Background(delay = 1000)
    void scheduleChangesSec() {
        if (TaxiApplication.isRequestsDetailsVisible()) {
            NewCRequest cRequest = RestClient.getInstance().getRequestDetails(newCRequest.getRequestsId());
            if (cRequest != null) newCRequest = cRequest;
            showDetails();
        }
    }

    @Background(delay = 10000)
    void scheduleChanges() {
        if (TaxiApplication.isRequestsDetailsVisible()) {
            NewCRequest cRequest = RestClient.getInstance().getRequestDetails(newCRequest.getRequestsId());
            if (cRequest != null) newCRequest = cRequest;
            showDetails();
        }
    }

    @UiThread
    void showDetails() {
        if (TaxiApplication.isRequestsDetailsVisible()) {
            if (newCRequest != null && newCRequest.getRequestsId() != null) {
                if (requestNumber != null) {
                    requestNumber.setText(newCRequest.getRequestsId().toString());
                    datecreated.setText(DateFormat.getDateInstance(DateFormat.SHORT).format(newCRequest.getDatecreated()));
                    Regions regions = RestClient.getInstance().getRegionById(newCRequest.getRegionId());
                    if (regions != null) {
                        address.setText(regions.getDescription() + " " + newCRequest.getFullAddress());
                    } else address.setText(newCRequest.getFullAddress());

                    if (newCRequest.getCarNumber() != null && !newCRequest.getCarNumber().equals("")) {
                        car.setText("Стил №" + newCRequest.getCarNumber());

                        car.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Intent requestsIntent = new Intent(RequestDetailsActivity.this, CarDetailsActivity_.class);
                                requestsIntent.putExtra("carNumber", newCRequest.getCarNumber());
                                RequestDetailsActivity.this.startActivityForResult(requestsIntent, CAR_DETAILS);
                            }
                        });
                    }
                    Map<String, List<Groups>> groupsMap = newCRequest.getRequestGroups();
                    if (groupsMap.containsKey("PRICE_GROUPS")) {
                        List<Groups> priceGroups = groupsMap.get("PRICE_GROUPS");
                        if (priceGroups.size() > 0) {
                            Groups priceGroup = priceGroups.get(0);
                            if (priceGroup != null)
                                price_group.setText(priceGroup.getDescription());
                        }
                    }
                    StringBuilder groupChosen = new StringBuilder();
                    for (Map.Entry<String, List<Groups>> groups : groupsMap.entrySet()) {
                        List<Groups> priceGroups = groups.getValue();
                        if (priceGroups != null) {
                            Groups priceGroup = priceGroups.get(0);
                            if (priceGroup != null) {
                                if (groups.getKey().equals("PRICE_GROUPS")) {
                                    price_group.setText(priceGroup.getDescription());
                                } else {
                                    groupChosen.append(priceGroup.getDescription()).append(",");
                                }
                            }
                        }
                    }
                    chosen_group.setText(groupChosen.toString());

                    if (newCRequest.getDispTime() != null && newCRequest.getDispTime() > 0)
                        arrive_time.setText(newCRequest.getDispTime() + " min");
                    else arrive_time.setText("не зададено");

                    remaining_time.setText(newCRequest.getExecTime());


                    if (newCRequest.getStatus() != null) {
                        String statusCode = RequestStatus.getByCode(newCRequest.getStatus()).toString();
                        int resourceID = getResources().getIdentifier(statusCode, "string", getPackageName());
                        if (resourceID > 0) {
                            try {
                                state.setText(resourceID);
                            } catch (Resources.NotFoundException e) {
                                Log.e(TAG, "Resources.NotFoundException:" + resourceID);
                            }
                        } else state.setText(statusCode);
                    }

                    if (newCRequest.getStatus().equals(RequestStatus.NEW_REQUEST_DELETE.getCode())) {
                        rejectButton.setVisibility(View.GONE);
                        editButton.setVisibility(View.GONE);
                        feedBackButton.setVisibility(View.GONE);
                    } else if (newCRequest.getStatus().equals(RequestStatus.NEW_REQUEST_BEGIN.getCode()) || newCRequest.getStatus().equals(RequestStatus.NEW_REQUEST_DONE.getCode())) {
                        rejectButton.setVisibility(View.GONE);
                        editButton.setVisibility(View.GONE);
                        feedBackButton.setVisibility(View.VISIBLE);
                    } else {
                        rejectButton.setVisibility(View.VISIBLE);
                        editButton.setVisibility(View.VISIBLE);
                        feedBackButton.setVisibility(View.GONE);
                        scheduleChanges();
                    }
                } else scheduleChangesSec();
            } else {
                Log.e(TAG, "No newCRequest or newCRequest.getRequestsId=null");
                //finish(); its close after server error
            }
        }
    }

    @Click
    void okButton() {
        TaxiApplication.requestsDetailsPaused();
        finish();
    }

    @Click
    void rejectButton() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Отказ на заявка");
        alertDialogBuilder.setMessage("Сигурни ли сте че искате да откажете заявката " + newCRequest.getFullAddress() + " ?");

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alertDialogBuilder.setView(input);

        alertDialogBuilder.setPositiveButton("ДА", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                rejectRequest(input.getText().toString());
                finish();
            }
        });

        alertDialogBuilder.setNeutralButton("НЕ", new DialogInterface.OnClickListener() {

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
        TaxiApplication.requestsDetailsPaused();
        RestClient.getInstance().rejectRequest(newCRequest.getRequestsId(), reason);
    }

    @Click
    void editButton() {
        TaxiApplication.requestsDetailsPaused();
        Intent requestsIntent = new Intent(RequestDetailsActivity.this, EditRequestActivity_.class);
        requestsIntent.putExtra("newCRequest", newCRequest);
        // proposalIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // proposalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        RequestDetailsActivity.this.startActivity(requestsIntent);
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
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Обратна връзка");
            alertDialogBuilder.setMessage("Как оценявате вашете пътуване от \"" + newCRequest.getFullAddress() + "\"");

            final LinearLayout parent = new LinearLayout(this);
            parent.setOrientation(LinearLayout.VERTICAL);
            parent.setGravity(Gravity.CENTER);
            parent.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            for (Feedback feedback : feedbacks) {
                final TextView driverLabel = new TextView(this);
                driverLabel.setText(feedback.getDescription());
                parent.addView(driverLabel);
                final RatingBar rating = new RatingBar(this);
                rating.setId(feedback.getId());
                rating.setNumStars(5);
                rating.setStepSize(0.1f);
                rating.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                parent.addView(rating);
            }

            final TextView commentLabel = new TextView(this);
            commentLabel.setText("Вашият коментар:");
            parent.addView(commentLabel);
            final EditText comment = new EditText(this);
            parent.addView(comment);

            alertDialogBuilder.setView(parent);

            alertDialogBuilder.setPositiveButton("ОК", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Map<Integer, Float> vote = new HashMap<Integer, Float>();
                    for (Feedback feedback : feedbacks) {
                        RatingBar ratingBar = (RatingBar) parent.findViewById(feedback.getId());
                        if (ratingBar != null) vote.put(feedback.getId(), ratingBar.getRating());
                        else Log.e(TAG, "no ratingBar found with id:" + feedback.getId());
                    }

                    sendFeedBack(comment.getText().toString(), vote);
                    dialog.dismiss();
                }
            });

            alertDialogBuilder.setNeutralButton("По късно", new DialogInterface.OnClickListener() {

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
    void sendFeedBack(String comment, Map<Integer, Float> vote) {
        if (comment != null && !comment.equals(""))
            RestClient.getInstance().RequestNotes(newCRequest.getRequestsId(), comment);
        RestClient.getInstance().sendFeedBack(newCRequest.getRequestsId(), vote);
    }
}