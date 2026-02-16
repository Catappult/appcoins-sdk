package com.appcoins.sdk.core.extensions

import org.json.JSONArray
import org.json.JSONObject

/**
 * Iterates through a [JSONArray], providing only non-null [JSONObject]s.
 */
inline fun JSONArray.forEachObject(action: (JSONObject) -> Unit) {
    for (i in 0 until length()) {
        optJSONObject(i)?.let(action)
    }
}
