package com.opentaxi.android;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import com.opentaxi.rest.RestClient;
import org.androidannotations.annotations.*;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/8/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
@EActivity(R.layout.lostpassword)
public class LostPasswordActivity extends Activity {

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
        if (result == null) userEmailField.setError("Проверете интернет връзката");
        else if (result) finish();
        else userEmailField.setError("Потребител с имаил:" + userEmailField.getText().toString() + " не съществува.");
    }
}
