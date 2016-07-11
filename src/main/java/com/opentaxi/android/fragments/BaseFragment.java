package com.opentaxi.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by stanimir on 2/4/16.
 */
public class BaseFragment extends Fragment {

    protected Activity mActivity;

    protected OnCommandListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Activity) {
            try {
                mListener = (OnCommandListener) context;
            } catch (ClassCastException e) {
                throw new ClassCastException(context.toString() + " must implement OnRequestEventsListener");
            }
            mActivity = (Activity) context;
        }
    }
}
