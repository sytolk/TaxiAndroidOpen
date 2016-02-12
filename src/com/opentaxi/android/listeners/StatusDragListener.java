package com.opentaxi.android.listeners;

import android.view.DragEvent;
import android.view.View;

/**
 * Created by stanimir on 1/13/16.
 */
public class StatusDragListener implements View.OnDragListener {

    @Override
    public boolean onDrag(View v, DragEvent event) {

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                //Log.i("onDrag", "ACTION_DRAG_STARTED");
                break;
            case DragEvent.ACTION_DRAG_ENTERED:
                //Log.i("onDrag", "ACTION_DRAG_ENTERED");
                break;
            case DragEvent.ACTION_DRAG_EXITED:
                //Log.i("onDrag", "ACTION_DRAG_EXITED");
                break;
            case DragEvent.ACTION_DROP:
                //Log.i("onDrag", "ACTION_DROP");
                int x_cord = (int) event.getX();
                int y_cord = (int) event.getY();
                View view = (View) event.getLocalState();
                view.setX(x_cord-(view.getWidth()/2));
                view.setY(y_cord-(view.getHeight()/2));
                view.setVisibility(View.VISIBLE);
                break;
            case DragEvent.ACTION_DRAG_ENDED:
                //Log.i("onDrag", "ACTION_DRAG_ENDED");
            default:
                break;
        }
        return true;
    }
}