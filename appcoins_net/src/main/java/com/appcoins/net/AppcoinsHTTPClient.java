package com.appcoins.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class AppcoinsHTTPClient implements AppcoinsConnectionQuerys {

  private final String serviceUrl;
  private URL urlConnection;
  private String concat;

  public AppcoinsHTTPClient(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  public void startService() throws IOException {

    urlConnection = new URL(serviceUrl + concat);
  }

  @Override public String Get(String params) throws IOException {
    concat = params;
    URLConnection yc = urlConnection.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
    String inputLine;
    while ((inputLine = in.readLine()) != null) {
      in.close();
    }
    return inputLine;
  }
}