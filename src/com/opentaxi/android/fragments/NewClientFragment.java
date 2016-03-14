package com.opentaxi.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.*;
import com.opentaxi.android.R;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.models.NewCUsers;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Contact;
import com.stil.generated.mysql.tables.pojos.Contactaddress;
import com.stil.generated.mysql.tables.pojos.Users;
import com.taxibulgaria.enums.CommunicationMethod;
import org.androidannotations.annotations.*;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 4/17/13
 * Time: 10:18 AM
 * developer STANIMIR MARINOV
 */
//@WindowFeature(Window.FEATURE_NO_TITLE)
@EFragment(R.layout.new_client)
public class NewClientFragment extends BaseFragment implements Validator.ValidationListener {

    private static final String TAG = "NewClientFragment";

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

    @TextRule(order = 5, minLength = 1, messageResId = R.string.city_required)
    @ViewById
    AutoCompleteTextView cityName;

    @TextRule(order = 6, minLength = 1, messageResId = R.string.phone_required)
    @ViewById
    EditText phoneNumber;

    //@Required(order = 5)
    @Email(order = 7, messageResId = R.string.email_not_valid)
    @ViewById(R.id.emailField)
    EditText email;

    //You must agree to the terms
    @Checked(order = 8, messageResId = R.string.agree_required)
    @ViewById
    CheckBox iAgreeCheckBox;

    /* @Extra
     NewCUsers newCUsers;*/
    //private Users users;
    Validator validator;

    private boolean haveErrors = false;

    @AfterViews
    void afterLoad() {

        /*if (newCUsers != null) { //from facebook -> UserPassActivity
            userName.setText(newCUsers.getUsername());
            email.setText(newCUsers.getEmail());
            createNewUser(newCUsers);
        } else {*/
        validator = new Validator(this);
        //validator.put();
        validator.setValidationListener(this);

        String[] cities = new String[]{
                "Бургас", "София", "Варна", "Пловдив", "Burgas", "Sofia", "Varna", "Plovdiv", "Несебър", "Nesebar", "Слънчев бряг", "Sunny beach", "Приморско", "Primorsko", "Царево", "Carevo", "Созопол", "Sozopol", "Разград", "Razgrad", "Монтана", "Montana", "Враца", "Vratsa", "Добрич", "Dobrich", "Русе", "Ruse", "Плевен", "Pleven", "Перник", "Pernik", "Пазарджик", "Pazardzhik", "Ловеч", "Lovech", "Хасково", "Haskovo", "Благоевград", "Blagoevgrad", "Габрово", "Gabrovo", "Кърджали", "Kurdzhali", "Кюстендил", "Kyustendil", "Шумен", "Shumen", "Силистра", "Silistra", "Сливен", "Sliven", "Смолян", "Smolyan", "Стара Загора", "Stara Zagora", "Търговище", "Turgovishte", "Велико Търново", "Veliko Turnovo", "Видин", "Vidin", "Ямбол", "Yambol"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, R.layout.spinner_layout, cities);
        adapter.setDropDownViewResource(R.layout.spinner_layout);
        cityName.setAdapter(adapter);

        haveErrors = false;
        // }
    }

    @Click
    void userAgreement() {
        //todo show it from server
    }

    @Click
    void sendButton() {
        if (validator != null) {
            userName.setError(null);
            pass.setError(null);
            cityName.setError(null);
            phoneNumber.setError(null);
            email.setError(null);

            validator.validate();
        }
    }

    @FocusChange({R.id.userNameField})
    void focusChangedOnUserNameField(View userField, boolean hasFocus) {
        if (!hasFocus) {
            userName.setError(null);
            checkUsername(userName.getText().toString());
        }
    }

    @Background
    void checkUsername(String username) {
        Boolean exist = RestClient.getInstance().checkUserExist(username);
        if (exist != null && exist) setUserError(mActivity.getString(R.string.username_exist));
        else {
            haveErrors = false;
        }
    }

    @FocusChange({R.id.emailField})
    void focusChangedOnEmailField(View emailField, boolean hasFocus) {
        if (!hasFocus) {
            email.setError(null);
            checkEmail(email.getText().toString());
        }
    }

    @Background
    void checkEmail(String emailCheck) {
        Boolean exist = RestClient.getInstance().checkEmailExist(emailCheck);
        if (exist != null && exist) setEmailError(mActivity.getString(R.string.email_exist));
        else {
            haveErrors = false;
        }
    }

    @Background
    void createNewUser(NewCUsers users) {
        Log.i(TAG, "createNewUser:" + users.getUsername());
        Users userPojo = RestClient.getInstance().createNewUser(users);
        if (userPojo != null) {
            if (userPojo.getRecordstatus() != null && userPojo.getRecordstatus()) {  //account is already active (facebook)
                RestClient.getInstance().setAuthHeaders(userPojo.getUsername(), userPojo.getPassword());
                //facebook user exist
                if (AppPreferences.getInstance() != null)
                    AppPreferences.getInstance().setUsers(new com.opentaxi.models.Users(userPojo));
                finishThis();
            } else ActivationDialog();
        } else setUserError("Error, check you internet connection and try again.");
    }

    @UiThread
    void finishThis() {
        if (mListener != null) mListener.startHome();
    }

    @UiThread
    void setUserError(String error) {
        if (userName != null) userName.setError(error);
        haveErrors = true;
    }

    @UiThread
    void setEmailError(String error) {
        if (email != null) email.setError(error);
        haveErrors = true;
    }

    @Override
    public void onValidationSucceeded() {
        Log.i(TAG, "onValidationSucceeded");
        if (!haveErrors) {
            NewCUsers users = new NewCUsers();
            users.setUsername(userName.getText().toString());
            users.setPassword(pass.getText().toString());
            users.setEmail(email.getText().toString());
            users.setPasswordhint(passwordHint.getText().toString());

            Contact contact = new Contact();
            contact.setFirstname(nameField.getText().toString());
            contact.setMiddlename(middleName.getText().toString());
            contact.setLastname(lastName.getText().toString());
            users.setContact(contact);

            if (!phoneNumber.getText().toString().isEmpty()) {
             /*else {
                TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                mPhoneNumber = manager.getLine1Number();
            }*/
                com.stil.generated.mysql.tables.pojos.CommunicationMethod communication = new com.stil.generated.mysql.tables.pojos.CommunicationMethod();
                communication.setContactData(phoneNumber.getText().toString());
                communication.setMethodType(CommunicationMethod.PHONE.getCode());
                users.setCommunication(communication);
            }

            Contactaddress address = new Contactaddress();
            address.setCity(cityName.getText().toString());
            users.setAddress(address);
            createNewUser(users);
        } else {
            if (userName != null) checkUsername(userName.getText().toString());
            Log.e(TAG, "haveErrors = true");
        }
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        String message = failedRule.getFailureMessage();

        if (failedView instanceof EditText) {
            failedView.requestFocus();
            ((EditText) failedView).setError(message);
        } else {
            Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
        }
    }

    @UiThread
    void ActivationDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
        alertDialogBuilder.setTitle(mActivity.getString(R.string.email_confirmation));
        alertDialogBuilder.setMessage(mActivity.getString(R.string.new_account_confirmation, email.getText().toString()));

        alertDialogBuilder.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finishThis();
            }
        });

        Dialog exitDialog = alertDialogBuilder.create();
        exitDialog.show();
    }
}
