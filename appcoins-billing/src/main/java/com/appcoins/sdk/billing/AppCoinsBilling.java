package com.appcoins.sdk.billing;

import android.util.Base64;
import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils;
import java.util.ArrayList;
import java.util.Collections;

import static com.appcoins.sdk.core.logger.Logger.logError;
import static com.appcoins.sdk.core.logger.Logger.logWarning;

public class AppCoinsBilling implements Billing {
    private final Repository repository;
    private final byte[] base64DecodedPublicKey;

    private Thread querySkuDetailsThread = null;

    public AppCoinsBilling(Repository repository, byte[] base64DecodedPublicKey) {
        this.repository = repository;
        this.base64DecodedPublicKey = base64DecodedPublicKey;
    }

    @Override
    public PurchasesResult queryPurchases(String skuType) {
        try {
            PurchasesResult purchasesResult = repository.getPurchases(skuType);

            if (purchasesResult.getResponseCode() != ResponseCode.OK.getValue()) {
                return new PurchasesResult(new ArrayList<>(), purchasesResult.getResponseCode());
            }

            ArrayList<Purchase> invalidPurchase = new ArrayList<>();
            for (Purchase purchase : purchasesResult.getPurchases()) {
                String purchaseData = purchase.getOriginalJson();
                byte[] decodeSignature = purchase.getSignature();

                if (!verifyPurchase(purchaseData, decodeSignature)) {
                    invalidPurchase.add(purchase);
                    return new PurchasesResult(Collections.emptyList(), ResponseCode.ERROR.getValue());
                }
            }

            if (!invalidPurchase.isEmpty()) {
                purchasesResult.getPurchases()
                    .removeAll(invalidPurchase);
            }
            return purchasesResult;
        } catch (ServiceConnectionException e) {
            return new PurchasesResult(Collections.emptyList(), ResponseCode.SERVICE_UNAVAILABLE.getValue());
        }
    }

    public boolean verifyPurchase(String purchaseData, byte[] decodeSignature) {
        boolean result = Security.verifyPurchase(base64DecodedPublicKey, purchaseData, decodeSignature);

        if (!result) {
            String keyEncoded;
            try {
                keyEncoded = Base64.encodeToString(base64DecodedPublicKey, Base64.DEFAULT);
            } catch (Exception exception) {
                keyEncoded = null;
            }
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendPurchaseSignatureVerificationFailureEvent(purchaseData, keyEncoded);
        }

        return result;
    }

    @Override
    public void querySkuDetailsAsync(SkuDetailsParams skuDetailsParams,
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
        } catch (Exception e) {
            logWarning("Failed to stop previous SkuDetails Request Thread: " + e);
        }
    }

    @Override
    public void consumeAsync(String purchaseToken, ConsumeResponseListener listener) {
        ConsumeAsync consumeAsync = new ConsumeAsync(purchaseToken, listener, repository);
        Thread t = new Thread(consumeAsync);
        t.start();
    }

    @Override
    public LaunchBillingFlowResult launchBillingFlow(BillingFlowParams params, String payload, String oemid,
        String guestWalletId) throws ServiceConnectionException {
        try {

            return repository.launchBillingFlow(params.getSkuType(), params.getSku(), payload, oemid, guestWalletId);
        } catch (ServiceConnectionException e) {
            logError("Service is not ready to launch billing flow. " + e);
            throw new ServiceConnectionException(e.getMessage());
        }
    }

    @Override
    public boolean isReady() {
        return repository.isReady();
    }
}
