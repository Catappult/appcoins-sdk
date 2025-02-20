package com.appcoins.sdk.billing.service;

import com.appcoins.sdk.core.analytics.events.SdkBackendRequestType;
import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.utils.RequestBuilderUtils;
import com.appcoins.sdk.core.analytics.SdkAnalyticsUtils;
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

import static com.appcoins.sdk.core.logger.Logger.logDebug;
import static com.appcoins.sdk.core.logger.Logger.logError;

public class BdsService implements Service {

    public static final int TIME_OUT_IN_MILLIS = 30000;
    public final String baseUrl;
    public final int timeoutInMillis;

    public BdsService(String baseUrl, int timeoutInMillis) {
        this.baseUrl = baseUrl;
        this.timeoutInMillis = timeoutInMillis;
    }

    RequestResponse createRequest(String baseUrl, String endPoint, String httpMethod, List<String> paths,
        Map<String, String> queries, Map<String, String> header, Map<String, Object> body,
        SdkBackendRequestType sdkBackendRequestType) {
        HttpURLConnection urlConnection = null;
        try {
            String urlBuilder = RequestBuilderUtils.buildUrl(baseUrl, endPoint, paths, queries);
            logDebug("Url -> " + urlBuilder);
            URL url = new URL(urlBuilder);

            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendBackendRequestEvent(sdkBackendRequestType, urlBuilder, httpMethod, paths, header, queries, body);

            urlConnection = openUrlConnection(url, httpMethod);

            urlConnection.setReadTimeout(timeoutInMillis);

            setUserAgent(urlConnection);
            setHeaders(urlConnection, header);
            handlePostPatchRequests(urlConnection, httpMethod, body);

            int responseCode = urlConnection.getResponseCode();
            InputStream inputStream;
            if (responseCode >= 400) {
                inputStream = urlConnection.getErrorStream();
                SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                    .sendBackendErrorEvent(sdkBackendRequestType, urlBuilder,
                        urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage(),
                        WalletUtils.context);
            } else {
                inputStream = urlConnection.getInputStream();
            }
            RequestResponse requestResponse = readResponse(inputStream, responseCode);
            String errorMessage = null;

            if (requestResponse.getException() != null) {
                errorMessage = requestResponse.getException()
                    .getMessage();
            }

            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendBackendResponseEvent(sdkBackendRequestType, responseCode, requestResponse.getResponse(),
                    errorMessage);
            return requestResponse;
        } catch (Exception firstException) {
            SdkAnalyticsUtils.INSTANCE.getSdkAnalytics()
                .sendBackendErrorEvent(sdkBackendRequestType, baseUrl + endPoint, firstException.toString(),
                    WalletUtils.context);
            return handleException(urlConnection, firstException);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    private void handlePostPatchRequests(HttpURLConnection urlConnection, String httpMethod, Map<String, Object> body)
        throws IOException {
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
        urlConnection.setRequestProperty("User-Agent", WalletUtils.INSTANCE.getUserAgent());
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

    private RequestResponse readResponse(InputStream inputStream, int responseCode) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return new RequestResponse(responseCode, response.toString(), null);
    }

    private void setPostOutput(HttpURLConnection urlConnection, Map<String, Object> bodyKeys) throws IOException {
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setDoOutput(true);
        OutputStream os = urlConnection.getOutputStream();
        String body = RequestBuilderUtils.buildBody(bodyKeys);
        byte[] input = body.getBytes(); //Default: UTF-8
        os.write(input, 0, input.length);
    }

    private RequestResponse handleException(HttpURLConnection urlConnection, Exception firstException) {
        logError("Failed to create backend request: " + firstException);
        int responseCode = 500;
        if (urlConnection != null) {
            try {
                responseCode = urlConnection.getResponseCode();
            } catch (IOException ioException) {
                logError("Failed to read response code from request: " + ioException);
            }
        }
        return new RequestResponse(responseCode, null, firstException);
    }

    public void makeRequest(String endPoint, String httpMethod, List<String> paths, Map<String, String> queries,
        Map<String, String> header, Map<String, Object> body, ServiceResponseListener serviceResponseListener,
        SdkBackendRequestType sdkBackendRequestType) {
        if (paths == null) {
            paths = new ArrayList<>();
        }
        if (queries == null) {
            queries = new HashMap<>();
        }
        ServiceAsyncTaskExecutorAsync serviceAsyncTaskExecutorAsync =
            new ServiceAsyncTaskExecutorAsync(this, baseUrl, endPoint, httpMethod, paths, queries, header, body,
                serviceResponseListener, sdkBackendRequestType);
        serviceAsyncTaskExecutorAsync.execute();
    }
}
