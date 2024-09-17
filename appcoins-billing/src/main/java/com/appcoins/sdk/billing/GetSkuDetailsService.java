package com.appcoins.sdk.billing;

import static com.appcoins.sdk.core.logger.Logger.logError;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class GetSkuDetailsService {
  private final static String URL_PATH = "/product/8.20240901/applications/packageName/inapp/consumables?skus=";
  private final String serviceUrl;
  private final String packageName;
  private final List<String> sku;
  private final String userAgent;
  private final String paymentFlow;

  public GetSkuDetailsService(final String serviceUrl, final String packageName,
      final List<String> sku, String userAgent, String paymentFlow) {
    this.serviceUrl = serviceUrl;
    this.packageName = packageName;
    this.sku = sku;
    this.userAgent = userAgent;
    this.paymentFlow = paymentFlow;
  }

  public String getSkuDetailsForPackageName() {
    StringBuilder response = new StringBuilder();
    URL url;
    try {
      String urlBuilt = buildURL(packageName, sku, paymentFlow);
      url = new URL(urlBuilt);

      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setRequestProperty("User-Agent", userAgent);
      connection.connect();
      BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      String inputLine;

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
      }

      if (in != null) {
        in.close();
      }

      if (connection != null) {
        connection.disconnect();
      }
    } catch (IOException e) {
      response = new StringBuilder();
      logError("Failed to execute request for getSkuDetailsForPackageName: " + e);
    }

    return response.toString();
  }

  private String buildURL(String packageName, List<String> sku, String paymentFlow) {
    StringBuilder url = new StringBuilder(serviceUrl + URL_PATH.replaceFirst("packageName", packageName));
    for (String skuName : sku) {
      url.append(skuName).append(",");
    }
    url = new StringBuilder(url.substring(0, url.length() - 1));
    if (paymentFlow!=null && !paymentFlow.isEmpty()){
      url.append("&discount_policy=").append(paymentFlow);
    }
    return url.toString();
  }
}
