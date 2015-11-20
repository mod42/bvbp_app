/*
 * Copyright (c) 2013 CA Technologies. All rights reserved.
 */

package com.l7tech.examplea;

import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.l7tech.msso.MobileSso;
import com.l7tech.msso.gui.HttpResponseFragment;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.net.URI;

/**
 * Retained fragment that downloads and/or caches a downloaded resource in JSON format.
 * <p/>
 * Activities that wish to make use of this fragment must implement {@link UserActivity}.
 */
public class JsonDownloaderFragment extends HttpResponseFragment {
    public static final int MAX_JSON_SIZE = 100 * 1024;

    private String lastDownloadedJson;

    private UserActivity userActivity() {
        return (UserActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (lastDownloadedJson != null)
            sendJsonToActivity(lastDownloadedJson);
    }

    void downloadJson() {
        // Can only be called while there is an activity associated
        UserActivity activity = (UserActivity) getActivity();
        activity.setProgressVisibility(ProgressBar.VISIBLE);

        final URI uri = activity.getJsonDownloadUri();
        HttpGet httpGet = new HttpGet(uri);
        activity.mobileSso().processRequest(httpGet, getResultReceiver());
    }

    void sendJsonToActivity(String json) {
        UserActivity activity = userActivity();
        if (activity != null) {
            activity.setDownloadedJson(json);
        }
    }

    @Override
    protected void onResponse(long requestId, int resultCode, String errorMessage, HttpResponse httpResponse) {
        // Might be invoked while we have no associated activity
        UserActivity activity = userActivity();
        if (activity != null)
            activity.setProgressVisibility(ProgressBar.GONE);

        if (errorMessage != null) {
            if (activity != null)
                activity.showMessage(errorMessage, Toast.LENGTH_LONG);
        } else {
            lastDownloadedJson = toString(httpResponse, MAX_JSON_SIZE, false);
            sendJsonToActivity(lastDownloadedJson);
        }
    }

    /**
     * Interface that must be implemented by an Activity that wishes to use this fragment.
     */
    public interface UserActivity {
        Context getBaseContext();

        MobileSso mobileSso();

        void setProgressVisibility(int visibility);

        void setDownloadedJson(String json);

        void showMessage(String errorMessage, final int toastLength);

        URI getJsonDownloadUri();
    }
}
