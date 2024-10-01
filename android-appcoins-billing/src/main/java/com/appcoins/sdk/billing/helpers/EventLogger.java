package com.appcoins.sdk.billing.helpers;

import com.appcoins.billing.sdk.BuildConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONException;
import org.json.JSONObject;

import static com.appcoins.sdk.core.logger.Logger.logDebug;
import static com.appcoins.sdk.core.logger.Logger.logError;

public class EventLogger implements Runnable {

    private final String BASE_URL = "https://ws75.aptoide.com/api/7/";
    private final String SERVICE_PATH = "user/addEvent/action=CLICK/context=BILLING_SDK/name=";
    private final String purchaseEventName = "PURCHASE_INTENT";
    private final String sku;
    private final String appPackage;

    public EventLogger(String sku, String appPackage) {
        this.sku = sku;
        this.appPackage = appPackage;
    }

    public void LogPurchaseEvent() throws JSONException {
        int sdkVersionCode = BuildConfig.VERSION_CODE;
        String sdkPackageName = BuildConfig.LIBRARY_PACKAGE_NAME;

        Boolean hasWallet = WalletUtils.hasBillingServiceInstalled();

        JSONObject jsonObj = new JSONObject();
        jsonObj.put("aptoide_vercode", Integer.toString(sdkVersionCode));
        jsonObj.put("aptoide_package", sdkPackageName);
        jsonObj.put("unity_version", "sdk");

        JSONObject purchaseObj = new JSONObject();
        purchaseObj.put("package_name", appPackage);
        purchaseObj.put("sku", sku);

        JSONObject dataObj = new JSONObject();
        dataObj.put("wallet_installed", hasWallet);
        dataObj.put("purchase", purchaseObj);

        jsonObj.put("data", dataObj);

        String finalURL = BASE_URL + SERVICE_PATH + purchaseEventName;

        postDataToURL(finalURL, jsonObj);
    }

    private void postDataToURL(String urlStr, JSONObject jsonObj) {
        URL url;

        try {
            url = new URL(urlStr);

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");

            OutputStream os = connection.getOutputStream();
            String jsonString = jsonObj.toString();
            byte[] postData = jsonString.getBytes();
            os.write(postData);
            os.close();

            connection.connect();

            int code = connection.getResponseCode();
            logDebug(String.valueOf(code));

            BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            if (connection != null) {
                connection.disconnect();
                logDebug(response.toString());
            }
            br.close();
        } catch (IOException e) {
            logError("Failed to send Event to Server: " + e);
        }
    }

    @Override public void run() {
        try {
            LogPurchaseEvent();
        } catch (JSONException e) {
            logError("Failed to Log Purchase Event: " + e);
        }
    }
}
