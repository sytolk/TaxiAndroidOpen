package org.mapsforge.applications.android.mapbg;

import org.mapsforge.core.graphics.Canvas;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.model.Rectangle;
import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.layer.overlay.Circle;

public class TextCircle extends Circle {

    private String text;
    private Paint paintText;
    private int offsetX = 0;
    private int offsetY = 0;

    /**
     * @param latLong     the initial center point of this circle (may be null).
     * @param radius      the initial non-negative radius of this circle in meters.
     * @param paintFill   the initial {@code Paint} used to fill this circle (may be null).
     * @param paintStroke the initial {@code Paint} used to stroke this circle (may be null).
     * @throws IllegalArgumentException if the given {@code radius} is negative or {@link Float#NaN}.
     */
    public TextCircle(LatLong latLong, float radius, String txt, Paint paintText, Paint paintFill, Paint paintStroke) {
        super(latLong, radius, paintFill, paintStroke);
        this.paintText = paintText;
        this.text = txt;
    }

    @Override
    public synchronized void draw(BoundingBox boundingBox, byte zoomLevel, Canvas canvas, Point topLeftPoint) {
        if (getPosition() == null || (getPaintStroke() == null && getPaintFill() == null)) {
            return;
        }

        double latitude = getPosition().latitude;
        double longitude = getPosition().longitude;
        int tileSize = displayModel.getTileSize();
        int pixelX = (int) (MercatorProjection.longitudeToPixelX(longitude, zoomLevel, tileSize) - topLeftPoint.x);
        int pixelY = (int) (MercatorProjection.latitudeToPixelY(latitude, zoomLevel, tileSize) - topLeftPoint.y);
        int radiusInPixel = getRadiusInPixels(latitude, zoomLevel);

        Rectangle canvasRectangle = new Rectangle(0, 0, canvas.getWidth(), canvas.getHeight());
        if (!canvasRectangle.intersectsCircle(pixelX, pixelY, radiusInPixel)) {
            return;
        }

        if (getPaintStroke() != null) {
            canvas.drawCircle(pixelX, pixelY, radiusInPixel, getPaintStroke());
        }
        if (getPaintFill() != null) {
            canvas.drawCircle(pixelX, pixelY, radiusInPixel, getPaintFill());
        }
        if (this.paintText != null) {
            canvas.drawText(text, pixelX + offsetX, pixelY + offsetY, this.paintText);
        }
    }

    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }
}
