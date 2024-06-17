package com.appcoins.sdk.ingameupdates.utils

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

object RequestBuilderUtils {

    @JvmStatic
    fun buildUrl(
        baseUrl: String,
        endPointExternal: String?,
        paths: List<String?>,
        queries: Map<String?, String?>
    ): String {
        var endPoint = endPointExternal
        val hasQueries = queries.isNotEmpty()
        if (endPoint == null) {
            endPoint = ""
        }
        val urlBuilder = StringBuilder(baseUrl + endPoint)
        for (path in paths) {
            if (path != null) {
                buildPath(path, urlBuilder)
            }
        }
        if (hasQueries) {
            urlBuilder.append("?")
        }
        for (entry in queries.entries) {
            if (entry.value != null && entry.key != null) {
                buildQuery(entry, urlBuilder)
            }
        }
        if (hasQueries) {
            urlBuilder.deleteCharAt(urlBuilder.length - 1)
        }
        return urlBuilder.toString()
    }

    private fun buildQuery(entry: Map.Entry<String?, String?>, urlBuilder: StringBuilder) {
        var key = ""
        var value = ""
        try {
            key = URLEncoder.encode(entry.key, "utf-8")
            value = URLEncoder.encode(entry.value, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        urlBuilder.append("$key=$value")
            .append("&")
    }

    private fun buildPath(path: String, urlBuilder: StringBuilder) {
        var encodedPath = ""
        try {
            encodedPath = URLEncoder.encode(path, "utf-8")
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        //Encoder transforms "=" into %3D, but it's not needed for paths
        encodedPath = encodedPath.replace("%3D".toRegex(), "=")
        urlBuilder.append("/")
            .append(encodedPath)
    }

    @JvmStatic
    fun buildBody(bodyKeys: Map<String, Any?>?): String {
        val builder = StringBuilder("{")
        if (bodyKeys != null) {
            for ((key, value1) in bodyKeys) {
                if (value1 != null) {
                    var value = value1.toString()
                    if (isString(value1)) {
                        value = "\"" + value + "\""
                    }
                    if (isMap(value1)) {
                        value = buildBody(value1 as Map<String, *>?)
                    }
                    builder.append("\"$key\":$value")
                        .append(",")
                }
            }
            if (bodyKeys.isNotEmpty()) {
                builder.deleteCharAt(builder.length - 1)
            }
        }
        builder.append("}")
        return builder.toString()
    }

    private fun isMap(value: Any?): Boolean {
        return value is Map<*, *>
    }

    private fun isString(value: Any?): Boolean {
        return value is String && !value.contains("{") && !value.contains(
            "["
        )
    }
}
