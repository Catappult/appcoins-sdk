package com.appcoins.communication;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;

public class ProcessedValueReturner {
    private final Context context;
    private final String senderUri;

    public ProcessedValueReturner(Context context, String senderUri) {
        this.context = context;
        this.senderUri = senderUri;
    }

    public void returnValue(String packageName, long requestCode, Parcelable response) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(senderUri));
        intent.setPackage(packageName);
        intent.putExtra("REQUEST_CODE", requestCode);
        intent.putExtra("RETURN_VALUE", response);
        context.startActivity(intent);
    }
}
