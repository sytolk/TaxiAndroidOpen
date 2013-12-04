package com.opentaxi.android.utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.Map;


public class KeyValueAdapter extends BaseAdapter implements SpinnerAdapter {

    private final Context _context;
    private final Map<Integer, String> _data;
    private final Integer[] _keys;


    public KeyValueAdapter(Context context, int textViewResourceId, Map<Integer, String> objects) {
        _context = context;
        _data = objects;

        //get positions
        int i = 0;
        _keys = new Integer[_data.size()];

        for (Integer key : _data.keySet()) {
            _keys[i++] = key;
        }
    }


    public int getCount() {
        return _data.size();
    }


    public int getPositionFromKey(Integer searchKey) {
        for (int i = 0; i < _keys.length; i++) {
            if (_keys[i].equals(searchKey))
                return i;
        }
        return -1;
    }


    public String getItem(int position) {
        return _data.get(_keys[position]);
    }


    public long getItemId(int position) {
        /*
        * I happened to be using long keys so I modified this function. you can leave it at:
        *  return position;
        */
        if (position >= _keys.length || position < 0) {
            return -1;
        }

        return _keys[position];
    }


    public View getView(int position, View view, ViewGroup parent) {

        final TextView text = new TextView(_context);
        text.setTextColor(Color.BLACK);
        text.setText(getItem(position));

        return text;
    }

}
