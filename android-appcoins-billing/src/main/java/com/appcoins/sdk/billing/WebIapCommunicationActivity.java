package com.appcoins.sdk.billing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class WebIapCommunicationActivity extends Activity {

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri uri = intent.getData();

        Log.i("WebIapCommunicationActivity", "onCreate: uri=" + uri.toString());

        String redirectResult = uri.getQueryParameter(REDIRECT_RESULT);
        Log.i("WebIapCommunicationActivity", "onCreate: redirectResult=" + redirectResult);

        if (redirectResult != null) {
            switch (redirectResult) {
                case SUCCESS:
                Toast.makeText(this, "Payment was successful", Toast.LENGTH_SHORT).show();
                break;
                case ERROR:
                String errorCode = uri.getQueryParameter(ERROR_CODE);
                String errorDetails = uri.getQueryParameter(ERROR_DETAILS);

                Toast.makeText(this, "An error occurred (" + errorCode + ") while making the payment: " + errorDetails, Toast.LENGTH_SHORT).show();
                break;
                case CANCEL:
                Toast.makeText(this, "Payment was canceled", Toast.LENGTH_SHORT).show();
                break;
                default:
                throw new IllegalStateException("Unexpected value: " + redirectResult);
            }
        }

        finish();
    }

    private static final String REDIRECT_RESULT = "redirectResult";
    private static final String ERROR_CODE = "errorCode";
    private static final String ERROR_DETAILS = "errorDetails";
    private static final String SUCCESS = "SUCCESS";
    private static final String ERROR = "ERROR";
    private static final String CANCEL = "CANCEL";
}