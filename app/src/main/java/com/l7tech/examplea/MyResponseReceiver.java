package com.l7tech.examplea;

import android.util.Log;

import com.l7tech.examplea.util.MssoResponseReceiver;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.util.EntityUtils;

/**
 * Created by milst0808 on 11/11/15.
 */
public class MyResponseReceiver extends MssoResponseReceiver {
    public AsyncResponse delegate = null;

    @Override
    public void onSuccess(HttpResponse response) {
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

            try {
                String s = EntityUtils.toString(response.getEntity());
                delegate.processFinish(s);
            } catch (Exception e) {
                Log.e("ListClaims", "Failed to parse JSON response for /claimshistory endpoint", e);
            }
        }
    }

    @Override
    public void onFailure(int errorCode, String errorMessage) {
        Log.e("ListClaims", "Login Failed: " + errorMessage);
    }
}
