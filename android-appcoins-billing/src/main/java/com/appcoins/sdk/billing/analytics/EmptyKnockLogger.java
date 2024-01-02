package com.appcoins.sdk.billing.analytics;


import com.appcoins.sdk.billing.analytics.manager.KnockEventLogger;
import java.io.Serializable;

class EmptyKnockLogger implements KnockEventLogger, Serializable {
  @Override public void log(String url) {

  }
}
