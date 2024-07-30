package com.appcoins.sdk.billing.utils;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import java.util.concurrent.atomic.AtomicInteger;

public class LayoutUtils {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    public static int generateRandomId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static void setBackground(View view, Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }
}
