package com.opentaxi.android.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.MaterialIcons;
import com.opentaxi.android.R;
import com.paging.listview.PagingBaseAdapter;
import com.stil.generated.mysql.tables.pojos.Groups;
import com.stil.generated.mysql.tables.pojos.Regions;
import com.taxibulgaria.enums.RequestStatus;
import com.taxibulgaria.rest.models.NewCRequest;

import java.text.DateFormat;
import java.util.List;
import java.util.Map;


public class RequestPagingAdapter extends PagingBaseAdapter<NewCRequest> {

    private Context context;
    private Regions[] regions;
    private boolean history;

    public RequestPagingAdapter(Context context, Regions[] regions, boolean history) {
        super();
        this.context = context;
        this.regions = regions;
        this.history = history;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public NewCRequest getRequest(int position) {
        return items.get(position);
    }

    @Override
    public String getItem(int position) {
        NewCRequest request = items.get(position);
        StringBuilder row = new StringBuilder();

        //row.append(context.getString(R.string.request_number)).append(": ").append(request.getRequestsId().toString()).append("\n")
        DateFormat df = android.text.format.DateFormat.getLongDateFormat(context);
        DateFormat tf = android.text.format.DateFormat.getTimeFormat(context);
        row.append(context.getString(R.string.datetime)).append(": ").append(df.format(request.getDatecreated())).append(" ").append(tf.format(request.getDatecreated())).append("\n")
                //ADDRESS
                .append(context.getString(R.string.address)).append(": ");
        /* String destination = null;
        if (request.getDetails() != null && request.getDetails().getFromCity() != null) {
            row.append(request.getDetails().getFromCity()).append(" ");
            destination = request.getDetails().getDestination();
        }*/

        //Regions regions = RestClient.getInstance().getRegionById(RegionsType.BURGAS_STATE.getCode(), newCRequest.getRegionId());
        if (regions != null) {
            Regions region = getRegionById(request.getRegionId());
            if (region != null && !request.getFullAddress().contains(region.getDescription()))
                row.append(region.getDescription()).append(" ");
        }

        row.append(request.getFullAddress());
        //if (destination != null) row.append(" \\").append(destination).append("\\");
        row.append("\n");

        //CAR NUMBER
        if (request.getCarId() != null) { //no car founded yet
            if (request.getCarNumber() != null && !request.getCarNumber().isEmpty()) {
                row.append(context.getString(R.string.car)).append(": ");
                if (request.getNotes() != null) row.append(request.getNotes());
                row.append(" №").append(request.getCarNumber()).append("\n");
            }
        }

        Map<String, List<Groups>> groupsMap = request.getRequestGroups();
        if (groupsMap != null && groupsMap.size() > 0) {
            row.append(context.getString(R.string.filters)).append(": ");
            for (List<Groups> groups : groupsMap.values()) {
                if (groups != null) {
                    for (Groups group : groups) {
                        if (group.getDescription() != null && !group.getDescription().isEmpty())
                            row.append(group.getDescription()).append(", ");
                    }
                }
            }
            row.append("\n");
        }

        if (request.getDispTime() != null && request.getDispTime() > 0)
            row.append(context.getString(R.string.car_arrive_time)).append(": ").append(context.getResources().getQuantityString(R.plurals.minutes, request.getDispTime(), request.getDispTime())).append("\n");

        /*if (request.getExecTime() != null)
            row.append(context.getString(R.string.time_remaining)).append(": ").append(request.getExecTime()).append("\n");*/

        if (request.getStatus() != null) {
            String statusCode = RequestStatus.getByCode(request.getStatus()).toString();
            int resourceID = context.getResources().getIdentifier(statusCode, "string", context.getPackageName());
            if (resourceID > 0) {
                try {
                    row.append(context.getString(R.string.status)).append(": ").append(context.getString(resourceID));
                } catch (Resources.NotFoundException e) {
                    Log.e("RequestPagingAdapter", "Resources NotFoundException:" + resourceID);
                }
            } else Log.e("RequestPagingAdapter", "Undefined state:" + resourceID);
        }

        return row.toString();
    }

    @Override
    public long getItemId(int position) {
        NewCRequest request = items.get(position);
        return request.getRequestsId();
        // return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        String text = getItem(position);

        if (convertView != null) {
            textView = (TextView) convertView;
        } else {
            textView = (TextView) LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, null);
        }
        textView.setText(text);
        Drawable icon;
        if (history)
            icon = new IconDrawable(context, MaterialIcons.md_feedback).colorRes(R.color.transparent_blue).sizeDp(25);
            //icon = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_feedback).actionBar().colorRes(R.color.transparent_blue);
        else {
            icon = new IconDrawable(context, MaterialIcons.md_mode_edit).colorRes(R.color.timebase_color).sizeDp(25);
            textView.setTextColor(ContextCompat.getColor(context, R.color.black_color));
        }
        //icon = new IconicsDrawable(context, GoogleMaterial.Icon.gmd_mode_edit).actionBar().colorRes(R.color.timebase_color);
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        return textView;
    }

    private Regions getRegionById(Integer regionId) {
        if (regionId != null) {
            if (regions != null) {
                for (Regions region : regions) {
                    if (region != null && region.getId().equals(regionId)) return region;
                }
            }
        }
        return null;
    }
}
