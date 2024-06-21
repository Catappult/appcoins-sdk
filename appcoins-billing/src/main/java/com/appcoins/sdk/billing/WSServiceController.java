package com.appcoins.sdk.billing;

import java.util.List;

public class WSServiceController {

  public static String getSkuDetailsService(String url, String packageName, List<String> sku,
      String userAgent, String paymentFlow) {
    GetSkuDetailsService getSkuDetailsService =
        new GetSkuDetailsService(url, packageName, sku, userAgent, paymentFlow);
    return getSkuDetailsService.getSkuDetailsForPackageName();
  }
}
