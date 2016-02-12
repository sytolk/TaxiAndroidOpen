/*
package com.opentaxi.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.models.NewCUsers;
import com.opentaxi.models.Users;
import com.opentaxi.rest.RestClient;
import com.sromku.simple.fb.Permission;
import com.sromku.simple.fb.SimpleFacebook;
import com.sromku.simple.fb.SimpleFacebookConfiguration;
import com.sromku.simple.fb.entities.Profile;
import com.sromku.simple.fb.entities.Work;
import com.sromku.simple.fb.listeners.OnLoginListener;
import com.sromku.simple.fb.listeners.OnProfileListener;
import com.stil.generated.mysql.tables.pojos.Contact;
import com.stil.generated.mysql.tables.pojos.Contactaddress;
import com.stil.generated.mysql.tables.pojos.FacebookUsers;
import com.taxibulgaria.enums.Gender;
import org.acra.ACRA;
import org.androidannotations.annotations.*;

*/
/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/8/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 *//*

@EActivity(R.layout.login)
public class UserPassActivityOld extends Activity implements Validator.ValidationListener {

    private static final String TAG = "UserPassActivity";

    private static final int RESULT_NEW_CLIENT = 1;
    private static final int RESULT_LOST_PASSWORD = 2;

    @ViewById(R.id.clientLoginButton)
    Button submitButton;

    @TextRule(order = 1, minLength = 3, message = "Username is too short.Enter at least 3 characters.")
    @ViewById(R.id.userNameField)
    EditText userName;

    @TextRule(order = 2, minLength = 6, message = "Password is too short.Enter at least 6 characters.")
    @ViewById(R.id.passwordField)
    EditText pass;

    @ViewById(R.id.pbProgress)
    ProgressBar pbProgress;

    @ViewById(R.id.loginLayout)
    LinearLayout loginLayout;

    Validator validator;

    SimpleFacebook mSimpleFacebook;
    private int result = Activity.RESULT_OK;

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
        hideProgress();
    }

    */
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
    }*//*


    @AfterViews
    void afterLoad() {
        submitButton.setClickable(true);
        result = Activity.RESULT_OK;
        validator = new Validator(this);
        validator.setValidationListener(this);

        pass.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(final View v, final int keyCode, final KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    userName.setError(null);
                    pass.setError(null);
                    validator.validateAsync();
                    //return true; //this will keep keyboard open
                }

                return false;
            }
        });
        */
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
        }*//*

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
                startActivityForResult(intent, MainActivity.SERVER_CHANGE);
                return true;

            case R.id.options_help:
                HelpActivity_.intent(this).startForResult(MainActivity.HELP);
                return true;

            case R.id.options_exit:
                result = Activity.RESULT_CANCELED;
                RestClient.getInstance().clearCache();
                finish();
                return true;
            case R.id.options_send_log:

                RestClient.getInstance().clearCache();
                */
/*String javaTmpDir = System.getProperty("java.io.tmpdir");
                File cacheDir = new File(javaTmpDir, "DiskLruCacheDir");
                if (cacheDir.exists()) {
                    File[] files = cacheDir.listFiles();
                    if (files != null)
                        for (File file : files) {
                            file.delete();
                        }
                }*//*

                ACRA.getErrorReporter().handleSilentException(new Exception("Developer Report"));
                // int i = 2 / 0;
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mSimpleFacebook == null) mSimpleFacebook = SimpleFacebook.getInstance(this);
        mSimpleFacebook.onActivityResult(this, requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @UiThread
    void showProgress() {
        if (pbProgress != null) {
            pbProgress.setVisibility(View.VISIBLE);
            loginLayout.setVisibility(View.GONE);
        }
    }

    @UiThread
    void hideProgress() {
        if (pbProgress != null) {
            pbProgress.setVisibility(View.GONE);
            loginLayout.setVisibility(View.VISIBLE);
        }
    }

    @Click
    void facebookButton() {

        mSimpleFacebook = SimpleFacebook.getInstance(this); // Permissions.USER_BIRTHDAY
        Permission[] permissions = new Permission[]{Permission.EMAIL, Permission.USER_WEBSITE, Permission.USER_WORK_HISTORY, Permission.USER_ABOUT_ME, Permission.USER_HOMETOWN};
        SimpleFacebookConfiguration configuration = new SimpleFacebookConfiguration.Builder()
                .setAppId("550947981660612")
                .setNamespace("taxi-bulgaria")
                .setPermissions(permissions)
                .setAskForAllPermissionsAtOnce(false)
                .build();
        SimpleFacebook.setConfiguration(configuration);

        final OnLoginListener onLoginListener = new OnLoginListener() {

            @Override
            public void onFail(String reason) {
                Log.e(TAG, "onFail:" + reason);
                overFacebookLoginTime(reason);
            }

            @Override
            public void onException(Throwable throwable) {
                Log.e(TAG, "onException:" + throwable.getMessage());
                overFacebookLoginTime(getString(R.string.exception));
            }

            @Override
            public void onThinking() {
                Log.i(TAG, "onThinking");
                showProgress();
            }

            @Override
            public void onLogin() {
                Log.i(TAG, "onLogin");
                showProgress();
                if (AppPreferences.getInstance() != null && mSimpleFacebook.getSession() != null) {
                    AppPreferences.getInstance().setAccessToken(mSimpleFacebook.getSession().getAccessToken());  //todo move this to disk cache
                    checkFacebook(mSimpleFacebook.getSession().getAccessToken());
                } else {
                    overFacebookLoginTime(getString(R.string.fb_session_error));
                    Log.e(TAG, "onLogin getSession=" + mSimpleFacebook.getSession());
                }
            }

            @Override
            public void onNotAcceptingPermissions(Permission.Type type) {
                if (mSimpleFacebook.getSession() != null)
                    Log.e(TAG, "onNotAcceptingPermissions token:" + mSimpleFacebook.getSession().getAccessToken());
                overFacebookLoginTime(getString(R.string.fb_no_access));
            }
        };
        mSimpleFacebook.login(onLoginListener);
        if (mSimpleFacebook.getSession() != null)
            Log.i(TAG, "mSimpleFacebook.login:" + mSimpleFacebook.getSession().getAccessToken());

        */
/*Session session = Session.getActiveSession();
        if (!session.isOpened() && !session.isClosed()) {
            session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
        } else {
            Session.openActiveSession(this, true, statusCallback);
        }*//*

    }

    @Background(delay = 15000)
    void maxFacebookLoginTime() {
        Log.e(TAG, "maxFacebookLoginTime");
        if (mSimpleFacebook.getSession() == null || mSimpleFacebook.getSession().getAccessToken() == null || mSimpleFacebook.getSession().getAccessToken().equals("")) {
            overFacebookLoginTime("времето изтече");
        } else Log.i(TAG, "maxFacebookLoginTime have token:" + mSimpleFacebook.getSession().getAccessToken());
    }

    @UiThread
    void overFacebookLoginTime(String title) {
        if (TaxiApplication.isUserPassVisible()) {
            TaxiApplication.userPassPaused();
            hideProgress();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(getString(R.string.fb_login) + " " + title);
            alertDialogBuilder.setMessage(getString(R.string.new_account_question));
            //null should be your on click listener
            alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    newClient();
                }
            });
            alertDialogBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    TaxiApplication.userPassResumed();
                }
            });

            Dialog facebookDialog = alertDialogBuilder.create();
            facebookDialog.show();
        } else Log.i(TAG, "overFacebookLoginTime TaxiApplication.isUserPassVisible=false");
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
                if (AppPreferences.getInstance() != null) AppPreferences.getInstance().setUsers(user);
                finish();
            } else setError(getString(R.string.wrong_userpass));

            //facebookLogout();
        } else { //new Facebook User
            Log.i(TAG, "New facebookUser");
            OnProfileListener profileListener = new OnProfileListener() {

                @Override
                public void onThinking() {
                    Log.i(TAG, "New facebookUser onThinking");
                    showProgress();
                    maxFacebookLoginTime();
                }

                @Override
                public void onException(Throwable throwable) {
                    Log.i(TAG, "New facebookUser onException", throwable);
                    //facebookLogout();
                    overFacebookLoginTime("повдигнато е изключение");
                }

                @Override
                public void onFail(String reason) {
                    Log.i(TAG, "New facebookUser onFail");
                    overFacebookLoginTime(reason);
                }

                @Override
                public void onComplete(Profile profile) {
                    Log.i(TAG, "New facebookUser onComplete");
                    if (profile != null) { //&& profile.getVerified()) {
                        NewCUsers users = new NewCUsers();
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
                        facebookUsers.setToken(mSimpleFacebook.getSession().getAccessToken());
                        users.setfUsers(facebookUsers);

                        Intent newClient = new Intent(UserPassActivityOld.this, NewClientActivity_.class);
                        newClient.putExtra("newCUsers", users);
                        newClient.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        UserPassActivityOld.this.startActivity(newClient);
                        finish();
                    } else Log.e(TAG, "profile is null or not verified");

                    //facebookLogout();
                }
            };

            mSimpleFacebook.getProfile(profileListener);
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

        LostPasswordFragment_.intent(this).startForResult(RESULT_LOST_PASSWORD);
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
                        if (userEncrypt != null && passEncrypt != null) {
                            if (!RestClient.getInstance().saveAuthorization(userEncrypt, passEncrypt)) {
                                user.setUsername(username);
                                user.setPassword(Hashing.sha1().hashString(password, Charsets.UTF_8).toString());
                                AppPreferences.getInstance().setUsers(user);
                                Log.e(TAG, "Exception: saveAuthorization");
                            }
                        }
                    } catch (Exception e) {
                        if (e.getMessage() != null) Log.e(TAG, "Exception:" + e.getMessage());
                    }
                }
                //Toast.makeText(UserPassActivity.this, "Влязохте в системата успешно!", Toast.LENGTH_LONG).show();
                finish();
            } else setError(getString(R.string.wrong_userpass));
            // Toast.makeText(UserPassActivity.this, "Грешно потребителско име или парола!", Toast.LENGTH_LONG).show();

        } else setError(getString(R.string.error_check_internet));
        //Toast.makeText(UserPassActivity.this, "Грешка! Сигурни ли сте че имате връзка с интернет?", Toast.LENGTH_LONG).show();
    }

    @Click
    void clientLoginButton() {
        submitButton.setClickable(false);
        if (validator != null) {
            userName.setError(null);
            pass.setError(null);

            validator.validate();
        }
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
*/
