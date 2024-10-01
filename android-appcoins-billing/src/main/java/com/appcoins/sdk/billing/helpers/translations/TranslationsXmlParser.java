package com.appcoins.sdk.billing.helpers.translations;

import static com.appcoins.sdk.core.logger.Logger.logError;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TranslationsXmlParser {

    private static final String translationsRelativePath =
            "appcoins-wallet/resources/translations/values-";
    private static final String translationsFileName = "/external_strings.xml";
    private final Context context;
    private final List<String> requiredCountryCodes;

    public TranslationsXmlParser(Context context) {
        this.context = context;
        this.requiredCountryCodes = Arrays.asList("HR", "BR", "CN");
    }

    List<String> parseTranslationXml(String language, String countryCode) {
        String translationXmlPath;
        if (isRequiredCountryCode(countryCode)) {
            translationXmlPath =
                    translationsRelativePath + language + "-r" + countryCode + translationsFileName;
        } else {
            translationXmlPath = translationsRelativePath + language + translationsFileName;
        }

        InputStream inputStream;
        List<String> xmlContent = new ArrayList<>();
        try {
            inputStream = context.getAssets()
                    .open(translationXmlPath);
            xmlContent = parseXml(inputStream);
            inputStream.close();
        } catch (IOException e) {
            logError("Failed to parse translation xml: " + e);
        }
        return xmlContent;
    }

    public List<String> parseTranslationXmlWithPath(String path) {

        InputStream inputStream;
        List<String> xmlContent = new ArrayList<>();
        try {
            inputStream = context.getAssets()
                    .open(path);
            xmlContent = parseXml(inputStream);
            inputStream.close();
        } catch (IOException e) {
            logError("Failed to parse translation xml with path: " + e);
        }
        return xmlContent;
    }

    private List<String> parseXml(InputStream inputStream) throws IOException {
        List<String> xmlContent = new ArrayList<>();
        try {
            XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
            xmlPullParserFactory.setNamespaceAware(true);
            XmlPullParser parser = xmlPullParserFactory.newPullParser();
            parser.setInput(inputStream, null);
            int eventType = parser.getEventType();
            xmlContent = new ArrayList<>();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String value = parser.getText();
                if (eventType == XmlPullParser.TEXT && !value.trim()
                        .isEmpty()) {
                    xmlContent.add(value.trim());
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            inputStream.close();
            logError("Failed to parse xml: " + e);
        }
        return xmlContent;
    }

    private boolean isRequiredCountryCode(String countryCode) {
        for (String code : requiredCountryCodes) {
            if (code.equals(countryCode)) {
                return true;
            }
        }
        return false;
    }
}