package com.appcoins.sdk.billing.service;

import android.os.AsyncTask;
import java.util.List;
import java.util.Map;

public class ServiceAsyncTask extends AsyncTask<Object, Object, RequestResponse> {

  private final String httpMethod;
  private final List<String> paths;
  private final Map<String, String> queries;
  private final Map<String, Object> body;
  private final Map<String, String> header;
  private final BdsService bdsService;
  private final String baseUrl;
  private final String endPoint;
  private final ServiceResponseListener serviceResponseListener;

  ServiceAsyncTask(BdsService bdsService, String baseUrl, String endPoint, String httpMethod,
      List<String> paths, Map<String, String> queries, Map<String, String> header,
      Map<String, Object> body, ServiceResponseListener serviceResponseListener) {
    this.bdsService = bdsService;
    this.baseUrl = baseUrl;
    this.endPoint = endPoint;
    this.httpMethod = httpMethod;
    this.paths = paths;
    this.queries = queries;
    this.header = header;
    this.body = body;
    this.serviceResponseListener = serviceResponseListener;
  }

  @Override protected RequestResponse doInBackground(Object[] objects) {
    return bdsService.createRequest(baseUrl, endPoint, httpMethod, paths, queries, header, body);
  }

  @Override protected void onPostExecute(RequestResponse requestResponse) {
    super.onPostExecute(requestResponse);
    if (serviceResponseListener != null) {
      serviceResponseListener.onResponseReceived(requestResponse);
    }
  }
}
