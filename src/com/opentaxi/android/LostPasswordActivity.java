package com.opentaxi.android;

import android.app.Activity;
import android.widget.EditText;
import com.opentaxi.rest.RestClient;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

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

    @Click
    void lostPassButton(){
        lostPass(userEmailField.getText().toString());
    }

    @Background
    void lostPass(String email) {
        RestClient.getInstance().lostPassword(email);
    }

}
