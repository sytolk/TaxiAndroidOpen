package com.opentaxi.android;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.models.Users;
import com.opentaxi.rest.RestClient;
import org.androidannotations.annotations.*;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/8/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
@EActivity(R.layout.login)
public class UserPassActivity extends Activity implements Validator.ValidationListener {

    private static final int RESULT_NEW_CLIENT = 1;

    @ViewById(R.id.clientLoginButton)
    Button submitButton;

    @TextRule(order = 1, minLength = 3, message = "Username is too short.Enter at least 3 characters.")
    @ViewById(R.id.userNameField)
    EditText userName;

    @TextRule(order = 2, minLength = 6, message = "Password is too short.Enter at least 6 characters.")
    @ViewById(R.id.passwordField)
    EditText pass;

    //private Users users;
    Validator validator;


    @AfterViews
    void afterLoad() {
        submitButton.setClickable(true);

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

   /* @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(Users.class.getName(), users);

        if (getParent() == null) {
            setResult(Activity.RESULT_OK, data);
        } else {
            getParent().setResult(Activity.RESULT_OK, data);
        }
        super.finish();
    }*/

    @OnActivityResult(RESULT_NEW_CLIENT)
    void onResult(int resultCode, Intent data) {

    }

    @Click
    void clientLoginButton() {
        submitButton.setClickable(false);
        validator.validateAsync();
    }

    @Click
    void newClient() {
        Log.i("newClient", "newClient");

        NewClientActivity_.intent(this).startForResult(RESULT_NEW_CLIENT);
    }

    @Click
    void lostPassword() {
        Log.i("lostPassword", "lostPassword");
    }

    @Background
    void login(String username, String password) {

        Log.i("Login", "user:" + username + " pass:" + password);
        Users user = RestClient.getInstance().Login(username, password);

        if (user != null) {
            if (user.getId() != null && user.getId() > 0) {
                //users = user;
                //AppPreferences.getInstance().setUsers(user);
                String userEncrypt = AppPreferences.getInstance().encrypt(username, "user_salt");
                String passEncrypt = AppPreferences.getInstance().encrypt(password, username);
                RestClient.getInstance().saveAuthorization(userEncrypt, passEncrypt);
                //Toast.makeText(UserPassActivity.this, "Влязохте в системата успешно!", Toast.LENGTH_LONG).show();
                finish();
            } else setError("Грешно потребителско име или парола!");
            // Toast.makeText(UserPassActivity.this, "Грешно потребителско име или парола!", Toast.LENGTH_LONG).show();

        } else setError("Грешка! Сигурни ли сте че имате връзка с интернет?");
        //Toast.makeText(UserPassActivity.this, "Грешка! Сигурни ли сте че имате връзка с интернет?", Toast.LENGTH_LONG).show();
    }

    @UiThread
    void setError(String error) {
        pass.setError(error);
        submitButton.setClickable(true);
    }

    @Override
    public void onValidationSucceeded() {
        login(userName.getText().toString(), pass.getText().toString());
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        String message = failedRule.getFailureMessage();

        if (failedView instanceof EditText) {
            failedView.requestFocus();
            ((EditText) failedView).setError(message);
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
        submitButton.setClickable(true);
    }
}
