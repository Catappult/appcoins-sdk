package com.appcoins.sdk.billing.helpers;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.appcoins.billing.AppcoinsBilling;
import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.sdk.billing.ResponseCode;
import com.appcoins.sdk.billing.payasguest.BillingRepository;
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod;
import com.appcoins.sdk.billing.service.BdsService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.appcoins.sdk.billing.helpers.AppcoinsBillingStubHelper.INAPP_DATA_SIGNATURE_LIST;
import static com.appcoins.sdk.billing.helpers.AppcoinsBillingStubHelper.INAPP_PURCHASE_DATA_LIST;
import static com.appcoins.sdk.billing.helpers.AppcoinsBillingStubHelper.INAPP_PURCHASE_ID_LIST;
import static com.appcoins.sdk.billing.helpers.AppcoinsBillingStubHelper.INAPP_PURCHASE_ITEM_LIST;
import static com.appcoins.sdk.billing.payasguest.BillingRepository.RESPONSE_ERROR;
import static com.appcoins.sdk.billing.payasguest.BillingRepository.RESPONSE_SUCCESS;

class AppcoinsBillingWrapper implements AppcoinsBilling, Serializable {

  private final AppcoinsBilling appcoinsBilling;
  private final AppCoinsPendingIntentCaller pendingIntentCaller;
  private final String walletId;
  private final int timeoutInMillis;

  AppcoinsBillingWrapper(AppcoinsBilling appcoinsBilling,
      AppCoinsPendingIntentCaller pendingIntentCaller, String walletId, int timeoutInMillis) {
    this.appcoinsBilling = appcoinsBilling;
    this.pendingIntentCaller = pendingIntentCaller;
    this.walletId = walletId;
    this.timeoutInMillis = timeoutInMillis;
  }

  @Override public int isBillingSupported(int apiVersion, String packageName, String type)
      throws RemoteException {
    pingConnection(apiVersion, packageName, type);
    return appcoinsBilling.isBillingSupported(apiVersion, packageName, type);
  }

  @Override
  public Bundle getSkuDetails(int apiVersion, String packageName, String type, Bundle skusBundle)
      throws RemoteException {
    return appcoinsBilling.getSkuDetails(apiVersion, packageName, type, skusBundle);
  }

  @Override public Bundle getBuyIntent(int apiVersion, String packageName, String sku, String type,
      String developerPayload) throws RemoteException {
    Bundle bundle = null;
    for (PaymentFlowMethod method : WalletUtils.getPayflowMethodsList()) {
      if (method instanceof PaymentFlowMethod.Wallet ||
          method instanceof PaymentFlowMethod.GamesHub) {
        bundle = WalletUtils.startServiceBind(method, appcoinsBilling,
            apiVersion, sku, type, developerPayload);
        if (bundle != null) {
          break;
        }
      }
    }
    if (bundle == null) {
      bundle = new Bundle();
      bundle.putInt(Utils.RESPONSE_CODE, ResponseCode.SERVICE_UNAVAILABLE.getValue());
    }
    pendingIntentCaller.saveIntent(bundle);
    return bundle;
  }

  @Override public Bundle getPurchases(int apiVersion, String packageName, String type,
      String continuationToken) throws RemoteException {
    Bundle bundle = appcoinsBilling.getPurchases(apiVersion, packageName, type, continuationToken);
    if (walletId != null) {
      ArrayList<String> idsList = bundle.getStringArrayList(INAPP_PURCHASE_ID_LIST);
      ArrayList<String> skuList = bundle.getStringArrayList(INAPP_PURCHASE_ITEM_LIST);
      ArrayList<String> dataList = bundle.getStringArrayList(INAPP_PURCHASE_DATA_LIST);
      ArrayList<String> signatureDataList = bundle.getStringArrayList(INAPP_DATA_SIGNATURE_LIST);
      BillingRepository billingRepository =
          new BillingRepository(new BdsService(BuildConfig.HOST_WS, BdsService.TIME_OUT_IN_MILLIS));
      GuestPurchasesInteract guestPurchasesInteract = new GuestPurchasesInteract(billingRepository);
      bundle =
          guestPurchasesInteract.mapGuestPurchases(bundle, walletId, packageName, type, idsList,
              skuList, dataList, signatureDataList);
    }
    return bundle;
  }

  @Override public int consumePurchase(int apiVersion, String packageName, String purchaseToken)
      throws RemoteException {
    int responseCode = appcoinsBilling.consumePurchase(apiVersion, packageName, purchaseToken);
    int guestResponseCode = consumeGuestPurchase(walletId, apiVersion, packageName, purchaseToken);
    if (responseCode == RESPONSE_SUCCESS || guestResponseCode == RESPONSE_SUCCESS) {
      return RESPONSE_SUCCESS;
    } else {
      return RESPONSE_ERROR;
    }
  }

  @Override public IBinder asBinder() {
    return appcoinsBilling.asBinder();
  }

  private int consumeGuestPurchase(String walletId, int apiVersion, String packageName,
      String purchaseToken) {
    int responseCode = RESPONSE_ERROR;
    if (walletId != null && apiVersion == 3) {
      BillingRepository billingRepository =
          new BillingRepository(new BdsService(BuildConfig.HOST_WS, timeoutInMillis));
      GuestPurchasesInteract guestPurchaseInteract = new GuestPurchasesInteract(billingRepository);
      responseCode =
          guestPurchaseInteract.consumeGuestPurchase(this.walletId, packageName, purchaseToken);
    }
    return responseCode;
  }
  private void pingConnection(final int apiVersion, final String packageName, final String type) {
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    executorService.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          appcoinsBilling.isBillingSupported(apiVersion, packageName, type);
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      }
    }, 0L, 60L, TimeUnit.SECONDS);
  }
}
