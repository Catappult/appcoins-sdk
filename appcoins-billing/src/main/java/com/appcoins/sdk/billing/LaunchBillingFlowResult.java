package com.appcoins.sdk.billing;

import android.content.Intent;

public class LaunchBillingFlowResult {
  private final int responseCode;
  private final Intent buyIntent;

  public LaunchBillingFlowResult(int responseCode, Intent buyIntent) {
    this.responseCode = responseCode;
    this.buyIntent = buyIntent;
  }

  public int getResponseCode() {
    return responseCode;
  }

  public Intent getBuyIntent() {
    return buyIntent;
  }
}
