package com.appcoins.sdk.billing.service;

import static com.appcoins.sdk.core.logger.Logger.logDebug;

import com.appcoins.sdk.billing.oemid.OemIdExtractor;

public class OemIdExtractorService {

    private final OemIdExtractor extractorV1;
    private final OemIdExtractor extractorV2;

    public OemIdExtractorService(OemIdExtractor extractorV1, OemIdExtractor extractorV2) {
        this.extractorV1 = extractorV1;
        this.extractorV2 = extractorV2;
    }

    public String extractOemId(String packageName) {
        String oemId;
        oemId = extractorV2.extract(packageName);
        if (oemId == null || oemId.isEmpty()) {
            oemId = extractorV1.extract(packageName);
        }
        logDebug("Extracted OemId -> " + oemId);
        return oemId;
    }
}


