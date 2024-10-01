package com.appcoins.sdk.billing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a block of information about in-app items.
 * An Inventory is returned by such methods as {@link IabHelper#queryInventory}.
 */
public class Inventory {
    Map<String, SkuDetails> mSkuMap = new HashMap<>();
    Map<String, Purchase> mPurchaseMap = new HashMap<>();

    public Inventory() {
    }

    /**
     * Returns the listing details for an in-app product.
     */
    public SkuDetails getSkuDetails(String sku) {
        return mSkuMap.get(sku);
    }

    /**
     * Returns purchase information for a given product, or null if there is no purchase.
     */
    public Purchase getPurchase(String sku) {
        return mPurchaseMap.get(sku);
    }

    /**
     * Returns whether or not there exists a purchase of the given product.
     */
    public boolean hasPurchase(String sku) {
        return mPurchaseMap.containsKey(sku);
    }

    /**
     * Return whether or not details about the given product are available.
     */
    public boolean hasDetails(String sku) {
        return mSkuMap.containsKey(sku);
    }

    /**
     * Erase a purchase (locally) from the inventory, given its product ID. This just
     * modifies the Inventory object locally and has no effect on the server! This is
     * useful when you have an existing Inventory object which you know to be up to date,
     * and you have just consumed an item successfully, which means that erasing its
     * purchase data from the Inventory you already have is quicker than querying for
     * a new Inventory.
     */
    public void erasePurchase(String sku) {
        mPurchaseMap.remove(sku);
    }

    /**
     * Returns a list of all owned product IDs.
     */
    List<String> getAllOwnedSkus() {
        return new ArrayList<>(mPurchaseMap.keySet());
    }

    /**
     * Returns a list of all owned product IDs of a given type
     */
    public List<String> getAllOwnedSkus(String itemType) {
        List<String> result = new ArrayList<>();
        for (Purchase p : mPurchaseMap.values()) {
            if (p.getItemType().equals(itemType)) result.add(p.getSku());
        }
        return result;
    }

    public List<SkuDetails> getAllSkuDetails() {
        return new ArrayList<>(mSkuMap.values());
    }

    /**
     * Returns a list of all purchases.
     */
    public List<Purchase> getAllPurchases() {
        return new ArrayList<>(mPurchaseMap.values());
    }

    public void addSkuDetails(SkuDetails d) {
        mSkuMap.put(d.getSku(), d);
    }

    public void addPurchase(Purchase p) {
        mPurchaseMap.put(p.getSku(), p);
    }
}
