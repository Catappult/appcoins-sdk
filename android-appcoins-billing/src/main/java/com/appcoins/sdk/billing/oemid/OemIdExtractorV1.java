package com.appcoins.sdk.billing.oemid;

import android.content.Context;
import android.content.pm.PackageManager;
import java.io.InputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.appcoins.sdk.core.logger.Logger.logWarning;

public class OemIdExtractorV1 implements OemIdExtractor {

    private final Context context;

    public OemIdExtractorV1(Context context) {
        this.context = context;
    }

    @Override
    public String extract(String packageName) {
        String oemId = null;
        try {
            String sourceDir = getPackageName(context, packageName);
            ZipFile myZipFile = new ZipFile(sourceDir);
            ZipEntry entry = myZipFile.getEntry("META-INF/attrib");
            if (entry != null) {
                InputStream inputStream = myZipFile.getInputStream(entry);
                Properties properties = new Properties();
                properties.load(inputStream);
                if (properties.containsKey("oemid")) {
                    oemId = properties.getProperty("oemid");
                }
            }
        } catch (Exception e) {
            logWarning("Failed to obtain OEMID from Extractor V1: " + e);
        }
        return oemId;
    }

    private String getPackageName(Context context, String packageName) throws PackageManager.NameNotFoundException {
        return context.getPackageManager()
            .getPackageInfo(packageName, 0).applicationInfo.sourceDir;
    }
}
