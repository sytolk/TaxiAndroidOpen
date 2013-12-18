package com.opentaxi.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.*;
import com.opentaxi.generated.mysql.tables.pojos.Contact;
import com.opentaxi.generated.mysql.tables.pojos.Contactaddress;
import com.opentaxi.generated.mysql.tables.pojos.Users;
import com.opentaxi.models.NewUsers;
import com.opentaxi.rest.RestClient;
import com.taxibulgaria.enums.CommunicationMethod;
import org.androidannotations.annotations.*;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 4/17/13
 * Time: 10:18 AM
 * developer STANIMIR MARINOV
 */
@WindowFeature(Window.FEATURE_NO_TITLE)
@EActivity(R.layout.new_client)
public class NewClientActivity extends FragmentActivity implements Validator.ValidationListener {

    private static final String TAG = "NewClientActivity";

    @ViewById
    Button sendButton;

    @TextRule(order = 1, minLength = 3, message = "Username is too short.Enter at least 3 characters.")
    @ViewById(R.id.userNameField)
    EditText userName;

    @Password(order = 2)
    @TextRule(order = 3, minLength = 6, message = "Password is too short.Enter at least 6 characters.")
    @ViewById(R.id.passwordField)
    EditText pass;

    @ConfirmPassword(order = 4)
    @ViewById(R.id.password2Field)
    EditText pass2;

    @ViewById
    EditText passwordHint;

    @ViewById
    EditText nameField;

    @ViewById
    EditText middleName;

    @ViewById
    EditText lastName;

    @TextRule(order = 5, minLength = 1, message = "Града е задължително поле.")
    @ViewById
    AutoCompleteTextView cityName;

    @TextRule(order = 6, minLength = 1, message = "Телефона е задължително поле.")
    @ViewById
    EditText phoneNumber;

    //@Required(order = 5)
    @Email(order = 7, message = "Невалиден email адрес.")
    @ViewById(R.id.emailField)
    EditText email;

    //You must agree to the terms
    @Checked(order = 8, message = "Трябва да премете условията за ползване.")
    @ViewById
    CheckBox iAgreeCheckBox;

    @Extra
    NewUsers newUsers;
    //private Users users;
    Validator validator;

    private boolean haveErrors = false;

    @AfterViews
    void afterLoad() {

        if (newUsers != null) {
            userName.setText(newUsers.getUsername());
            email.setText(newUsers.getEmail());
            createNewUser(newUsers);
        } else {
            validator = new Validator(this);
            //validator.put();
            validator.setValidationListener(this);

            String[] cities = new String[]{
                    "Бургас", "София", "Варна", "Пловдив", "Burgas", "Sofia", "Varna", "Plovdiv"
            };
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_layout, cities);
            adapter.setDropDownViewResource(R.layout.spinner_layout);
            cityName.setAdapter(adapter);

            haveErrors = false;
        }
    }

    @Click
    void userAgreement() {
        //todo show it from server
    }

    @Click
    void sendButton() {
        if (validator != null)
            validator.validate();
    }

    @FocusChange({R.id.userNameField})
    void focusChangedOnUserNameField(View userField, boolean hasFocus) {
        if (!hasFocus) {
            checkUsername(userName.getText().toString());
        }
    }

    @Background
    void checkUsername(String username) {
        Boolean exist = RestClient.getInstance().checkUserExist(username);
        if (exist != null && exist) setUserError("Потребителското име съществува");
        else haveErrors = false;
    }

    @FocusChange({R.id.emailField})
    void focusChangedOnEmailField(View emailField, boolean hasFocus) {
        if (!hasFocus) {
            checkEmail(email.getText().toString());
        }
    }

    @Background
    void checkEmail(String email) {
        Boolean exist = RestClient.getInstance().checkEmailExist(email);
        if (exist != null && exist) setEmailError("Този имеил съществува");
        else haveErrors = false;
    }

    @Background
    void createNewUser(NewUsers users) {
        Log.i(TAG, "createNewUser:" + users.getUsername());
        Users userPojo = RestClient.getInstance().createNewUser(users);
        if (userPojo != null) {
            if (userPojo.getRecordstatus() != null && userPojo.getRecordstatus()) {  //account is already active
                RestClient.getInstance().setAuthHeadersEncoded(userPojo.getUsername(), userPojo.getPassword());
                finishThis();
            } else ActivationDialog();
        } else setUserError("Error, check you internet connection and try again.");
    }

    @UiThread
    void finishThis() {
        finish();
    }

    @UiThread
    void setUserError(String error) {
        userName.setError(error);
        haveErrors = true;
    }

    @UiThread
    void setEmailError(String error) {
        email.setError(error);
        haveErrors = true;
    }

    @Override
    public void onValidationSucceeded() {
        Log.i(TAG, "onValidationSucceeded");
        if (!haveErrors) {
            NewUsers users = new NewUsers();
            users.setUsername(userName.getText().toString());
            users.setPassword(pass.getText().toString());
            users.setEmail(email.getText().toString());
            users.setPasswordhint(passwordHint.getText().toString());

            Contact contact = new Contact();
            contact.setFirstname(nameField.getText().toString());
            contact.setMiddlename(middleName.getText().toString());
            contact.setLastname(lastName.getText().toString());
            users.setContact(contact);

            String mPhoneNumber;
            if (!phoneNumber.getText().toString().equals("")) {
                mPhoneNumber = phoneNumber.getText().toString();
            } else {
                TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                mPhoneNumber = manager.getLine1Number();
            }
            if (mPhoneNumber != null) {
                com.opentaxi.generated.mysql.tables.pojos.CommunicationMethod communication = new com.opentaxi.generated.mysql.tables.pojos.CommunicationMethod();
                communication.setContactData(mPhoneNumber);
                communication.setMethodType(CommunicationMethod.PHONE.getCode());
                users.setCommunication(communication);
            }

            Contactaddress address = new Contactaddress();
            address.setCity(cityName.getText().toString());
            users.setAddress(address);
            createNewUser(users);
        } else Log.e(TAG, "haveErrors = true");
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
    }

    @UiThread
    void ActivationDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Потвърди емайла");
        alertDialogBuilder.setMessage("Вашият акунт е създаден успешно но е нужно да потвърдите имаила си (" + email.getText().toString() + "). Това може да стане като кликнете на линка който ви изпратихме на:" + email.getText().toString());

        alertDialogBuilder.setNeutralButton("ОК", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        Dialog exitDialog = alertDialogBuilder.create();

        // If Google Play services can provide an error dialog
        if (exitDialog != null) {
            try {
                // Create a new DialogFragment for the error dialog
                MainDialogFragment errorFragment = new MainDialogFragment();
                // Set the dialog in the DialogFragment
                errorFragment.setDialog(exitDialog);
                // Show the error dialog in the DialogFragment
                errorFragment.show(getSupportFragmentManager(), "EmailVerify");
            } catch (Exception e) {
                if (e.getMessage() != null) Log.e(TAG, e.getMessage());
            }
        }
    }

    public static class MainDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public MainDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (mDialog == null) super.setShowsDialog(false);
            return mDialog;
        }
    }
}
