package com.opentaxi.android;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
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

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/8/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 */
@EActivity(R.layout.login)
public class UserPassActivity extends Activity implements Validator.ValidationListener {

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

    Validator validator;

    SimpleFacebook mSimpleFacebook;

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

        mSimpleFacebook = SimpleFacebook.getInstance(this);
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

                //Log.e(TAG, "onLogin");
                if (AppPreferences.getInstance() != null)
                    AppPreferences.getInstance().setAccessToken(mSimpleFacebook.getAccessToken());  //todo move this to disk cache
                checkFacebook(mSimpleFacebook.getAccessToken());
            }

            @Override
            public void onNotAcceptingPermissions() {
                Log.e(TAG, "onNotAcceptingPermissions");
            }

            @Override
            public void onThinking() {
                Log.i(TAG, "onThinking");
            }

            @Override
            public void onException(Throwable throwable) {
                Log.e(TAG, "onException:" + throwable.getMessage());
                facebookLogout();
            }

            @Override
            public void onFail(String reason) {
                Log.e(TAG, "onFail:" + reason);
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

    @Background
    void checkFacebook(String token) {
        Log.i(TAG, "checkFacebook token:" + token);
        facebookUser(RestClient.getInstance().FacebookLogin(token));
    }

    @UiThread
    void facebookUser(Users user) {
        if (user != null) { //user already exist
            if (user.getId() != null && user.getId() > 0) {
                AppPreferences.getInstance().setUsers(user);
                finish();
            } else setError("Грешно потребителско име или парола!");

            facebookLogout();
        } else { //new Facebook User

            SimpleFacebook.OnProfileRequestListener onProfileRequestListener = new SimpleFacebook.OnProfileRequestListener() {

                @Override
                public void onFail(String reason) {
                }

                @Override
                public void onException(Throwable throwable) {
                    facebookLogout();
                }

                @Override
                public void onThinking() {
                }

                @Override
                public void onComplete(Profile profile) {

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

                    facebookLogout();
                }

            };

            mSimpleFacebook.getProfile(onProfileRequestListener);
        }
    }

    @Background
    void facebookLogout() {
        SimpleFacebook.OnLogoutListener onLogoutListener = new SimpleFacebook.OnLogoutListener() {

            @Override
            public void onFail(String reason) {
            }

            @Override
            public void onException(Throwable throwable) {
            }

            @Override
            public void onThinking() {
            }

            @Override
            public void onLogout() {
            }

        };
        mSimpleFacebook.logout(onLogoutListener);
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
