/*
 * Copyright 2010, 2011, 2012, 2013 mapsforge.org
 * Copyright 2014 Ludwig M Brinckmann
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

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.layer.Layer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarkerOverlay extends Layer {

    private final List<Layer> overlayItems = Collections.synchronizedList(new ArrayList<Layer>());

    public MarkerOverlay() {
        super();
    }

    @Override
    public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
        synchronized (this.overlayItems) {
            for (Layer marker : this.overlayItems) {
                marker.draw(boundingBox, zoomLevel, canvas, topLeftPoint);
            }
        }
    }

    @Override
    public void onDestroy() {
        synchronized (this.overlayItems) {
            for (Layer marker : this.overlayItems) {
                marker.onDestroy();
            }
        }
    }

    /**
     * @return a synchronized (thread-safe) list of all {@link Layer OverlayItems} on this {@code ListOverlay}.
     *         Manual synchronization on this list is necessary when iterating over it.
     */
    public List<Layer> getOverlayItems() {
        synchronized (this.overlayItems) {
            return this.overlayItems;
        }
    }
}
