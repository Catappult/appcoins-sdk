package com.appcoins.sdk.billing.analytics;


import com.appcoins.sdk.billing.analytics.manager.KnockEventLogger;

class EmptyKnockLogger implements KnockEventLogger {
  @Override public void log(String url) {

  }
}
