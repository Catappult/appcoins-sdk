package com.appcoins.sdk.billing.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class RequestBuilderUtils {

  public static String buildUrl(String baseUrl, String endPoint, List<String> paths,
      Map<String, String> queries) {
    boolean hasQueries = !queries.isEmpty();
    if (endPoint == null) {
      endPoint = "";
    }
    StringBuilder urlBuilder = new StringBuilder(baseUrl + endPoint);
    for (String path : paths) {
      if (path != null) {
        buildPath(path, urlBuilder);
      }
    }
    if (hasQueries) {
      urlBuilder.append("?");
    }
    for (Map.Entry<String, String> entry : queries.entrySet()) {
      if (entry.getValue() != null && entry.getKey() != null) {
        buildQuery(entry, urlBuilder);
      }
    }
    if (hasQueries) {
      urlBuilder.deleteCharAt(urlBuilder.length() - 1);
    }
    return urlBuilder.toString();
  }

  private static void buildQuery(Map.Entry<String, String> entry, StringBuilder urlBuilder) {
    String key = "";
    String value = "";
    try {
      key = URLEncoder.encode(entry.getKey(), "utf-8");
      value = URLEncoder.encode(entry.getValue(), "utf-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    urlBuilder.append(key).append("=").append(value).append("&");
  }

  private static void buildPath(String path, StringBuilder urlBuilder) {
    String encodedPath = "";
    try {
      encodedPath = URLEncoder.encode(path, "utf-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    //Encoder transforms "=" into %3D, but it's not needed for paths
    encodedPath = encodedPath.replaceAll("%3D", "=");
    urlBuilder.append("/")
        .append(encodedPath);
  }

  public static String buildBody(Map<String, Object> bodyKeys) {
    StringBuilder builder = new StringBuilder("{");
    if (bodyKeys != null) {
      for (Map.Entry<String, Object> entry : bodyKeys.entrySet()) {
        if (entry.getValue() != null) {
          String value = entry.getValue()
              .toString();
          if (isString(entry.getValue())) {
            value = "\"" + value + "\"";
          }
          if (isMap(entry.getValue())) {
            value = buildBody((Map) entry.getValue());
          }
          builder.append("\"").append(entry.getKey()).append("\"").append(":").append(value).append(",");
        }
      }
      if (!bodyKeys.isEmpty()) {
        builder.deleteCharAt(builder.length() - 1);
      }
    }
    builder.append("}");
    return builder.toString();
  }

  private static boolean isMap(Object value) {
    return value instanceof Map;
  }

  private static boolean isString(Object value) {
    return value instanceof String && !((String) value).contains("{") && !((String) value).contains(
        "[");
  }
}
