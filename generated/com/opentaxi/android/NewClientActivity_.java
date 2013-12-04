//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations 3.0-SNAPSHOT.
//


package com.opentaxi.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import com.opentaxi.android.R.id;
import com.opentaxi.android.R.layout;
import com.opentaxi.models.NewUsers;
import org.androidannotations.api.BackgroundExecutor;
import org.androidannotations.api.SdkVersionHelper;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedListener;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class NewClientActivity_
    extends NewClientActivity
    implements HasViews, OnViewChangedListener
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();
    private Handler handler_ = new Handler(Looper.getMainLooper());

    private void init_(Bundle savedInstanceState) {
        OnViewChangedNotifier.registerOnViewChangedListener(this);
        requestWindowFeature(1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
        setContentView(layout.new_client);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (((SdkVersionHelper.getSdkInt()< 5)&&(keyCode == KeyEvent.KEYCODE_BACK))&&(event.getRepeatCount() == 0)) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    public static NewClientActivity_.IntentBuilder_ intent(Context context) {
        return new NewClientActivity_.IntentBuilder_(context);
    }

    public static NewClientActivity_.IntentBuilder_ intent(Fragment supportFragment) {
        return new NewClientActivity_.IntentBuilder_(supportFragment);
    }

    @Override
    public void onViewChanged(HasViews hasViews) {
        userName = ((EditText) hasViews.findViewById(id.userNameField));
        middleName = ((EditText) hasViews.findViewById(id.middleName));
        passwordHint = ((EditText) hasViews.findViewById(id.passwordHint));
        iAgreeCheckBox = ((CheckBox) hasViews.findViewById(id.iAgreeCheckBox));
        pass = ((EditText) hasViews.findViewById(id.passwordField));
        lastName = ((EditText) hasViews.findViewById(id.lastName));
        email = ((EditText) hasViews.findViewById(id.emailField));
        nameField = ((EditText) hasViews.findViewById(id.nameField));
        sendButton = ((Button) hasViews.findViewById(id.sendButton));
        pass2 = ((EditText) hasViews.findViewById(id.password2Field));
        cityName = ((AutoCompleteTextView) hasViews.findViewById(id.cityName));
        if (hasViews.findViewById(id.userAgreement)!= null) {
            hasViews.findViewById(id.userAgreement).setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    NewClientActivity_.this.userAgreement();
                }

            }
            );
        }
        if (hasViews.findViewById(id.sendButton)!= null) {
            hasViews.findViewById(id.sendButton).setOnClickListener(new OnClickListener() {


                @Override
                public void onClick(View view) {
                    NewClientActivity_.this.sendButton();
                }

            }
            );
        }
        if (hasViews.findViewById(id.emailField)!= null) {
            hasViews.findViewById(id.emailField).setOnFocusChangeListener(new OnFocusChangeListener() {


                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    NewClientActivity_.this.focusChangedOnEmailField(view, hasFocus);
                }

            }
            );
        }
        if (hasViews.findViewById(id.userNameField)!= null) {
            hasViews.findViewById(id.userNameField).setOnFocusChangeListener(new OnFocusChangeListener() {


                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    NewClientActivity_.this.focusChangedOnUserNameField(view, hasFocus);
                }

            }
            );
        }
        afterLoad();
    }

    @Override
    public void ActivationDialog() {
        handler_.post(new Runnable() {


            @Override
            public void run() {
                NewClientActivity_.super.ActivationDialog();
            }

        }
        );
    }

    @Override
    public void setUserError(final String error) {
        handler_.post(new Runnable() {


            @Override
            public void run() {
                NewClientActivity_.super.setUserError(error);
            }

        }
        );
    }

    @Override
    public void setEmailError(final String error) {
        handler_.post(new Runnable() {


            @Override
            public void run() {
                NewClientActivity_.super.setEmailError(error);
            }

        }
        );
    }

    @Override
    public void createNewUser(final NewUsers users) {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0, "") {


            @Override
            public void execute() {
                try {
                    NewClientActivity_.super.createNewUser(users);
                } catch (Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }

        }
        );
    }

    @Override
    public void checkEmail(final String email) {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0, "") {


            @Override
            public void execute() {
                try {
                    NewClientActivity_.super.checkEmail(email);
                } catch (Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }

        }
        );
    }

    @Override
    public void checkUsername(final String username) {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0, "") {


            @Override
            public void execute() {
                try {
                    NewClientActivity_.super.checkUsername(username);
                } catch (Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }

        }
        );
    }

    public static class IntentBuilder_ {

        private Context context_;
        private final Intent intent_;
        private Fragment fragmentSupport_;

        public IntentBuilder_(Context context) {
            context_ = context;
            intent_ = new Intent(context, NewClientActivity_.class);
        }

        public IntentBuilder_(Fragment fragment) {
            fragmentSupport_ = fragment;
            context_ = fragment.getActivity();
            intent_ = new Intent(context_, NewClientActivity_.class);
        }

        public Intent get() {
            return intent_;
        }

        public NewClientActivity_.IntentBuilder_ flags(int flags) {
            intent_.setFlags(flags);
            return this;
        }

        public void start() {
            context_.startActivity(intent_);
        }

        public void startForResult(int requestCode) {
            if (fragmentSupport_!= null) {
                fragmentSupport_.startActivityForResult(intent_, requestCode);
            } else {
                if (context_ instanceof Activity) {
                    ((Activity) context_).startActivityForResult(intent_, requestCode);
                } else {
                    context_.startActivity(intent_);
                }
            }
        }

    }

}
