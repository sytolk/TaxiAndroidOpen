/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2013-2014 Ludwig M Brinckmann
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
package org.mapsforge.applications.android.mapbg;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;
import org.mapsforge.applications.android.filepicker.FilePicker;
import org.mapsforge.core.graphics.Bitmap;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.layer.MyLocationOverlay;

public class LocationOverlayMapViewer extends BasicMapViewerXml {
    private MyLocationOverlay myLocationOverlay;
    private static final int SELECT_MAP_FILE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == SELECT_MAP_FILE) {
            if (resultCode == RESULT_OK) {
                this.myLocationOverlay.setSnapToLocationEnabled(false);
                if (intent != null && intent.getStringExtra(FilePicker.SELECTED_FILE) != null) {
                    mapFileName = intent.getStringExtra(FilePicker.SELECTED_FILE);
                    Log.i("L onActivityResult", "mapFileName:" + mapFileName);
                    preferencesFacade.putString(PREFERENCE_MAP_PATH, mapFileName);
                    preferencesFacade.save();
                    redrawLayers();
                } else Log.e("L onActivityResult", "intent:" + intent);
            } else if (resultCode == RESULT_CANCELED && mapFileName == null) {
                Log.e("L onActivityResult", "resultCode:" + resultCode);
                finish();
            } else Log.e("L onActivityResult", "resultCode:" + resultCode);
        } else Log.e(TAG, "requestCode:" + requestCode);
    }

    @Override
    public void onPause() {
        myLocationOverlay.disableMyLocation();
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        this.myLocationOverlay.enableMyLocation(true);
    }

    @Override
    protected void createLayers() {
        super.createLayers();
        // a marker to show at the position
        Drawable drawable = getResources().getDrawable(R.drawable.ic_maps_indicator_current_position_anim1);
        Bitmap my_position = AndroidGraphicFactory.convertToBitmap(drawable);
        // create the overlay and tell it to follow the location
        this.myLocationOverlay = new MyLocationOverlay(this, this.mapViewPositions.get(0), my_position);
        this.myLocationOverlay.setSnapToLocationEnabled(true);
        this.layerManagers.get(0).getLayers().add(this.myLocationOverlay);

        ToggleButton snapToLocationView = (ToggleButton) findViewById(R.id.snapToLocationView);
        snapToLocationView.setVisibility(View.VISIBLE);
        snapToLocationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invertSnapToLocation();
            }
        });
    }

    void invertSnapToLocation() {
        if (this.myLocationOverlay != null) {
            if (this.myLocationOverlay.isSnapToLocationEnabled()) {
                this.myLocationOverlay.setSnapToLocationEnabled(false);
            } else this.myLocationOverlay.setSnapToLocationEnabled(true);
        }
    }
}
