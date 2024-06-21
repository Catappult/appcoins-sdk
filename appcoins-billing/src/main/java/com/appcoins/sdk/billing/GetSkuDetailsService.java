package com.appcoins.sdk.billing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;

public class GetSkuDetailsService {

  private final static String URL_PATH = "/inapp/8.20180518/packages/packageName/products?names=";
  private final String serviceUrl;
  private String packageName;
  private List<String> sku;
  private String userAgent;
  private String paymentFlow;

  public GetSkuDetailsService(final String serviceUrl, final String packageName,
      final List<String> sku, String userAgent, String paymentFlow) {
    this.serviceUrl = serviceUrl;
    this.packageName = packageName;
    this.sku = sku;
    this.userAgent = userAgent;
    this.paymentFlow = paymentFlow;
  }

  public String getSkuDetailsForPackageName() {
    String response = "";
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
        response += inputLine;
      }

      if (in != null) {
        in.close();
      }

      if (connection != null) {
        connection.disconnect();
      }
    } catch (MalformedURLException e) {
      response = "";
      e.printStackTrace();
    } catch (ProtocolException e) {
      response = "";
      e.printStackTrace();
    } catch (IOException e) {
      response = "";
      e.printStackTrace();
    }

    return response;
  }

  private String buildURL(String packageName, List<String> sku, String paymentFlow) {
    String url = serviceUrl + URL_PATH.replaceFirst("packageName", packageName);
    for (String skuName : sku) {
      url += skuName + ",";
    }
    url = url.substring(0, url.length() - 1);
    if (paymentFlow!=null && !paymentFlow.isEmpty()){
      url += "&discount_policy=" + paymentFlow;
    }
    return url;
  }
}
