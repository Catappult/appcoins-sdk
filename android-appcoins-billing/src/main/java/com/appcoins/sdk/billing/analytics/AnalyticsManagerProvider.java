package com.appcoins.sdk.billing.analytics;

import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsManagerProvider {

  private static AnalyticsManager analyticsManagerInstance = null;

  public static AnalyticsManager provideAnalyticsManager() {
    if (analyticsManagerInstance == null) {
      IndicativeEventLogger indicativeEventLogger =
          new IndicativeEventLogger(IndicativeAnalytics.INSTANCE);

      analyticsManagerInstance =
          new AnalyticsManager.Builder()
              .addLogger(indicativeEventLogger, provideIndicativeEventList())
              .setAnalyticsNormalizer(new KeysNormalizer())
              .setKnockLogger(new EmptyKnockLogger())
              .setDebugLogger(new DebugLogger())
              .build();
    }
    return analyticsManagerInstance;
  }

  private static List<String> provideIndicativeEventList() {
    List<String> list = new ArrayList<>();
    list.add(AnalyticsEvents.SDK_IAP_PURCHASE_INTENT_START);
    list.add(AnalyticsEvents.SDK_OPEN_WALLET_ATTEMPT);
    list.add(AnalyticsEvents.SDK_WALLET_INSTALL_IMPRESSION);
    list.add(AnalyticsEvents.SDK_WALLET_INSTALL_CLICK);
    list.add(AnalyticsEvents.SDK_DOWNLOAD_WALLET_VANILLA_IMPRESSION);
    list.add(AnalyticsEvents.SDK_DOWNLOAD_WALLET_FALLBACK_IMPRESSION);
    list.add(AnalyticsEvents.SDK_INSTALL_WALLET_FEEDBACK);
    return list;
  }
}
