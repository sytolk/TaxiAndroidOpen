package com.opentaxi.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.opentaxi.generated.mysql.tables.pojos.Messages;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/25/13
 * Time: 10:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class MessageActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private static final int MESSAGE = 40;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.confirm_layout);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {

            Messages messages = (Messages) intent.getSerializableExtra(Messages.class.getName());

            if (messages != null) {

                TextView requestView = (TextView) findViewById(R.id.confirmText);
                requestView.setText(messages.getMsg());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        finish();
    }

    @Override
    public void finish() {
        if (getParent() == null) {
            setResult(Activity.RESULT_OK);
        } else {
            getParent().setResult(Activity.RESULT_OK);
        }
        super.finish();
    }

    public void confirmOkClick(View view) {
        finish();
        //Intent intent = new Intent(MessageActivity.this, MainActivity.class);
        //MessageActivity.this.startActivityIfNeeded(intent, -1);
    }
}