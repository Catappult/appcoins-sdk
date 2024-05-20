package com.appcoins.sdk.billing;

import android.app.PendingIntent;
import android.content.Intent;

public class LaunchBillingFlowResult {
  private final int responseCode;
  private final PendingIntent buyIntent;

  private final Intent webBuyIntent;

  public LaunchBillingFlowResult(int responseCode, PendingIntent buyIntent, Intent webBuyIntent) {
    this.responseCode = responseCode;
    this.buyIntent = buyIntent;
    this.webBuyIntent = webBuyIntent;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public PendingIntent getBuyIntent() {
    return buyIntent;
  }

  public Intent getWebBuyIntent() {
    return webBuyIntent;
  }
}
