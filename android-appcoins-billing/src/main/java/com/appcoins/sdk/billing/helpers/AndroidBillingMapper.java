package com.appcoins.sdk.billing.helpers;

import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.GET_SKU_DETAILS_ITEM_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ID_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE;
import static com.appcoins.sdk.core.logger.Logger.logDebug;

import android.os.Bundle;
import android.util.Base64;

import com.appcoins.sdk.billing.Appc;
import com.appcoins.sdk.billing.LaunchBillingFlowResult;
import com.appcoins.sdk.billing.Price;
import com.appcoins.sdk.billing.Purchase;
import com.appcoins.sdk.billing.PurchasesResult;
import com.appcoins.sdk.billing.SkuDetails;
import com.appcoins.sdk.billing.SkuDetailsResult;
import com.appcoins.sdk.billing.SkuDetailsV2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AndroidBillingMapper {
  private static final String APPC = "APPC";

  public static PurchasesResult mapPurchases(Bundle bundle, String skuType) {
    int responseCode = bundle.getInt(RESPONSE_CODE);
    List<Purchase> list = new ArrayList<>();
    ArrayList<String> purchaseDataList =
        bundle.getStringArrayList(INAPP_PURCHASE_DATA_LIST);
    ArrayList<String> signatureList =
        bundle.getStringArrayList(INAPP_DATA_SIGNATURE_LIST);
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

          String developerPayload = null;
          try {
            if (jsonElement.has("developerPayload")) {
              developerPayload = jsonElement.getString("developerPayload");
            }
          } catch (org.json.JSONException e) {
            logDebug("Field error" + e.getLocalizedMessage());
          }

          String token = null;
          try {
            if (jsonElement.has("token")) {
              token = jsonElement.getString("token");
            }
          } catch (org.json.JSONException e) {
            logDebug("Field error " + e.getLocalizedMessage());
          }

          if (token == null) {
            token = jsonElement.getString("purchaseToken");
          }
          boolean isAutoRenewing = false;
          try {
            if (jsonElement.has("autoRenewing")) {
              isAutoRenewing = jsonElement.getBoolean("autoRenewing");
            }
          } catch (org.json.JSONException e) {
            logDebug("Field error " + e.getLocalizedMessage());
          }
          //Base64 decoded string
          byte[] decodedSignature = Base64.decode(signature, Base64.DEFAULT);
          list.add(
              new Purchase(orderId, skuType, purchaseData, decodedSignature, purchaseTime, purchaseState,
                  developerPayload, token, packageName, sku, isAutoRenewing));
        } catch (JSONException e) {
          e.printStackTrace();
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

    if (bundle.containsKey("DETAILS_LIST")) {
      ArrayList<String> responseList = bundle.getStringArrayList("DETAILS_LIST");
      for (String value : responseList) {
        SkuDetails skuDetails = parseSkuDetails(skuType, value);
        arrayList.add(skuDetails);
      }
    }
    int responseCode = (int) bundle.get(RESPONSE_CODE);

    return new SkuDetailsResult(arrayList, responseCode);
  }

  public static SkuDetails parseSkuDetails(String skuType, String skuDetailsData) {
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
      String description = jsonElement.getString("description");

      return new SkuDetails(skuType, sku, type, price, priceAmountMicros, priceCurrencyCode,
          appcPrice, appcPriceAmountMicros, appcPriceCurrencyCode, fiatPrice, fiatPriceAmountMicros,
          fiatPriceCurrencyCode, title, description);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return new SkuDetails(skuType, "", "", "", 0, "", "", 0, "", "", 0, "", "", "");
  }

  public static LaunchBillingFlowResult mapBundleToHashMapGetIntent(Bundle bundle) {
    return new LaunchBillingFlowResult(bundle.getInt(RESPONSE_CODE), bundle.getParcelable("BUY_INTENT"));
  }

  public static ArrayList<SkuDetailsV2> mapSkuDetailsFromWS(String skuDetailsResponse) {
    ArrayList<SkuDetailsV2> skuDetailsList = new ArrayList<>();

    if (!skuDetailsResponse.isEmpty()) {
      try {
        JSONObject jsonElement = new JSONObject(skuDetailsResponse);
        JSONArray items = jsonElement.getJSONArray("items");
        for (int i = 0; i < items.length(); i++) {
          try {
            JSONObject obj = items.getJSONObject(i);

            String sku = obj.getString("sku");
            String title = obj.getString("title");
            String description = null;
            if (obj.has("description")){
              description = obj.optString("description");
            }

            JSONObject priceObj = obj.getJSONObject("price");

            JSONObject appcObj = priceObj.getJSONObject("appc");
            String appcLabel = appcObj.getString("label");
            int appcMicros = appcObj.getInt("micros");
            Appc appc = new Appc(appcLabel, appcMicros);

            String currency = priceObj.getString("currency");
            String label = priceObj.getString("label");
            int micros = priceObj.getInt("micros");
            Price price = new Price(currency, label, micros, appc);

            SkuDetailsV2 skuDetailsV2 = new SkuDetailsV2(sku, title, description, price);

            skuDetailsList.add(skuDetailsV2);
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }

    return skuDetailsList;
  }

  public static SkuDetails mapSingleSkuDetails(String skuType, String skuDetailsResponse) {
    SkuDetails skuDetails =
        new SkuDetails(skuType, "", "", "", 0, "", "", 0, "", "", 0, "", "", "");
    if (!skuDetailsResponse.isEmpty()) {
      try {
        JSONObject jsonElement = new JSONObject(skuDetailsResponse);
        JSONArray items = jsonElement.getJSONArray("items");
        if (!items.isNull(0)) {
          JSONObject obj = items.getJSONObject(0);

          String sku = obj.getString("name");

          JSONObject priceObj = obj.getJSONObject("price");

          String price = priceObj.getJSONObject("fiat")
              .getString("value");
          long priceAmountMicros = getFiatAmountInMicros(priceObj.getJSONObject("fiat"));
          String priceCurrencyCode = getFiatCurrencyCode(priceObj.getJSONObject("fiat"));
          if (priceObj.has("base") && priceObj.getString("base")
              .equalsIgnoreCase(APPC)) {
            price = getAppcPrice(priceObj);
            priceAmountMicros = getAppcAmountInMicros(priceObj);
            priceCurrencyCode = APPC;
          }

          String appcPrice = priceObj.getString("appc");
          long appcPriceAmountMicros = getAppcAmountInMicros(priceObj);

          String fiatPrice = priceObj.getJSONObject("fiat")
              .getString("value");
          long fiatPriceAmountMicros = getFiatAmountInMicros(priceObj.getJSONObject("fiat"));
          String fiatPriceCurrencyCode = getFiatCurrencyCode(priceObj.getJSONObject("fiat"));

          String title = escapeString(obj.getString("label"));

          String description = escapeString(obj.getString("description"));

          skuDetails =
              new SkuDetails(skuType, sku, skuType, price, priceAmountMicros, priceCurrencyCode,
                  appcPrice, appcPriceAmountMicros, APPC, fiatPrice, fiatPriceAmountMicros,
                  fiatPriceCurrencyCode, title, description);
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return skuDetails;
  }

  private static String escapeString(String value) {
    StringBuilder str = new StringBuilder();

    for (int i = 0, length = value.length(); i < length; i++) {
      char c = value.charAt(i);
      switch (c) {
        case '"':
        case '\\':
        case '/':
          str.append('\\')
              .append(c);
          break;
        case '\t':
          str.append("\\t");
          break;
        case '\b':
          str.append("\\b");
          break;
        case '\n':
          str.append("\\n");
          break;
        case '\r':
          str.append("\\r");
          break;
        case '\f':
          str.append("\\f");
          break;
        default:
          str.append(c);
          break;
      }
    }
    return str.toString();
  }

  private static String getAppcPrice(JSONObject parentObject) throws JSONException {
    return String.format("%s %s", parentObject.getString("appc"), APPC);
  }

  private static long getAppcAmountInMicros(JSONObject parentObject) throws JSONException {
    double price = parentObject.getDouble("appc") * 1000000;
    return (long) price;
  }

  private static String getFiatPrice(JSONObject parentObject) throws JSONException {
    String value = parentObject.getString("value");
    String symbol = parentObject.getJSONObject("currency")
        .getString("symbol");
    return String.format("%s %s", symbol, value);
  }

  private static long getFiatAmountInMicros(JSONObject parentObject) throws JSONException {
    double price = parentObject.getDouble("value") * 1000000;
    return (long) price;
  }

  private static String getFiatCurrencyCode(JSONObject parentObject) throws JSONException {
    return parentObject.getJSONObject("currency")
        .getString("code");
  }

  public static String mapSkuDetailsResponse(SkuDetailsV2 skuDetails) {
    return "{\"productId\":\""
        + skuDetails.getSku()
        + "\",\"type\" : \""
        + "INAPP"
        + "\",\"price\" : \""
        + skuDetails.getPrice().getLabel()
        + "\",\"price_currency_code\": \""
        + skuDetails.getPrice().getCurrency()
        + "\",\"price_amount_micros\": "
        + skuDetails.getPrice().getMicros()
        + ",\"appc_price\" : \""
        + skuDetails.getPrice().getAppc().getLabel()
        + "\",\"appc_price_currency_code\": \""
        + "APPC"
        + "\",\"appc_price_amount_micros\": "
        + skuDetails.getPrice().getAppc().getMicros()
        + ",\"fiat_price\" : \""
        + skuDetails.getPrice().getLabel()
        + "\",\"fiat_price_currency_code\": \""
        + skuDetails.getPrice().getCurrency()
        + "\",\"fiat_price_amount_micros\": "
        + skuDetails.getPrice().getMicros()
        + ",\"title\" : \""
        + skuDetails.getTitle()
        + "\",\"description\" : \""
        + skuDetails.getDescription()
        + "\"}";
  }
}
