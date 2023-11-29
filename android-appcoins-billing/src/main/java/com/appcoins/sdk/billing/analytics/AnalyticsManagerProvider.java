package com.appcoins.sdk.billing.analytics;

import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.service.BdsService;
import com.appcoins.sdk.billing.analytics.manager.AnalyticsManager;
import java.util.ArrayList;
import java.util.List;

public class AnalyticsManagerProvider {

  private static AnalyticsManager analyticsManagerInstance = null;

  public static AnalyticsManager provideAnalyticsManager() {
    if (analyticsManagerInstance == null) {
      BdsService rakamService = new BdsService("https://rakam-api.aptoide.com/event/collect",
          BdsService.TIME_OUT_IN_MILLIS);
      WalletAddressProvider walletAddressProvider =
          WalletAddressProvider.provideWalletAddressProvider();
      RakamEventLogger rakamEventLogger =
          new RakamEventLogger(rakamService, walletAddressProvider, WalletUtils.context);
      IndicativeEventLogger indicativeEventLogger =
          new IndicativeEventLogger(new IndicativeAnalytics(WalletUtils.context));

      analyticsManagerInstance =
          new AnalyticsManager.Builder().addLogger(rakamEventLogger, provideRakamEventList())
              .addLogger(indicativeEventLogger, provideIndicativeEventList())
              .setAnalyticsNormalizer(new KeysNormalizer())
              .setKnockLogger(new EmptyKnockLogger())
              .setDebugLogger(new DebugLogger())
              .build();
    }
    return analyticsManagerInstance;
  }

  private static List<String> provideRakamEventList() {
    List<String> list = new ArrayList<>();
    list.add(BillingAnalytics.PAYMENT_METHOD);
    list.add(BillingAnalytics.PAYMENT_CONFIRMATION);
    list.add(BillingAnalytics.PAYMENT_CONCLUSION);
    list.add(BillingAnalytics.PAYMENT_START);
    return list;
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
