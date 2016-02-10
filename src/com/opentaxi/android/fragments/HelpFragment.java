package com.opentaxi.android.fragments;

import android.app.Activity;
import android.webkit.WebSettings;
import android.webkit.WebView;
import com.opentaxi.android.R;
import org.androidannotations.annotations.*;

/**
 * Created with IntelliJ IDEA.
 * User: stanimir
 * Date: 1/25/13
 * Time: 10:03 AM
 * To change this template use File | Settings | File Templates.
 */
@EFragment(R.layout.links_layout)
public class HelpFragment extends BaseFragment {

    //private static final String TAG = "LinksActivity";

    @ViewById(R.id.webview)
    WebView webview;


   /* @Override
    public void onBackPressed() {

        if (webview != null) {
            //webview.clearHistory();
            //webview.clearCache(true);
            webview.loadUrl("about:blank");
        } else super.onBackPressed();
    }

    @Override
    public void finish() {
        if (webview != null) {
            webview.clearHistory();
            webview.clearCache(true);
            webview.loadUrl("about:blank");
            webview.destroy();
            webview = null;
        }
        super.finish();
    }*/

    @AfterViews
    void afterActivity() {
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        //webview.setPluginsEnabled(true);
        //webview.loadUrl("javascript:android.selection.longTouch();");
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webview.loadUrl("http://taxi-bulgaria.com/help/client");
    }

    @Click
    void okButton() {
        if(mListener!=null) mListener.startHome();
    }
}