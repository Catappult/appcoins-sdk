package com.appcoins.sdk.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.appcoins.sdk.billing.listeners.SDKWebResponse;
import com.appcoins.sdk.billing.listeners.SDKWebResponseStream;

public class WebIapCommunicationActivity extends Activity {

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri uri = intent.getData();

        String redirectResult = uri.getQueryParameter(REDIRECT_RESULT);

        if (redirectResult != null) {
            SDKWebResponse sdkWebResponse =
                    new SDKWebResponse(
                            mapRedirectResultToResultCode(redirectResult),
                            uri.getQueryParameter(DATA)
                    );
            SDKWebResponseStream.getInstance().emit(sdkWebResponse);
        }

        finish();
    }

    private int mapRedirectResultToResultCode(String redirectResult) {
        int resultCode;
        switch (redirectResult) {
            case SUCCESS:
                resultCode = -1;
                break;
            case ERROR:
                resultCode = 1;
                break;
            case CANCEL:
                resultCode = 0;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + redirectResult);
        }
        return resultCode;
    }

    private static final String REDIRECT_RESULT = "redirectResult";
    private static final String DATA = "data";
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_DETAILS = "errorDetails";
    private static final String SUCCESS = "SUCCESS";
    private static final String ERROR = "ERROR";
    private static final String CANCEL = "CANCEL";
}