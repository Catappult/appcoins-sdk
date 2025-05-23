package com.appcoins.sdk.billing.listeners;

import com.appcoins.sdk.billing.ProductDetailsResponseListener;
import com.appcoins.sdk.billing.SkuDetails;
import java.util.List;

/**
 * Deprecated listener interface. Use {@link ProductDetailsResponseListener} instead.
 */
@Deprecated
public interface SkuDetailsResponseListener {
    void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList);
}
