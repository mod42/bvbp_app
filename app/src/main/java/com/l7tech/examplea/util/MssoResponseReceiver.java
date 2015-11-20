/*
 * Copyright (c) 2013 CA Technologies. All rights reserved.
 */
package com.l7tech.examplea.util;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import com.l7tech.msso.service.MssoClient;
import com.l7tech.msso.service.MssoIntents;
import org.apache.http.HttpResponse;

public abstract class MssoResponseReceiver extends ResultReceiver {

    private static final String TAG = MssoResponseReceiver.class.getCanonicalName();

    private MssoResponseReceiver(Handler handler) {
        super(handler);
    }
    public MssoResponseReceiver() {
        super(null);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        long requestId = resultData.getLong(MssoIntents.RESULT_REQUEST_ID);
        try {
            if (resultCode != MssoIntents.RESULT_CODE_SUCCESS) {
                String message = resultData.getString(MssoIntents.RESULT_ERROR_MESSAGE);
                if (message == null) {
                    message = "<Unknown error>";
                }
                onFailure(resultCode, message);
            } else {
                if (requestId == -1 || requestId == 0) {
                    onFailure(MssoIntents.RESULT_CODE_ERR_BAD_REQUEST_ID, "Received result included an invalid request ID");
                } else {
                    HttpResponse httpResponse = MssoClient.takeResponse(requestId);
                    if (httpResponse == null) {
                        onFailure(MssoIntents.RESULT_CODE_ERR_BAD_REQUEST_ID, "Request was canceled");
                    } else {
                        onSuccess(httpResponse);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    /**
     * Callback when successfully invoked the endpoint.
     *
     * @param response The HttpResponse of the endpoint.
     */
    public abstract void onSuccess(HttpResponse response);

    /**
     * Callback when failed to invoke the endpoint.
     *
     * @param errorCode The error code for the error. {@link com.l7tech.msso.service.MssoIntents} RESULT_CODE
     * @param errorMessage The error message for the error.
     */
    public abstract void onFailure(int errorCode, String errorMessage);

}
