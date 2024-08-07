package com.appcoins.sdk.billing;

import android.util.Log;

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;
import java.util.ArrayList;
import java.util.Collections;

public class AppCoinsBilling implements Billing {
  private static final String TAG = "AppCoinsBilling";
  private final Repository repository;
  private final byte[] base64DecodedPublicKey;

  private Thread querySkuDetailsThread = null;

  public AppCoinsBilling(Repository repository, byte[] base64DecodedPublicKey) {
    this.repository = repository;
    this.base64DecodedPublicKey = base64DecodedPublicKey;
  }

  @Override public PurchasesResult queryPurchases(String skuType) {
    try {
      PurchasesResult purchasesResult = repository.getPurchases(skuType);

      if (purchasesResult.getResponseCode() != ResponseCode.OK.getValue()) {
        return new PurchasesResult(new ArrayList<Purchase>(), purchasesResult.getResponseCode());
      }

      ArrayList<Purchase> invalidPurchase = new ArrayList<Purchase>();
      for (Purchase purchase : purchasesResult.getPurchases()) {
        String purchaseData = purchase.getOriginalJson();
        byte[] decodeSignature = purchase.getSignature();

        if (!verifyPurchase(purchaseData, decodeSignature)) {
          invalidPurchase.add(purchase);
          return new PurchasesResult(Collections.emptyList(), ResponseCode.ERROR.getValue());
        }
      }

      if (invalidPurchase.size() > 0) {
        purchasesResult.getPurchases()
            .removeAll(invalidPurchase);
      }
      return purchasesResult;
    } catch (ServiceConnectionException e) {
      return new PurchasesResult(Collections.emptyList(),
          ResponseCode.SERVICE_UNAVAILABLE.getValue());
    }
  }

  public boolean verifyPurchase(String purchaseData, byte[] decodeSignature) {
    return Security.verifyPurchase(base64DecodedPublicKey, purchaseData, decodeSignature);
  }

  @Override public void querySkuDetailsAsync(SkuDetailsParams skuDetailsParams,
      SkuDetailsResponseListener onSkuDetailsResponseListener) {
    stopPreviousSkuDetailsRequests();
    SkuDetailsAsync skuDetailsAsync =
        new SkuDetailsAsync(skuDetailsParams, onSkuDetailsResponseListener, repository);
    querySkuDetailsThread = new Thread(skuDetailsAsync);
    querySkuDetailsThread.start();
  }

  private void stopPreviousSkuDetailsRequests() {
    try {
      querySkuDetailsThread.stop();
    } catch (Exception e){
      Log.d(TAG, "Failed to stop previous SkuDetails Request Thread: " + e.getMessage());
    }
  }

  @Override public void consumeAsync(String purchaseToken, ConsumeResponseListener listener) {
    ConsumeAsync consumeAsync = new ConsumeAsync(purchaseToken, listener, repository);
    Thread t = new Thread(consumeAsync);
    t.start();
  }

  @Override
  public LaunchBillingFlowResult launchBillingFlow(BillingFlowParams params, String payload, String oemid, String guestWalletId)
      throws ServiceConnectionException {
    try {

      LaunchBillingFlowResult result =
          repository.launchBillingFlow(params.getSkuType(), params.getSku(), payload, oemid, guestWalletId);

      return result;
    } catch (ServiceConnectionException e) {
      e.printStackTrace();
      throw new ServiceConnectionException(e.getMessage());
    }
  }

  @Override public boolean isReady() {
    return repository.isReady();
  }
}
