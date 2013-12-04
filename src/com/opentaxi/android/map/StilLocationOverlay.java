package com.opentaxi.android.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.location.*;
import android.os.Bundle;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.Circle;
import org.mapsforge.android.maps.overlay.Marker;
import org.mapsforge.android.maps.overlay.Overlay;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 4/12/13
 * Time: 2:55 PM
 * developer STANIMIR MARINOV
 */
public class StilLocationOverlay implements LocationListener, Overlay {
    private static final int UPDATE_DISTANCE = 0;
    private static final int UPDATE_INTERVAL = 1000;

    /**
     * @param location the location whose geographical coordinates should be converted.
     * @return a new GeoPoint with the geographical coordinates taken from the given location.
     */
    public static GeoPoint locationToGeoPoint(Location location) {
        return new GeoPoint(location.getLatitude(), location.getLongitude());
    }

    private static Paint getDefaultCircleFill() {
        return getPaint(Style.FILL, Color.BLUE, 48);
    }

    private static Paint getDefaultCircleStroke() {
        Paint paint = getPaint(Style.STROKE, Color.BLUE, 128);
        paint.setStrokeWidth(2);
        return paint;
    }

    private static Paint getPaint(Style style, int color, int alpha) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(style);
        paint.setColor(color);
        paint.setAlpha(alpha);
        return paint;
    }

    private boolean centerAtNextFix;
    private final Circle circle;
    private Location lastLocation;
    private final LocationManager locationManager;
    private final MapView mapView;
    private final Marker marker;
    private boolean myLocationEnabled;
    private boolean snapToLocationEnabled;

    /**
     * Constructs a new {@code MyLocationOverlay} with the given drawable and the default circle paints.
     *
     * @param context  a reference to the application context.
     * @param mapView  the {@code MapView} on which the location will be displayed.
     * @param drawable a drawable to display at the current location (might be null).
     */
    public StilLocationOverlay(Context context, MapView mapView, Drawable drawable) {
        this(context, mapView, drawable, getDefaultCircleFill(), getDefaultCircleStroke());
    }

    /**
     * Constructs a new {@code MyLocationOverlay} with the given drawable and circle paints.
     *
     * @param context      a reference to the application context.
     * @param mapView      the {@code MapView} on which the location will be displayed.
     * @param drawable     a drawable to display at the current location (might be null).
     * @param circleFill   the {@code Paint} used to fill the circle that represents the current location (might be null).
     * @param circleStroke the {@code Paint} used to stroke the circle that represents the current location (might be null).
     */
    public StilLocationOverlay(Context context, MapView mapView, Drawable drawable, Paint circleFill, Paint circleStroke) {
        this.mapView = mapView;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.marker = new Marker(null, drawable);
        this.circle = new Circle(null, 0, circleFill, circleStroke);
    }

    /**
     * Stops the receiving of location updates. Has no effect if location updates are already disabled.
     */
    public synchronized void disableMyLocation() {
        if (this.myLocationEnabled) {
            this.myLocationEnabled = false;
            this.locationManager.removeUpdates(this);
            this.mapView.getOverlayController().redrawOverlays();
        }
    }

    @Override
    public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas) {
        if (!this.myLocationEnabled) {
            return;
        }

        double canvasPixelLeft = MercatorProjection.longitudeToPixelX(boundingBox.minLongitude, zoomLevel);
        double canvasPixelTop = MercatorProjection.latitudeToPixelY(boundingBox.maxLatitude, zoomLevel);
        Point canvasPosition = new Point(canvasPixelLeft, canvasPixelTop);
        this.circle.draw(boundingBox, zoomLevel, canvas, canvasPosition);
        this.marker.draw(boundingBox, zoomLevel, canvas, canvasPosition);
    }

    /**
     * Enables the receiving of location updates from the most accurate {@link LocationProvider} available.
     *
     * @param centerAtFirstFix whether the map should be centered to the first received location fix.
     * @return true if at least one location provider was found, false otherwise.
     */
    public synchronized boolean enableMyLocation(boolean centerAtFirstFix) {
        if (!enableBestAvailableProvider()) {
            return false;
        }

        this.centerAtNextFix = centerAtFirstFix;
        return true;
    }

    /**
     * @return the most-recently received location fix (might be null).
     */
    public synchronized Location getLastLocation() {
        return this.lastLocation;
    }

    /**
     * @return true if the map will be centered at the next received location fix, false otherwise.
     */
    public synchronized boolean isCenterAtNextFix() {
        return this.centerAtNextFix;
    }

    /**
     * @return true if the receiving of location updates is currently enabled, false otherwise.
     */
    public synchronized boolean isMyLocationEnabled() {
        return this.myLocationEnabled;
    }

    /**
     * @return true if the snap-to-location mode is enabled, false otherwise.
     */
    public synchronized boolean isSnapToLocationEnabled() {
        return this.snapToLocationEnabled;
    }

    @Override
    public void onLocationChanged(Location location) {
        synchronized (this) {
            this.lastLocation = location;

            GeoPoint geoPoint = locationToGeoPoint(location);
            this.marker.setGeoPoint(geoPoint);
            this.circle.setGeoPoint(geoPoint);
            this.circle.setRadius(location.getAccuracy());

            if (this.centerAtNextFix || this.snapToLocationEnabled) {
                this.centerAtNextFix = false;
                this.mapView.getMapViewPosition().setCenter(geoPoint);
            }
        }
        this.mapView.getOverlayController().redrawOverlays();
    }

    @Override
    public void onProviderDisabled(String provider) {
        enableBestAvailableProvider();
    }

    @Override
    public void onProviderEnabled(String provider) {
        enableBestAvailableProvider();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // do nothing
    }

    /**
     * @param snapToLocationEnabled whether the map should be centered at each received location fix.
     */
    public synchronized void setSnapToLocationEnabled(boolean snapToLocationEnabled) {
        this.snapToLocationEnabled = snapToLocationEnabled;
    }

    private synchronized boolean enableBestAvailableProvider() {
        disableMyLocation();

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String bestAvailableProvider = this.locationManager.getBestProvider(criteria, true);
        if (bestAvailableProvider == null) {
            return false;
        }

        this.locationManager.requestLocationUpdates(bestAvailableProvider, UPDATE_INTERVAL, UPDATE_DISTANCE, this);
        this.myLocationEnabled = true;
        return true;
    }

    public synchronized void setDrawable(Drawable drawable) {
        this.marker.setDrawable(drawable);
    }
}