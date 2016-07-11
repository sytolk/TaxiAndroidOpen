package com.opentaxi.android.fragments;

import android.util.Log;
import android.widget.EditText;
import com.opentaxi.android.R;
import com.opentaxi.rest.RestClient;
import com.stil.generated.mysql.tables.pojos.RequestNotes;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by stanimir on 2/10/16.
 */
@EFragment(R.layout.contacts)
public class ContactsFragment extends BaseFragment {

    @ViewById
    EditText contactForm;

    @Click
    void sendContactsButton() {
        Log.i("sendButton", "sendButton");
        if (contactForm != null) sendNotes(contactForm.getText().toString());
        if (mListener != null) mListener.startHome();
    }

    @Background
    void sendNotes(String notes) {
        if (notes != null && !notes.isEmpty()) {
            RequestNotes requestNotes = new RequestNotes();
            requestNotes.setNotes(contactForm.getText().toString());
            RestClient.getInstance().RequestNotes(requestNotes);
        }
    }
}
