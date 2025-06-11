package com.appcoins.sdk.billing;

import android.util.Base64;
import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import com.appcoins.sdk.billing.helpers.BillingResultHelper;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;
import com.appcoins.sdk.core.security.PurchasesSecurityHelper;
import java.util.ArrayList;

import static com.appcoins.sdk.core.logger.Logger.logError;
import static com.appcoins.sdk.core.logger.Logger.logWarning;
import static java.util.Collections.emptyList;

public class AppCoinsBilling implements Billing {
    private final Repository repository;

    private Thread querySkuDetailsThread = null;
    private Thread queryInappPurchasesThread = null;
    private Thread querySubsPurchasesThread = null;

    public AppCoinsBilling(Repository repository) {
        this.repository = repository;
    }

    @Override
    public void queryPurchasesAsync(QueryPurchasesParams queryPurchasesParams,
        PurchasesResponseListener purchasesResponseListener) {
        stopPreviousPurchasesRequests(queryPurchasesParams);
        PurchasesAsync purchasesAsync = new PurchasesAsync(queryPurchasesParams, purchasesResponseListener, repository);
        if (queryPurchasesParams.getProductType()
            .equalsIgnoreCase("inapp")) {
            queryInappPurchasesThread = new Thread(purchasesAsync);
            queryInappPurchasesThread.start();
        } else if (queryPurchasesParams.getProductType()
            .equalsIgnoreCase("subs")) {
            querySubsPurchasesThread = new Thread(purchasesAsync);
            querySubsPurchasesThread.start();
        } else {
            purchasesResponseListener.onQueryPurchasesResponse(BillingResult.newBuilder()
                .setResponseCode(ResponseCode.DEVELOPER_ERROR.getValue())
                .setDebugMessage(
                    BillingResultHelper.getMessageFromErrorType(BillingResultHelper.ERROR_TYPE_INVALID_PRODUCT_TYPE))
                .build(), emptyList());
            logError("Invalid product type: " + queryPurchasesParams.getProductType());
        }
    }

    @Override
    public PurchasesResult queryPurchases(String skuType) {
        try {
            PurchasesResult purchasesResult = repository.getPurchases(skuType);

            if (purchasesResult.getBillingResult()
                .getResponseCode() != ResponseCode.OK.getValue()) {
                return new PurchasesResult(new ArrayList<>(), purchasesResult.getBillingResult());
            }

            for (Purchase purchase : purchasesResult.getPurchasesList()) {
                String purchaseData = purchase.getOriginalJson();
                byte[] decodeSignature = Base64.decode(purchase.getSignature(), Base64.DEFAULT);

                if (!PurchasesSecurityHelper.INSTANCE.verifyPurchase(purchaseData, decodeSignature)) {
                    return new PurchasesResult(emptyList(), BillingResult.newBuilder()
                        .setResponseCode(ResponseCode.ERROR.getValue())
                        .build());
                }
            }

            return purchasesResult;
        } catch (ServiceConnectionException e) {
            return new PurchasesResult(emptyList(), BillingResult.newBuilder()
                .setResponseCode(ResponseCode.SERVICE_UNAVAILABLE.getValue())
                .setDebugMessage(
                    BillingResultHelper.getMessageFromErrorType(BillingResultHelper.ERROR_TYPE_SERVICE_NOT_AVAILABLE))
                .build());
        }
    }

    @Override
    public void queryProductDetailsAsync(QueryProductDetailsParams queryProductDetailsParams,
        ProductDetailsResponseListener productDetailsResponseListener) {
        stopPreviousSkuDetailsRequests();
        ProductDetailsAsync productDetailsAsync =
            new ProductDetailsAsync(queryProductDetailsParams, productDetailsResponseListener, repository);
        querySkuDetailsThread = new Thread(productDetailsAsync);
        querySkuDetailsThread.start();
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

    @Override
    public BillingResult isFeatureSupported(FeatureType feature) {
        try {
            return repository.isFeatureSupported(feature);
        } catch (ServiceConnectionException e) {
            return BillingResult.newBuilder()
                .setResponseCode(ResponseCode.SERVICE_UNAVAILABLE.getValue())
                .setDebugMessage(
                    BillingResultHelper.getMessageFromErrorType(BillingResultHelper.ERROR_TYPE_SERVICE_NOT_AVAILABLE))
                .build();
        }
    }

    private void stopPreviousPurchasesRequests(QueryPurchasesParams queryPurchasesParams) {
        try {
            if (queryPurchasesParams.getProductType()
                .equalsIgnoreCase("inapp")) {
                queryInappPurchasesThread.stop();
            } else if (queryPurchasesParams.getProductType()
                .equalsIgnoreCase("subs")) {
                querySubsPurchasesThread.stop();
            }
        } catch (Exception e) {
            logWarning(
                "Failed to stop previous Purchases " + queryPurchasesParams.getProductType() + " Request Thread: " + e);
        }
    }

    private void stopPreviousSkuDetailsRequests() {
        try {
            querySkuDetailsThread.stop();
        } catch (Exception e) {
            logWarning("Failed to stop previous SkuDetails Request Thread: " + e);
        }
    }
}
