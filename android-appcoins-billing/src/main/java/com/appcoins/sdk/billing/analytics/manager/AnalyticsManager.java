package com.appcoins.sdk.billing.analytics.manager;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.appcoins.sdk.core.logger.Logger.logDebug;
import static com.appcoins.sdk.core.logger.Logger.logWarningDebug;

public class AnalyticsManager implements Serializable {
    private final KeyValueNormalizer analyticsNormalizer;
    private final Map<EventLogger, Collection<String>> eventLoggers;

    private AnalyticsManager(Map<EventLogger, Collection<String>> eventLoggers,
        KeyValueNormalizer analyticsNormalizer) {
        this.eventLoggers = eventLoggers;
        this.analyticsNormalizer = analyticsNormalizer;
    }

    /**
     * <p>Logs the events to the correspondent event loggers.</p>
     *
     * </p> Only the events whose {@code eventName} is listed in the respective eventLoggers map are
     * logged.</p>
     *
     * @param data The attributes of the event
     * @param eventName The name of the event to be logged.
     * @param action The action done by the user.
     * @param context The context of where the event took place
     */
    public void logEvent(Map<String, Object> data, String eventName, Action action, String context) {
        logDebug("Called with: "
            + "eventName = ["
            + eventName
            + "], data = ["
            + data
            + "],  action = ["
            + action
            + "], context = ["
            + context
            + "]");
        int eventsSent = 0;
        data = analyticsNormalizer.normalize(data);
        for (Map.Entry<EventLogger, Collection<String>> loggerEntry : eventLoggers.entrySet()) {
            if (loggerEntry.getValue()
                .contains(eventName)) {
                loggerEntry.getKey()
                    .logEvent(eventName, data, action, context);
                eventsSent++;
            }
        }

        if (eventsSent <= 0) {
            logWarningDebug(eventName + " event not sent ");
        }
    }

    /**
     * <p>Possible actions, that were performed by the user, to log</p>
     */
    public enum Action {
        CLICK, SCROLL, INPUT, AUTO, ROOT, VIEW, INSTALL, OPEN, IMPRESSION, PULL_REFRESH, DISMISS, ENDLESS_SCROLL, ERROR
    }

    /**
     * <p>Builds an AnalyticsManager with a list of EventLoggers, an HttpKnockEventLogger
     * and a SessionLogger.</p>
     */
    public static class Builder {
        private final Map<EventLogger, Collection<String>> eventLoggers;
        private KeyValueNormalizer analyticsNormalizer;

        /**
         * <p>Start the builder.</p>
         */
        public Builder() {
            eventLoggers = new HashMap<>();
        }

        /**
         * <p>Adds an {@link EventLogger} and the respective {@code supportedEvents }. </p>
         * <p>This {@code eventLogger} will allow to register a service to log any of the {@code
         * supportedEvents
         * }.</p>
         *
         * <p>If this builder was not started yet (see {@link #Builder()}), a
         * {@link NullPointerException} will occur.</p>
         *
         * @param eventLogger The EventLogger to add.
         * @param supportedEvents A collection of the possible events associated with the {@code
         * eventLogger}.
         *
         * @return A builder with the added {@code eventLogger} and respective {@code
         * supportedEvents}.
         *
         * @see NullPointerException
         */
        public Builder addLogger(EventLogger eventLogger, Collection<String> supportedEvents) {
            eventLoggers.put(eventLogger, supportedEvents);
            return this;
        }

        /**
         * <p>Sets a {@link KeyValueNormalizer} that will allow to normalize event attributes
         * according to the normalizer implementation.</p>
         *
         * @param analyticsNormalizer The {@code analyticsNormalizer} to normalize the events data.
         *
         * @return A builder with the updated {@link KeyValueNormalizer}
         */
        public Builder setAnalyticsNormalizer(KeyValueNormalizer analyticsNormalizer) {
            this.analyticsNormalizer = analyticsNormalizer;
            return this;
        }

        /**
         * <p>Builds an AnalyticsManager object.</p>
         *
         * <p> An AnalyticsManager needs least one {@link EventLogger}.</p>
         *
         * <p>If this builder was not started ( see {@link #Builder()} ), a
         * {@link NullPointerException} will occur.</p>
         *
         * <p>If at least one {@link EventLogger} was not added (see {@link
         * #addLogger(EventLogger, Collection)}, an IllegalArgumentException will be thrown.</p>
         *
         * @return An AnalyticsManager object with a {@code EventLogger}.
         *
         * @see NullPointerException
         */
        public AnalyticsManager build() {
            if (eventLoggers.isEmpty()) {
                throw new IllegalArgumentException("Analytics manager need at least one logger");
            }
            return new AnalyticsManager(eventLoggers, analyticsNormalizer);
        }
    }
}
