package com.opentaxi.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.opentaxi.android.simplefacebook.Permissions;
import com.opentaxi.android.simplefacebook.SimpleFacebook;
import com.opentaxi.android.simplefacebook.SimpleFacebookConfiguration;
import com.opentaxi.android.simplefacebook.entities.Profile;
import com.opentaxi.android.simplefacebook.entities.Work;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.generated.mysql.tables.pojos.Contact;
import com.opentaxi.generated.mysql.tables.pojos.Contactaddress;
import com.opentaxi.generated.mysql.tables.pojos.FacebookUsers;
import com.opentaxi.models.NewUsers;
import com.opentaxi.models.Users;
import com.opentaxi.rest.RestClient;
import com.taxibulgaria.enums.Gender;
import org.androidannotations.annotations.*;

import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/8/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
@EActivity(R.layout.login)
public class UserPassActivity extends FragmentActivity implements Validator.ValidationListener {

    private static final String TAG = "UserPassActivity";

    private static final int RESULT_NEW_CLIENT = 1;
    private static final int RESULT_LOST_PASSWORD = 2;

    /*@ViewById(R.id.clientLoginButton)
    Button submitButton;*/

    @TextRule(order = 1, minLength = 3, message = "Username is too short.Enter at least 3 characters.")
    @ViewById(R.id.userNameField)
    EditText userName;

    @TextRule(order = 2, minLength = 6, message = "Password is too short.Enter at least 6 characters.")
    @ViewById(R.id.passwordField)
    EditText pass;

    @ViewById(R.id.pbProgress)
    ProgressBar pbProgress;

    Validator validator;

    SimpleFacebook mSimpleFacebook;
    private int result = Activity.RESULT_OK;

    private static final int SERVER_CHANGE = 12;

    @Override
    public void onBackPressed() {
        result = Activity.RESULT_CANCELED;
        super.onBackPressed();
    }

    @Override
    public void finish() {
        TaxiApplication.userPassPaused();
        if (result != Activity.RESULT_CANCELED) result = Activity.RESULT_OK;
        if (getParent() == null) {
            setResult(result);
        } else {
            getParent().setResult(result);
        }
        super.finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        TaxiApplication.userPassPaused();
    }

    @Override
    public void onResume() {
        super.onResume();
        TaxiApplication.userPassResumed();
        if (pbProgress != null) pbProgress.setVisibility(View.GONE);
    }

    /*@InstanceState
    Bundle savedInstanceState;

    private Session.StatusCallback statusCallback = new SessionStatusCallback();



    @Override
    public void onStart() {
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }*/

    @AfterViews
    void afterLoad() {
        //submitButton.setClickable(true);
        result = Activity.RESULT_OK;
        validator = new Validator(this);
        validator.setValidationListener(this);

        pass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    validator.validateAsync();
                    //return true; //this will keep keyboard open
                }

                return false;
            }
        });
        /*Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(this, null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(this);
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.options_server:
                Intent intent = new Intent(this, ServersActivity_.class);
                startActivityForResult(intent, SERVER_CHANGE);
                return true;

            case R.id.options_exit:

                finish();
                return true;
            case R.id.options_send_log:

                String javaTmpDir = System.getProperty("java.io.tmpdir");
                File cacheDir = new File(javaTmpDir, "DiskLruCacheDir");
                if (cacheDir.exists()) {
                    File[] files = cacheDir.listFiles();
                    if (files != null)
                        for (File file : files) {
                            file.delete();
                        }
                }
                int i = 2 / 0;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSimpleFacebook != null)
            mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*private class SessionStatusCallback implements Session.StatusCallback {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            Log.i("LoginUsingLoginFragmentActivity:" + session.getAccessToken(), String.format("New session state: %s", state.toString()));
        }
    }*/

    /*@OnActivityResult(RESULT_NEW_CLIENT)
    void onResult(int resultCode, Intent data) {

    }*/

    /*@Click
    void clientLoginButton() {
        submitButton.setClickable(false);
        validator.validateAsync();
    }*/

    @Click
    void facebookButton() {

        mSimpleFacebook = SimpleFacebook.getInstance(this); // Permissions.USER_BIRTHDAY
        Permissions[] permissions = new Permissions[]{Permissions.EMAIL, Permissions.USER_WEBSITE, Permissions.USER_WORK_HISTORY, Permissions.USER_ABOUT_ME, Permissions.USER_HOMETOWN};
        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                //.setAppId("550947981660612")
                .setNamespace("taxi-bulgaria")
                .setPermissions(permissions)
                .build();
        SimpleFacebook.setConfiguration(configuration);

        SimpleFacebook.OnLoginListener onLoginListener = new SimpleFacebook.OnLoginListener() {

            @Override
            public void onLogin() {

                Log.i(TAG, "onLogin");
                if (AppPreferences.getInstance() != null)
                    AppPreferences.getInstance().setAccessToken(mSimpleFacebook.getAccessToken());  //todo move this to disk cache
                checkFacebook(mSimpleFacebook.getAccessToken());
            }

            @Override
            public void onNotAcceptingPermissions() {
                Log.e(TAG, "onNotAcceptingPermissions token:" + mSimpleFacebook.getAccessToken());
                overFacebookLoginTime();
            }

            @Override
            public void onThinking() {
                Log.i(TAG, "onThinking");
                pbProgress.setVisibility(View.VISIBLE);
                maxFacebookLoginTime();
            }

            @Override
            public void onException(Throwable throwable) {
                Log.e(TAG, "onException:" + throwable.getMessage());
                overFacebookLoginTime();
                //facebookLogout();
            }

            @Override
            public void onFail(String reason) {
                Log.e(TAG, "onFail:" + reason);
                overFacebookLoginTime();
            }
        };

        mSimpleFacebook.login(onLoginListener);
        Log.i(TAG, "mSimpleFacebook.login:" + mSimpleFacebook.getAccessToken());

        /*Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(this, true, statusCallback);
        }*/
    }

    @Background(delay = 15000)
    void maxFacebookLoginTime() {
        if (mSimpleFacebook.getAccessToken() == null || mSimpleFacebook.getAccessToken().equals("")) {
            overFacebookLoginTime();
        }
    }

    @UiThread
    void overFacebookLoginTime() {
        if (TaxiApplication.isUserPassVisible()) {
            TaxiApplication.userPassPaused();
            pbProgress.setVisibility(View.GONE);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Времето за вход през Facebook изтече");
            alertDialogBuilder.setMessage("Възможно е вашето устройство да не се поддържа от Facebook. Искате ли да създадете свой потребителски акаунт в системата на Taxi Bulgaria ?");
            //null should be your on click listener
            alertDialogBuilder.setPositiveButton("ДА", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    newClient();
                }
            });
            alertDialogBuilder.setNegativeButton("НЕ", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    TaxiApplication.userPassResumed();
                }
            });

            Dialog facebookDialog = alertDialogBuilder.create();

            if (facebookDialog != null) {
                try {
                    // Create a new DialogFragment for the error dialog
                    MainDialogFragment errorFragment = new MainDialogFragment();
                    // Set the dialog in the DialogFragment
                    errorFragment.setDialog(facebookDialog);
                    // Show the error dialog in the DialogFragment
                    errorFragment.show(getSupportFragmentManager(), "FacebookDialog");
                } catch (Exception e) {
                    if (e.getMessage() != null) Log.e(TAG, e.getMessage());
                }
            }
        }
    }

    // Define a DialogFragment that displays the error dialog
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


    @Background
    void checkFacebook(String token) {
        Log.i(TAG, "checkFacebook token:" + token);
        facebookUser(RestClient.getInstance().FacebookLogin(token));
    }

    @UiThread
    void facebookUser(Users user) {
        if (user != null) { //user already exist in taxi-bulgaria service
            Log.i(TAG, "facebookUser user:" + user.getUsername());
            if (user.getId() != null && user.getId() > 0) {
                RestClient.getInstance().setAuthHeadersEncoded(user.getUsername(), user.getPassword());
                AppPreferences.getInstance().setUsers(user);
                finish();
            } else setError("Грешно потребителско име или парола!");

            //facebookLogout();
        } else { //new Facebook User
            Log.i(TAG, "New facebookUser");
            SimpleFacebook.OnProfileRequestListener onProfileRequestListener = new SimpleFacebook.OnProfileRequestListener() {

                @Override
                public void onFail(String reason) {
                    Log.i(TAG, "New facebookUser onFail");
                    overFacebookLoginTime();
                }

                @Override
                public void onException(Throwable throwable) {
                    Log.i(TAG, "New facebookUser onException");
                    //facebookLogout();
                    overFacebookLoginTime();
                }

                @Override
                public void onThinking() {
                    Log.i(TAG, "New facebookUser onThinking");
                    pbProgress.setVisibility(View.VISIBLE);
                    maxFacebookLoginTime();
                }

                @Override
                public void onComplete(Profile profile) {
                    Log.i(TAG, "New facebookUser onComplete");
                    if (profile != null) { //&& profile.getVerified()) {
                        NewUsers users = new NewUsers();
                        users.setUsername(profile.getUsername());
                        users.setEmail(profile.getEmail());
                        users.setImage(profile.getPicture());

                        Contact contact = new Contact();
                        contact.setFirstname(profile.getFirstName());
                        contact.setMiddlename(profile.getMiddleName());
                        contact.setLastname(profile.getLastName());
                        contact.setNotes(profile.getBio());
                        if (profile.getWork() != null) {
                            for (Work work : profile.getWork()) {
                                String workTitle = work.getEmployer() + " " + work.getDescription();
                                if (contact.getJobtitle() != null)
                                    contact.setJobtitle(contact.getJobtitle() + " " + workTitle);
                                else contact.setJobtitle(workTitle);
                            }
                        }
                        if (profile.getGender() != null) {
                            if (profile.getGender().equals("male"))
                                contact.setGender(Gender.MALE.getCode());
                            else if (profile.getGender().equals("female")) contact.setGender(Gender.FEMALE.getCode());
                        }

                        users.setContact(contact);

                        Contactaddress address = new Contactaddress();
                        address.setCity(profile.getHometown());
                        users.setAddress(address);

                        FacebookUsers facebookUsers = new FacebookUsers();
                        facebookUsers.setFacebookId(Long.parseLong(profile.getId()));
                        facebookUsers.setToken(mSimpleFacebook.getAccessToken());
                        users.setfUsers(facebookUsers);

                        Intent newClient = new Intent(UserPassActivity.this, NewClientActivity_.class);
                        newClient.putExtra("newUsers", users);
                        newClient.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        UserPassActivity.this.startActivity(newClient);
                        finish();
                    } else Log.e(TAG, "profile is null or not verified");

                    //facebookLogout();
                }

            };

            mSimpleFacebook.getProfile(onProfileRequestListener);
        }
    }

    @Click
    void newClient() {
        Log.i("newClient", "newClient");

        NewClientActivity_.intent(this).startForResult(RESULT_NEW_CLIENT);
    }

    @Click
    void lostPassword() {
        Log.i("lostPassword", "lostPassword");

        LostPasswordActivity_.intent(this).startForResult(RESULT_LOST_PASSWORD);
    }

    @Background
    void login(String username, String password) {

        Log.i("Login", "user:" + username + " pass:" + password);
        Users user = RestClient.getInstance().Login(username, password);

        if (user != null) {
            if (user.getId() != null && user.getId() > 0) {
                //users = user;
                //AppPreferences.getInstance().setUsers(user);
                if (AppPreferences.getInstance() != null) {
                    try {
                        String userEncrypt = AppPreferences.getInstance().encrypt(username, "user_salt");
                        String passEncrypt = AppPreferences.getInstance().encrypt(password, username);
                        RestClient.getInstance().saveAuthorization(userEncrypt, passEncrypt);
                    } catch (Exception e) {
                        if (e.getMessage() != null) Log.e(TAG, "Exception:" + e.getMessage());
                    }
                }
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
        //submitButton.setClickable(true);
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
        //submitButton.setClickable(true);
    }
}
