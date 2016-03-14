package com.opentaxi.android.fragments;

import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import com.opentaxi.android.R;
import com.opentaxi.rest.RestClient;
import org.androidannotations.annotations.*;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/8/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
@EFragment(R.layout.lostpassword)
public class LostPasswordFragment extends BaseFragment {

    @ViewById
    EditText userEmailField;

    @AfterViews
    void afterLoad() {

        userEmailField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    lostPass(userEmailField.getText().toString());

                    //return true; //this will keep keyboard open
                }

                return false;
            }
        });
    }

    @Background
    void lostPass(String email) {
        result(RestClient.getInstance().lostPassword(email));
    }

    @UiThread
    void result(Boolean result) {
        if (result == null) userEmailField.setError(mActivity.getString(R.string.check_internet));
        else if (result) {
            if (mListener != null) mListener.startHome();
        } else userEmailField.setError(mActivity.getString(R.string.user_not_exist, userEmailField.getText().toString()));
    }

    @Click
    void sendButton(){
        lostPass(userEmailField.getText().toString());
    }
}
