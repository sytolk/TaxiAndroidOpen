package com.opentaxi.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.android.utils.MyGeocoder;
import com.opentaxi.models.MapRequest;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.NewRequest;
import com.stil.generated.mysql.tables.pojos.Regions;
import com.taxibulgaria.enums.RegionsType;
import org.androidannotations.annotations.*;
import org.mapsforge.applications.android.mapbg.LocationOverlayMapViewer;
import org.mapsforge.applications.android.mapbg.TextCircle;
import org.mapsforge.applications.android.mapbg.Utils;
import org.mapsforge.core.graphics.Align;
import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.layer.Layers;
import org.mapsforge.map.layer.overlay.Circle;
import org.mapsforge.map.layer.renderer.TileRendererLayer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Demonstrates how to enable a LongPress on a layer, long press creates/removes
 * circles, tap on a circle toggles the colour.
 */
@EActivity
public class LongPressMapAction extends LocationOverlayMapViewer {

    @Extra
    MapRequest mapRequest;

    private static final Paint GREEN = Utils.createPaint(AndroidGraphicFactory.INSTANCE.createColor(Color.GREEN), 0, Style.FILL);
    int index = -1;

    //private NewRequest address = new NewRequest();
    /*private static final Paint RED = Utils.createPaint(
            AndroidGraphicFactory.INSTANCE.createColor(Color.RED), 0,
            Style.FILL);*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppPreferences.getInstance() != null && mapFileName == null) {
            setMapFile(AppPreferences.getInstance().getMapFile());
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        TaxiApplication.mapPaused();
        if (AppPreferences.getInstance() != null && mapFileName != null) {
            AppPreferences.getInstance().setMapFile(mapFileName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        TaxiApplication.mapResumed();
    }

    protected int getLayoutId() {
        return R.layout.show_address;
    }

    @Override
    protected void createLayers() {
        try {
            super.createLayers();
        } catch (Exception e) {
            if (e.getMessage() != null) Log.e(TAG, "Invalid map file? " + e.getMessage());
            startMapFilePicker();
        }
        Layers layers = this.layerManagers.get(0).getLayers();
        File mapFile = this.getMapFile();
        if (mapFile != null) {
            Log.i(TAG, "createLayers " + mapFile.getAbsolutePath());
            TileRendererLayer tileRendererLayer = new TileRendererLayer(
                    this.tileCache,
                    this.mapViewPositions.get(0),
                    false,
                    org.mapsforge.map.android.graphics.AndroidGraphicFactory.INSTANCE) {
                @Override
                public boolean onLongPress(LatLong tapLatLong, Point thisXY,
                                           Point tapXY) {
                    LongPressMapAction.this.onLongPress(tapLatLong);
                    return true;
                }
            };
            tileRendererLayer.setMapFile(mapFile);
            tileRendererLayer.setXmlRenderTheme(this.getRenderTheme());
            layers.add(tileRendererLayer);

            //allow move to map if its have GPS cooordinates
            invertSnapToLocation();

            if (mapRequest != null) showTextCircle(mapRequest);
            else showAlert();
        }
    }

    void showAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(getString(R.string.address_choose));
        alertDialogBuilder.setMessage(getString(R.string.choose_from_map));
        alertDialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    protected void onLongPress(LatLong position) {
        if (this.mapRequest == null) this.mapRequest = new MapRequest();
        this.mapRequest.setNorth(position.latitude);
        this.mapRequest.setEast(position.longitude);
        showAddress(position);
        Layers layers = this.layerManagers.get(0).getLayers(); //this.mapViews.get(0).getLayerManager().getLayers()
        if (index >= 0) layers.remove(index);
        float circleSize = 8 * this.mapViews.get(0).getModel().displayModel.getScaleFactor();
        Circle circle = new Circle(position, circleSize, GREEN, null);
        layers.add(circle);
        index = layers.size() - 1;
        circle.requestRedraw();
    }

    @Background
    void showAddress(LatLong position) {
        NewRequest address = RestClient.getInstance().getAddressByCoordinates((float) position.latitude, (float) position.longitude);
        this.mapRequest = new MapRequest();
        if (address != null) {
            this.mapRequest.setCity(getString(R.string.burgas));
            Regions regions = RestClient.getInstance().getRegionById(RegionsType.BURGAS_STATE.getCode(), address.getRegionId());
            if (regions != null) this.mapRequest.setRegion(regions.getDescription());
            this.mapRequest.setAddress(address.getFullAddress());
            this.mapRequest.setNorth(position.latitude);
            this.mapRequest.setEast(position.longitude);
            showTextCircle(this.mapRequest);
            return;
        } else Log.i(TAG, "address=null");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this);
            try {
                List<Address> addresses = geocoder.getFromLocation(position.latitude, position.longitude, 1);
                //Log.i(TAG, "Address from Geocoder:" + addresses.toString());
                if (addresses == null || addresses.isEmpty()) {
                    addresses = MyGeocoder.getFromLocation(position.latitude, position.longitude, 1);
                }
                processAddress(position.latitude, position.longitude, addresses);

            } catch (IOException e) {
                List<Address> addresses = MyGeocoder.getFromLocation(position.latitude, position.longitude, 1);
                processAddress(position.latitude, position.longitude, addresses);
                Log.e(TAG, "IOException:" + e.getMessage());
            }
        } else {
            Log.e(TAG, "Geocoder not present");
            List<Address> addresses = MyGeocoder.getFromLocation(position.latitude, position.longitude, 1);
            processAddress(position.latitude, position.longitude, addresses);
        }
    }

    private void processAddress(double latitude, double longitude, List<Address> addresses) {
        if (addresses != null && !addresses.isEmpty()) {
            Address gAddr = addresses.get(0);
            Log.i(TAG, gAddr.toString());
            if (gAddr.getLocality() != null) {
                this.mapRequest.setCity(gAddr.getLocality());
                if (gAddr.getMaxAddressLineIndex() >= 0) {
                    String adr = gAddr.getAddressLine(0);
                    if (adr != null && !adr.equals("Unnamed Rd"))
                        this.mapRequest.setAddress(adr);
                }
                if (gAddr.getLatitude() > 0) this.mapRequest.setNorth(gAddr.getLatitude());
                else this.mapRequest.setNorth(latitude);
                if (gAddr.getLongitude() > 0) this.mapRequest.setEast(gAddr.getLongitude());
                else this.mapRequest.setEast(longitude);
                showTextCircle(this.mapRequest);
            } else Log.i(TAG, "Address from Geocoder no getLocality");
        }
    }

    @UiThread
    void showTextCircle(MapRequest address) {
        if (address != null && address.getNorth() > 0 && address.getEast() > 0) {
            Layers layers = this.layerManagers.get(0).getLayers(); //this.mapViews.get(0).getLayerManager().getLayers()
            if (layers != null) {
                if (index >= 0 && index < layers.size()) layers.remove(index);
                float circleSize = 6 * this.mapViews.get(0).getModel().displayModel.getScaleFactor();
                Paint paint = Utils.createPaint(AndroidGraphicFactory.INSTANCE.createColor(Color.BLACK), 0, Style.FILL); //android.graphics.Paint.ANTI_ALIAS_FLAG);
                paint.setTextAlign(Align.LEFT);
                paint.setTextSize(25f);
                LatLong position = new LatLong(address.getNorth(), address.getEast());
                StringBuilder fullAddress = new StringBuilder();
                if (address.getCity() != null) fullAddress.append(address.getCity()).append(" ");
                if (address.getRegion() != null) fullAddress.append(address.getRegion()).append(" ");
                if (address.getAddress() != null) fullAddress.append(address.getAddress()).append(" ");
                TextCircle circle = new TextCircle(position, circleSize, fullAddress.toString(), paint, GREEN, null);
                circle.setOffsetX(10);
                layers.add(circle);
                index = layers.size() - 1;
                circle.requestRedraw();
                this.mapViews.get(0).getModel().mapViewPosition.setCenter(position);
            }
        }
    }

    @Click
    void okButton() {
        //setResult(Activity.RESULT_OK, new Intent().putExtra("newRequest", this.newRequest));
        finish();
    }

    @Override
    public void finish() {
        //Log.i(TAG, "Address:" + newRequest.getFullAddress());
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, new Intent().putExtra("mapRequest", this.mapRequest));
        } else {
            getParent().setResult(Activity.RESULT_OK, new Intent().putExtra("mapRequest", this.mapRequest));
        }
        super.finish();
    }
}
