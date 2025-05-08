package com.appcoins.sdk.billing.helpers;

import android.os.Bundle;
import android.util.Base64;
import com.appcoins.sdk.billing.LaunchBillingFlowResult;
import com.appcoins.sdk.billing.Purchase;
import com.appcoins.sdk.billing.PurchasesResult;
import com.appcoins.sdk.billing.ResponseCode;
import com.appcoins.sdk.billing.SkuDetails;
import com.appcoins.sdk.billing.SkuDetailsResult;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.DETAILS_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.GET_SKU_DETAILS_ITEM_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ID_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.KEY_BUY_INTENT;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE;
import static com.appcoins.sdk.core.logger.Logger.logDebug;
import static com.appcoins.sdk.core.logger.Logger.logError;

public class AndroidBillingMapper {

    public static PurchasesResult mapPurchases(Bundle bundle, String skuType) {
        int responseCode = bundle.getInt(RESPONSE_CODE);
        List<Purchase> list = new ArrayList<>();
        ArrayList<String> purchaseDataList = bundle.getStringArrayList(INAPP_PURCHASE_DATA_LIST);
        ArrayList<String> signatureList = bundle.getStringArrayList(INAPP_DATA_SIGNATURE_LIST);
        ArrayList<String> idsList = bundle.getStringArrayList(INAPP_PURCHASE_ID_LIST);

        if (purchaseDataList != null && signatureList != null && idsList != null) {
            for (int i = 0; i < purchaseDataList.size(); ++i) {
                String purchaseData = purchaseDataList.get(i);
                String signature = signatureList.get(i);

                JSONObject jsonElement;
                try {
                    jsonElement = new JSONObject(purchaseData);
                    String orderId = jsonElement.getString("orderId");
                    String packageName = jsonElement.getString("packageName");
                    String sku = jsonElement.getString("productId");
                    long purchaseTime = jsonElement.getLong("purchaseTime");
                    int purchaseState = jsonElement.getInt("purchaseState");

                    String developerPayload = getStringValueFromJson(jsonElement, "developerPayload");
                    String obfuscatedAccountId = getStringValueFromJson(jsonElement, "obfuscatedAccountId");
                    String token = getStringValueFromJson(jsonElement, "token");
                    if (token == null) {
                        token = getStringValueFromJson(jsonElement, "purchaseToken");
                    }
                    boolean isAutoRenewing = getBooleanValueFromJson(jsonElement, "autoRenewing");

                    //Base64 decoded string
                    byte[] decodedSignature = Base64.decode(signature, Base64.DEFAULT);
                    list.add(new Purchase(orderId, skuType, purchaseData, decodedSignature, purchaseTime, purchaseState,
                        developerPayload, obfuscatedAccountId, token, packageName, sku, isAutoRenewing));
                } catch (JSONException e) {
                    logError("Failed to map Purchase: " + e);
                }
            }
        }
        return new PurchasesResult(list, responseCode);
    }

    public static Bundle mapArrayListToBundleSkuDetails(List<String> skus) {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(GET_SKU_DETAILS_ITEM_LIST, (ArrayList<String>) skus);
        return bundle;
    }

    public static SkuDetailsResult mapBundleToHashMapSkuDetails(String skuType, Bundle bundle) {
        ArrayList<SkuDetails> arrayList = new ArrayList<>();

        if (bundle.containsKey(DETAILS_LIST)) {
            ArrayList<String> responseList = bundle.getStringArrayList(DETAILS_LIST);
            if (responseList != null) {
                for (String value : responseList) {
                    SkuDetails skuDetails = parseSkuDetails(skuType, value);
                    if (skuDetails != null) {
                        arrayList.add(skuDetails);
                    }
                }
            }
        }

        int responseCode = ResponseCode.ERROR.getValue();
        if (bundle.containsKey(RESPONSE_CODE)) {
            responseCode = (int) bundle.get(RESPONSE_CODE);
        }

        return new SkuDetailsResult(arrayList, responseCode);
    }

    public static LaunchBillingFlowResult mapBundleToHashMapGetIntent(Bundle bundle) {
        return new LaunchBillingFlowResult(bundle.getInt(RESPONSE_CODE), bundle.getParcelable(KEY_BUY_INTENT));
    }

    private static SkuDetails parseSkuDetails(String skuType, String skuDetailsData) {
        try {
            JSONObject jsonElement = new JSONObject(skuDetailsData);

            String sku = jsonElement.getString("productId");
            String type = jsonElement.getString("type");
            String price = jsonElement.getString("price");
            long priceAmountMicros = jsonElement.getLong("price_amount_micros");
            String priceCurrencyCode = jsonElement.getString("price_currency_code");
            String appcPrice = jsonElement.getString("appc_price");
            long appcPriceAmountMicros = jsonElement.getLong("appc_price_amount_micros");
            String appcPriceCurrencyCode = jsonElement.getString("appc_price_currency_code");
            String fiatPrice = jsonElement.getString("fiat_price");
            long fiatPriceAmountMicros = jsonElement.getLong("fiat_price_amount_micros");
            String fiatPriceCurrencyCode = jsonElement.getString("fiat_price_currency_code");
            String title = jsonElement.getString("title");
            String description = getStringValueFromJson(jsonElement, "description");
            String period = getStringValueFromJson(jsonElement, "period");
            String trial_period = getStringValueFromJson(jsonElement, "trial_period");
            String trial_period_end_date = getStringValueFromJson(jsonElement, "trial_period_end_date");

            return new SkuDetails(skuType, sku, type, price, priceAmountMicros, priceCurrencyCode, appcPrice,
                appcPriceAmountMicros, appcPriceCurrencyCode, fiatPrice, fiatPriceAmountMicros, fiatPriceCurrencyCode,
                title, description, period, trial_period, trial_period_end_date);
        } catch (JSONException e) {
            logError("Failed to parse SkuDetails: " + e);
        }

        return null;
    }

    private static String getStringValueFromJson(JSONObject jsonObject, String key) {
        String value = null;
        try {
            if (jsonObject.has(key)) {
                value = jsonObject.getString(key);
            }
        } catch (org.json.JSONException e) {
            logDebug("Field error" + e.getLocalizedMessage());
        }
        return value;
    }

    private static boolean getBooleanValueFromJson(JSONObject jsonObject, String key) {
        boolean value = false;
        try {
            if (jsonObject.has(key)) {
                value = jsonObject.getBoolean(key);
            }
        } catch (org.json.JSONException e) {
            logDebug("Field error" + e.getLocalizedMessage());
        }
        return value;
    }
}
