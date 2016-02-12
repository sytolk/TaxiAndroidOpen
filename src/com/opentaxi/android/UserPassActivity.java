/*
package com.opentaxi.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import com.facebook.*;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.opentaxi.android.utils.AppPreferences;
import com.opentaxi.models.NewCUsers;
import com.opentaxi.models.Users;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.Contact;
import com.stil.generated.mysql.tables.pojos.Contactaddress;
import com.stil.generated.mysql.tables.pojos.FacebookUsers;
import com.taxibulgaria.enums.Gender;
import de.greenrobot.event.EventBus;
import org.acra.ACRA;
import org.androidannotations.annotations.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/8/13
 * Time: 8:02 PM
 * To change this template use File | Settings | File Templates.
 *//*

@EActivity(R.layout.login)
public class UserPassActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        Validator.ValidationListener {

    private static final String TAG = "UserPassActivity";

    private static final int RESULT_NEW_CLIENT = 1;
    private static final int RESULT_LOST_PASSWORD = 2;

    @ViewById(R.id.clientLoginButton)
    Button submitButton;

    @TextRule(order = 1, minLength = 3, message = "Username is too short.Enter at least 3 characters.")
    @ViewById(R.id.userNameField)
    AutoCompleteTextView userName;

    @TextRule(order = 2, minLength = 6, message = "Password is too short.Enter at least 6 characters.")
    @ViewById(R.id.passwordField)
    EditText pass;

    @ViewById(R.id.login_progress)
    ProgressBar pbProgress;

    @ViewById(R.id.login_form)
    View mLoginFormView;

    */
/*@ViewById(R.id.lostPassword)
    TextView lostPassword;

    @ViewById(R.id.newClient)
    TextView newClient;*//*


    */
/*@ViewById(R.id.loginLayout)
    LinearLayout loginLayout;*//*


    */
/*@ViewById(R.id.clientLoginButton)
    Button clientLoginButton;*//*


    @ViewById(R.id.g_sign_in_button)
    SignInButton mPlusSignInButton;

    @ViewById(R.id.facebookLoginButton)
    LoginButton facebookLoginButton;

    Validator validator;
    private CallbackManager callbackManager;

    */
/* Client used to interact with Google APIs. *//*

    private GoogleApiClient mGoogleApiClient;
    */
/* Request code used to invoke sign in user interactions. *//*

    private static final int RC_SIGN_IN = 0;
    */
/* Is there a ConnectionResult resolution in progress? *//*

    private boolean mIsResolving = false;
    */
/* Should we automatically resolve ConnectionResults when possible? *//*

    private boolean mShouldResolve = false;

    ProgressDialog ringProgressDialog;

    private int result = Activity.RESULT_OK;

    @Override
    public void onBackPressed() {
        result = Activity.RESULT_CANCELED;
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FacebookSdk.sdkInitialize(getApplicationContext());
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) fbLoggedId(accessToken);
    }

    @AfterViews
    void afterLoad() {
        submitButton.setClickable(true);
        result = Activity.RESULT_OK;
        validator = new Validator(this);
        validator.setValidationListener(this);

        initInstances();
    }

    private void initInstances() {

        //populateAutoComplete();

        pass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    userName.setError(null);
                    pass.setError(null);
                    validator.validateAsync();

                    return true;  //this will keep keyboard open
                }
                return false;
            }
        });

        //newClient.setOnClickListener(this);
        //lostPassword.setOnClickListener(this);
        //clientLoginButton.setOnClickListener(this);

        //Google+ Login
        mPlusSignInButton.setSize(SignInButton.SIZE_WIDE);
        //mPlusSignInButton.setOnClickListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .build();

        List<String> fPermissions = new ArrayList<>();
        fPermissions.add("public_profile");
        fPermissions.add("email");
        fPermissions.add("user_hometown");
        //Facebook Login
        facebookLoginButton.setReadPermissions(fPermissions); //user_birthday

        callbackManager = CallbackManager.Factory.create();
        // Callback registration
        facebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {
                fbLoggedId(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "facebookLoginButton onCancel");
                //Toast.makeText(UserPassActivity.this, "User cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Log.i(TAG, "facebookLoginButton onError", exception);
                //Toast.makeText(UserPassActivity.this, "Error on Login, check your facebook app_id", Toast.LENGTH_LONG).show();
            }
        });

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void fbLoggedId(AccessToken accessToken) {
        final String token = accessToken.getToken();
        //AppPreferences.getInstance(getApplicationContext()).setAccessToken(token);
        GraphRequest request = GraphRequest.newMeRequest(accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        // Application code
                        //Log.i(TAG, "facebookLoginButton object:" + object.toString());
                        //Log.i(TAG, "facebookLoginButton response:" + object.toString());
                        //object:{"id":"1173223717","first_name":"Stanimir","last_name":"Marinov","locale":"bg_BG","email":"smarinov@abv.bg","gender":"male","age_range":{"min":21},"hometown":{"id":"114964575187249","name":"Burgas, Bulgaria"}}
                        NewCUsers users = new NewCUsers();
                        try {
                            users.setUsername(object.getString("email")); //profile.getUsername());
                            users.setEmail(object.getString("email"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //users.setImage(profile.getPicture());

                        Contact contact = new Contact();
                        try {
                            contact.setFirstname(object.getString("first_name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //contact.setMiddlename(profile.getMiddleName());
                        try {
                            contact.setLastname(object.getString("last_name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // contact.setNotes(profile.getBio());
                                */
/*if (profile.getWork() != null) {
                                    for (Work work : profile.getWork()) {
                                        String workTitle = work.getEmployer() + " " + work.getDescription();
                                        if (contact.getJobtitle() != null)
                                            contact.setJobtitle(contact.getJobtitle() + " " + workTitle);
                                        else contact.setJobtitle(workTitle);
                                    }
                                }*//*

                        try {
                            String gender = object.getString("gender");
                            if (gender != null) {
                                if (gender.equals("male"))
                                    contact.setGender(Gender.MALE.getCode());
                                else if (gender.equals("female"))
                                    contact.setGender(Gender.FEMALE.getCode());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        users.setContact(contact);

                        try {
                            JSONObject hometown = object.getJSONObject("hometown");
                            if (hometown != null) {
                                String city = hometown.getString("name");
                                if (city != null) {
                                    int comma = city.indexOf(",");
                                    if (comma > -1) {
                                        city = city.substring(0, comma);
                                    }
                                    Contactaddress address = new Contactaddress();
                                    address.setCity(city);
                                    users.setAddress(address);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        FacebookUsers facebookUsers = new FacebookUsers();
                        try {
                            facebookUsers.setToken(token);
                            facebookUsers.setFacebookId(object.getLong("id"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        users.setfUsers(facebookUsers);
                        showProgress(true);
                        createNewUser(users);
                                */
/*Intent newClient = new Intent(UserPassActivity.this, NewClientActivity_.class);
                                newClient.putExtra("newCUsers", users);
                                newClient.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                UserPassActivity.this.startActivity(newClient);*//*

                        //finish();
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,first_name,last_name,locale,email,gender,age_range,hometown");//birthday
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Background
    void createNewUser(NewCUsers users) {
        Log.i(TAG, "createNewUser:" + users.getUsername());
        com.stil.generated.mysql.tables.pojos.Users userPojo = RestClient.getInstance().createNewUser(users);
        if (userPojo != null) {
            if (userPojo.getRecordstatus() != null && userPojo.getRecordstatus()) {  //account is already active (facebook)
                RestClient.getInstance().setAuthHeadersEncoded(userPojo.getUsername(), userPojo.getPassword());
                com.opentaxi.models.Users user = new com.opentaxi.models.Users(userPojo);
                //facebook user exist
                if (AppPreferences.getInstance() != null)
                    AppPreferences.getInstance().setUsers(user);

                EventBus.getDefault().post(user);
                finishThis();
            } //else ActivationDialog();
        } else overFacebookLoginTime("Error");
    }

    @UiThread
    void finishThis() {
        showProgress(false);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callbackManager != null) callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    */
/**
     * Shows the progress UI and hides the login form.
     *//*

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            pbProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            pbProgress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    pbProgress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            pbProgress.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
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
        //hideProgress();
        showProgress(false);
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

    */
/*@UiThread
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
    }*//*


    @UiThread
    void overFacebookLoginTime(String title) {
        if (TaxiApplication.isUserPassVisible()) {
            TaxiApplication.userPassPaused();
            showProgress(false);
            //hideProgress();
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


    */
/*@Background
    void checkFacebook(AccessToken token) {
        Log.i(TAG, "checkFacebook token:" + token);
        facebookUser(RestClient.getInstance().FacebookLogin(token.getToken()), token);
    }*//*


    */
/*@UiThread
    void facebookUser(Users user, AccessToken token) {
        if (user != null) { //user already exist in taxi-bulgaria service
            Log.i(TAG, "facebookUser user:" + user.getUsername());
            if (user.getId() != null && user.getId() > 0) {
                RestClient.getInstance().setAuthHeadersEncoded(user.getUsername(), user.getPassword());
                if (AppPreferences.getInstance() != null) AppPreferences.getInstance().setUsers(user);
                finish();
            } else setError(getString(R.string.wrong_userpass));

            //facebookLogout();
        } else { //new Facebook User

        }
    }*//*


    @Click
    void newClient() {
        //Log.i("newClient", "newClient");

        NewClientActivity_.intent(this).startForResult(RESULT_NEW_CLIENT);
    }

    @Click
    void lostPassword() {
        //Log.i("lostPassword", "lostPassword");

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
                EventBus.getDefault().post(user);
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


    @Override
    public void onConnected(Bundle bundle) {
        mShouldResolve = false;
        getProfileInformation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        ringProgressDialog.dismiss();
        mGoogleApiClient.connect();
    }

    */
/**
     * Fetching user's information name, email, profile pic
     *//*

    private void getProfileInformation() {
        ringProgressDialog.dismiss();
        if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
            Person currentPerson = Plus.PeopleApi
                    .getCurrentPerson(mGoogleApiClient);
            String personName = currentPerson.getDisplayName();
            String personPhotoUrl = currentPerson.getImage().getUrl();
            String personGooglePlusProfile = currentPerson.getUrl();
            String birth = currentPerson.getBirthday();
            String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

            // by default the profile url gives 50x50 px image only
            // we can replace the value with whatever dimension we want by
            // replacing sz=X
//                personPhotoUrl = personPhotoUrl.substring(0,
//                        personPhotoUrl.length() - 2)
//                        + PROFILE_PIC_SIZE;

            //new LoadProfileImage().execute(personPhotoUrl);

        } else {
            Toast.makeText(getApplicationContext(),
                    "Person information is null", Toast.LENGTH_LONG).show();
        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(UserPassActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        userName.setAdapter(adapter);
    }

    @Click
    void g_sign_in_button() {
//        toastLoading.show();
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        ringProgressDialog = ProgressDialog.show(UserPassActivity.this, "Connecting...", "Atempting to connect", true);
        ringProgressDialog.setCancelable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mShouldResolve = true;
                    mGoogleApiClient.connect();
                } catch (Exception e) {
                    ringProgressDialog.dismiss();
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Could not connect to Google Play Services.  The user needs to select an account,
        // grant permissions or resolve an error in order to sign in. Refer to the javadoc for
        // ConnectionResult to see possible error codes.
        Log.d("TAG_LOGIN", "onConnectionFailed:" + connectionResult);
        ringProgressDialog.dismiss();

        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    Log.e("TAG_LOGIN", "Could not resolve ConnectionResult.", e);
                    Toast.makeText(UserPassActivity.this, "Could not resolve ConnectionResult", Toast.LENGTH_LONG).show();
                    mIsResolving = false;
                }
            } else {
                // Could not resolve the connection result, show the user an
                // error dialog.
                Toast.makeText(UserPassActivity.this, "Error on Login, check your google + login method", Toast.LENGTH_LONG).show();
            }
        } else {
            // Show the signed-out UI
        }
    }

}
*/
