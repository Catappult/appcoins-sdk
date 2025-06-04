package com.appcoins.sdk.billing;

import java.util.List;

public interface PurchasesUpdatedListener {
    /**
     * Implement this method to get notifications for purchases updates.
     *
     * @param billingResult {@link BillingResult} with the response code and error message if present.
     * @param purchases List of updated {@link Purchase} purchases if present.
     */
    void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases);
}
