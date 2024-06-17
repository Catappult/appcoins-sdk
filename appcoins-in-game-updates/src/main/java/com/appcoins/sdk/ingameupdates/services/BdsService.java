package com.appcoins.sdk.ingameupdates.services;

import android.content.Context;
import android.os.AsyncTask;

import com.appcoins.sdk.ingameupdates.usecases.GetUserAgent;
import com.appcoins.sdk.ingameupdates.utils.RequestBuilderUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BdsService implements Service {

    public final static int TIME_OUT_IN_MILLIS = 30000;
    private String baseUrl;
    private int timeoutInMillis;
    private List<ServiceAsyncTask> asyncTasks;
    private Context context;

    public BdsService(Context context, String baseUrl, int timeoutInMillis) {
        this.context = context;
        this.baseUrl = baseUrl;
        this.timeoutInMillis = timeoutInMillis;
        this.asyncTasks = new ArrayList<>();
    }

    RequestResponse createRequest(String baseUrl, String endPoint, String httpMethod,
                                  List<String> paths, Map<String, String> queries, Map<String, String> header,
                                  Map<String, Object> body) {
        HttpURLConnection urlConnection = null;
        try {
            String urlBuilder = RequestBuilderUtils.buildUrl(baseUrl, endPoint, paths, queries);
            URL url = new URL(urlBuilder);
            urlConnection = openUrlConnection(url, httpMethod);

            urlConnection.setReadTimeout(timeoutInMillis);

            setUserAgent(urlConnection);
            setHeaders(urlConnection, header);
            handlePostPatchRequests(urlConnection, httpMethod, body);

            int responseCode = urlConnection.getResponseCode();
            InputStream inputStream;
            if (responseCode >= 400) {
                inputStream = urlConnection.getErrorStream();
            } else {
                inputStream = urlConnection.getInputStream();
            }
            return readResponse(inputStream, responseCode);
        } catch (Exception firstException) {
            firstException.printStackTrace();
            return handleException(urlConnection, firstException);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void handlePostPatchRequests(HttpURLConnection urlConnection, String httpMethod,
                                         Map<String, Object> body) throws IOException {
        if (isValidPostPatchRequest(httpMethod, body)) {
            if (httpMethod.equals("PATCH")) {
                urlConnection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            }
            setPostOutput(urlConnection, body);
        }
    }

    private boolean isValidPostPatchRequest(String httpMethod, Map<String, Object> body) {
        return (httpMethod.equals("POST") || httpMethod.equals("PATCH")) && body != null;
    }

    private void setUserAgent(HttpURLConnection urlConnection) {
        urlConnection.setRequestProperty("User-Agent", GetUserAgent.INSTANCE.invoke(context));
    }

    private void setHeaders(HttpURLConnection urlConnection, Map<String, String> header) {
        if (header != null) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private HttpURLConnection openUrlConnection(URL url, String httpMethod) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod(httpMethod);
        return urlConnection;
    }

    private RequestResponse readResponse(InputStream inputStream, int responseCode)
            throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return new RequestResponse(responseCode, response.toString(), null);
    }

    private void setPostOutput(HttpURLConnection urlConnection, Map<String, Object> bodyKeys)
            throws IOException {
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setDoOutput(true);
        OutputStream os = urlConnection.getOutputStream();
        String body = RequestBuilderUtils.buildBody(bodyKeys);
        byte[] input = body.getBytes(); //Default: UTF-8
        os.write(input, 0, input.length);
    }

    private RequestResponse handleException(HttpURLConnection urlConnection,
                                            Exception firstException) {
        firstException.printStackTrace();
        int responseCode = 500;
        if (urlConnection != null) {
            try {
                responseCode = urlConnection.getResponseCode();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return new RequestResponse(responseCode, null, firstException);
    }

    public void makeRequest(String endPoint, String httpMethod, List<String> paths,
                            Map<String, String> queries, Map<String, String> header, Map<String, ?> body,
                            ServiceResponseListener serviceResponseListener) {
        if (paths == null) {
            paths = new ArrayList<>();
        }
        if (queries == null) {
            queries = new HashMap<>();
        }
        ServiceAsyncTask serviceAsyncTask =
                new ServiceAsyncTask(this, baseUrl, endPoint, httpMethod, paths, queries, header, body,
                        serviceResponseListener);
        serviceAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        asyncTasks.add(serviceAsyncTask);
    }

    @Override
    public void cancelRequests() {
        for (ServiceAsyncTask asyncTask : asyncTasks) {
            asyncTask.cancel(true);
        }
    }
}
