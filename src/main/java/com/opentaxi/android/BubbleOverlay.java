/*
 * Copyright © 2013-2014 Ludwig M Brinckmann
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.opentaxi.android;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Cars;
import com.taxibulgaria.rest.models.NewCRequest;
import com.taxibulgaria.rest.models.RequestCView;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.mapsforge.applications.android.LocationOverlayMapViewer;
import org.mapsforge.applications.android.Utils;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layer;
import org.mapsforge.map.layer.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic map viewer that shows bubbles with content at a few locations.
 */
@EActivity //(R.layout.mapviewer)
public class BubbleOverlay extends LocationOverlayMapViewer {

    private Bitmap bubble;
    //private MarkerOverlay addressOverlay = new MarkerOverlay();
    //private MarkerOverlay carsOverlay = new MarkerOverlay();
    List<Layer> addressOverlay = new ArrayList<Layer>();
    List<Layer> carsOverlay = new ArrayList<Layer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidGraphicFactory.createInstance(this.getApplication());
        if (getMapFileName() == null) {
            try {
                setMapFile(AppPreferences.getInstance().getMapFile());
            } catch (Exception e) {
                Log.e(TAG, "BubbleOverlay", e);
            }
        }
        try {
            super.onCreate(savedInstanceState);
        } catch (IllegalArgumentException e) { //invalid map file
            e.printStackTrace();
            startMapFilePicker();
        } catch (RuntimeException e) { //invalid map file
            e.printStackTrace();
            startMapFilePicker();
        } catch (Exception e) { //invalid map file
            e.printStackTrace();
            startMapFilePicker();
        }
    }

    @Override
    protected void onDestroy() {
        if (bubble != null) bubble.decrementRefCount();
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        TaxiApplication.mapPaused();
        if (AppPreferences.getInstance() != null && getMapFileName() != null) {
            AppPreferences.getInstance().setMapFile(getMapFileName());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        TaxiApplication.mapResumed();
    }

    @Background(delay = 15000)
    void showMyRequestsDelayed() {
        if (TaxiApplication.isMapVisible()) {
            RequestCView requestView = new RequestCView();
            requestView.setPage(1);
            requestView.setMy(true);
            showRequests(RestClient.getInstance().getRequests(requestView));
        }
    }

    @Background
    void showMyRequests() {
        RequestCView requestView = new RequestCView();
        requestView.setPage(1);
        requestView.setMy(true);
        showRequests(RestClient.getInstance().getRequests(requestView));
    }

    @Background
    void showCar(Integer carsId) {
        if (TaxiApplication.isMapVisible()) {
            showCarPosition(RestClient.getInstance().getCarsInfo(carsId));
        }
    }

    @UiThread
    void showCarPosition(Cars cars) {
        if (TaxiApplication.isMapVisible() && cars != null) {

            //final List<Layer> overlayItems = carsOverlay.getOverlayItems();
            //overlayItems.clear();

            TextView bubbleView = new TextView(this);
            Utils.setBackground(bubbleView, getResources().getDrawable(R.drawable.balloon_overlay_unfocused));
            bubbleView.setGravity(Gravity.CENTER);
            bubbleView.setMaxEms(20);
            bubbleView.setTextSize(15);
            bubbleView.setTextColor(Color.BLACK);
            bubbleView.setText(cars.getNumber());
            bubble = Utils.viewToBitmap(this, bubbleView);
            bubble.incrementRefCount();

            if (cars.getCurrPosNorth() != null && cars.getCurrPosEast() != null) {
                Marker marker = new Marker(new LatLong(cars.getCurrPosNorth(), cars.getCurrPosEast()), bubble, 0, -bubble.getHeight() / 2);
                //marker.setDisplayModel(this.mapViews.get(0).getModel().displayModel);
                carsOverlay.add(marker);
                mapView.getLayerManager().getLayers().add(marker);
                //Log.i(TAG, "Car:" + cars.getNumber());
            }

            //carsOverlay.requestRedraw();
        }
    }

    @UiThread
    void showRequests(RequestCView requests) {
        if (requests != null) {
            //final List<Layer> overlayItems = addressOverlay.getOverlayItems();
            //overlayItems.clear();
            if (addressOverlay.size() > 0) {
                for (Layer layer : this.addressOverlay) {
                    mapView.getLayerManager().getLayers().remove(layer);
                }
                addressOverlay.clear();
            }
            if (carsOverlay.size() > 0) {
                for (Layer layer : this.carsOverlay) {
                    mapView.getLayerManager().getLayers().remove(layer);
                }
                carsOverlay.clear();
            }

            List<NewCRequest> newRequest = requests.getGridModel();
            if (newRequest != null) {
                for (final NewCRequest newCRequest : newRequest) {
                    if (newCRequest != null) {
                        if (newCRequest.getNorth() != null && newCRequest.getEast() != null && newCRequest.getNorth() > 0 && newCRequest.getEast() > 0 && newCRequest.getFullAddress() != null) {
                            TextView bubbleView = new TextView(this);
                            Utils.setBackground(bubbleView, getResources().getDrawable(R.drawable.balloon_overlay_unfocused));
                            bubbleView.setGravity(Gravity.CENTER);
                            bubbleView.setMaxEms(20);
                            bubbleView.setTextSize(15);
                            bubbleView.setTextColor(Color.BLACK);
                            bubbleView.setText(newCRequest.getFullAddress());
                            bubble = Utils.viewToBitmap(this, bubbleView);
                            bubble.incrementRefCount();
                            Marker marker = new Marker(new LatLong(newCRequest.getNorth(), newCRequest.getEast()), bubble, 0, -bubble.getHeight() / 2);
                            //marker.setDisplayModel(this.mapViews.get(0).getModel().displayModel);
                            addressOverlay.add(marker);
                            //Log.i(TAG, newCRequest.getFullAddress());
                        }
                        if (newCRequest.getCarNumber() != null) {
                            //Log.i(TAG, "CarNumber:" + newCRequest.getCarNumber());
                            showCar(newCRequest.getCarId());
                        }
                    }
                }
            }
            mapView.getLayerManager().getLayers().addAll(addressOverlay);
            //this.layerManagers.get(0).getLayers().setGroup("requests", overlayItems);
            //addressOverlay.requestRedraw();
        }
        showMyRequestsDelayed();
    }

    @Override
    protected void createLayers() {
        try {
            super.createLayers();
        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(TAG, "Invalid map file? " + e.getMessage());
            startMapFilePicker();
        }
        //Layers layers = this.layerManagers.get(0).getLayers();
        //if (!layers.contains(addressOverlay)) layers.add(addressOverlay);
        //if (!layers.contains(carsOverlay)) layers.add(carsOverlay);
        showMyRequests();
    }

    /*@Override
    protected void createMapViewPositions() {
        super.createMapViewPositions();
        this.mapViews.get(0).getModel().mapViewPosition.setCenter(new LatLong(42.5, 27.468));
    }*/

    /*@Override
    protected void destroyLayers() {
        if (bubble != null) bubble.decrementRefCount();
    }*/

   /* @Override
    protected void onStart() {
        super.onStart();
        this.mapViewPositions.get(0).setCenter(new LatLong(42.5, 27.468));
    }*/
}
