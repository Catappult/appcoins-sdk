package com.appcoins.sdk.billing.analytics.manager;

public interface AnalyticsLogger {
  void logDebug(String tag, String msg);

  void logWarningDebug(String TAG, String msg);
}
