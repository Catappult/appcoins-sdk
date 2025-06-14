package com.appcoins.sdk.billing;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;
import com.appcoins.sdk.billing.types.SkuType;

public interface AppcoinsBillingClient {
    /**
     * Call this method to obtain the Purchases associated to the User.
     *
     * @param queryPurchasesParams {@link QueryPurchasesParams} parameters for the querying of the Purchases.
     *
     * @return {@link PurchasesResult}.
     */
    PurchasesResult queryPurchasesAsync(QueryPurchasesParams queryPurchasesParams);

    /**
     * Call this method to obtain the Purchases associated to the User.
     *
     * @param queryPurchasesParams {@link QueryPurchasesParams} parameters for the querying of the Purchases.
     * @param purchasesResponseListener {@link PurchasesResponseListener} listener to which the Purchases will be sent.
     */
    void queryPurchasesAsync(QueryPurchasesParams queryPurchasesParams,
        PurchasesResponseListener purchasesResponseListener);

    /**
     * Call this method to obtain the details of the Products available for your application.
     *
     * @param queryProductDetailsParams {@link QueryProductDetailsParams} of the Products to be searched.
     * @param productDetailsResponseListener {@link ProductDetailsResponseListener} listener to which the Product
     * Details will
     * be sent.
     */
    void queryProductDetailsAsync(QueryProductDetailsParams queryProductDetailsParams,
        ProductDetailsResponseListener productDetailsResponseListener);

    /**
     * Call this method to consume a Purchase.
     * It is important to call this method after a Purchase is made in order to finalize the Payment and consume it.
     * Not calling this method will lead to a refund of the Payment made by the User.
     *
     * @param consumeParams {@link ConsumeParams} of the Purchase to be consumed.
     * @param consumeResponseListener listener to where the status of the Consume will be provided.
     */
    void consumeAsync(ConsumeParams consumeParams, ConsumeResponseListener consumeResponseListener);

    /**
     * Call this method to launch a billing flow for an SKU.
     * <p>
     * <b>Can't be called in the Main/UI Thread. Use IO Thread when executing this method.</b>
     *
     * @return int type value of the {@link ResponseCode}.
     */
    int launchBillingFlow(Activity activity, BillingFlowParams billingFlowParams);

    /**
     * Call this method to initialize the Connection to the BillingClient.
     *
     * @param listener of type {@link AppCoinsBillingStateListener} to which the connection state will be provided.
     */
    void startConnection(AppCoinsBillingStateListener listener);

    /**
     * Method to end any connection to the BillingClient.
     */
    void endConnection();

    /**
     * Call this method to verify the status of the BillingClient.
     *
     * @return Boolean value if the BillingClient is ready to be interacted with.
     */
    boolean isReady();

    /**
     * @param requestCode the requestCode received from the onActivityResult method.
     * @param resultCode the resultCode received from the onActivityResult method.
     * @param data the data received from the onActivityResult method.
     *
     * @return Boolean value if the result was processed correctly.
     *
     * @deprecated This method is no longer necessary in the Billing Integration.
     */
    @Deprecated
    boolean onActivityResult(int requestCode, int resultCode, Intent data);

    /**
     * Method to obtain the Deeplink to the correct store from which the Application got installed.
     * <p>
     * <b>Can't be called in the Main/UI Thread. Use IO Thread when executing this method.</b>
     *
     * @return {@link ReferralDeeplink}.
     */
    ReferralDeeplink getReferralDeeplink();

    /**
     * Call this method to verify if an update is available for your application.
     *
     * @return If there is an update available for your application.
     */
    boolean isAppUpdateAvailable();

    /**
     * Call this method to launch the Store from which your application was installed in your applications page.
     */
    void launchAppUpdateStore(Context context);

    /**
     * This method will launch a PopUp Dialog when a new version of your application is available and
     * if the User proceeds, will launch the Store from which your application was installed in your applications page.
     */
    void launchAppUpdateDialog(Context context);

    /**
     * Call this method to verify if a {@link FeatureType} is supported in the current SDK Version and
     * the Billing Service that it is using.
     *
     * @return Integer value of the ResponseCode. ResponseCode.OK if the Feature is Supported.
     */
    int isFeatureSupported(FeatureType feature);

    /**
     * This method is deprecated, use {@link #queryPurchasesAsync(QueryPurchasesParams, PurchasesResponseListener)}
     * or {@link #queryPurchasesAsync(QueryPurchasesParams)} instead.
     * Call this method to obtain the Purchases associated to the User.
     *
     * @param skuType {@link SkuType} value of the Type of Purchase to search.
     *
     * @return {@link PurchasesResult}.
     */
    @Deprecated
    PurchasesResult queryPurchases(String skuType);

    /**
     * This method is deprecated, use
     * {@link #queryProductDetailsAsync(QueryProductDetailsParams, ProductDetailsResponseListener)} instead.
     * <p>
     * Call this method to obtain the details of the SKUs available in your application.
     *
     * @param skuDetailsParams {@link SkuDetailsParams} of the SKUs to be searched.
     * @param onSkuDetailsResponseListener {@link SkuDetailsResponseListener} listener to which the SKU Details will
     * be sent.
     */
    @Deprecated
    void querySkuDetailsAsync(SkuDetailsParams skuDetailsParams,
        SkuDetailsResponseListener onSkuDetailsResponseListener);

    /**
     * This method is deprecated, use {@link #consumeAsync(ConsumeParams, ConsumeResponseListener)} instead.
     * <p>
     * Call this method to consume a Purchase.
     * It is important to call this method after a Purchase is made in order to finalize the Payment and consume it.
     * Not calling this method will lead to a refund of the Payment made by the User.
     *
     * @param token token {@link Purchase#token} of the Purchase to be consumed.
     * @param consumeResponseListener listener to where the status of the Consume will be provided.
     */
    @Deprecated
    void consumeAsync(String token, ConsumeResponseListener consumeResponseListener);
}
