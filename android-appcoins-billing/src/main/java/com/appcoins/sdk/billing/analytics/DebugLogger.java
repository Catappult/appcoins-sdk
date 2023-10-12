package com.appcoins.sdk.billing.analytics;

import android.util.Log;
import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.sdk.billing.analytics.manager.AnalyticsLogger;

class DebugLogger implements AnalyticsLogger {
  @Override public void logDebug(String tag, String msg) {
    if (BuildConfig.DEBUG) {
      Log.d(tag, msg);
    }
  }

  @Override public void logWarningDebug(String TAG, String msg) {
    if (BuildConfig.DEBUG) {
      Log.d(TAG, msg);
    }
  }
}
