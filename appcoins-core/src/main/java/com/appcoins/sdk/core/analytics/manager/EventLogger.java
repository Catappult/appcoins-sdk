package com.appcoins.sdk.core.analytics.manager;

import android.content.Context;
import java.util.Map;

public interface EventLogger {
    void initialize(Context context, String key);

    /**
     * <p>Sends an event with parameters.</p>
     *
     * @param eventName The name of the event to be logged.
     * @param data The attributes of the event.
     * @param action The action done by the user.
     * @param context The context of where the event took place.
     */
    void logEvent(String eventName, Map<String, Object> data, AnalyticsManager.Action action, String context);
}
