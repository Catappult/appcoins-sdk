package com.appcoins.sdk.billing.helpers.translations;

import android.content.Context;
import java.util.List;
import java.util.Locale;

public class TranslationsRepository {

    private static TranslationsRepository translationsRepositoryInstance = null;
    private final TranslationsModel translationsModel;
    private final TranslationsXmlParser translationsXmlParser;
    private String languageCode = "";
    private String countryCode = "";

    private TranslationsRepository(TranslationsModel translationsModel, TranslationsXmlParser translationsXmlParser) {
        this.translationsModel = translationsModel;
        this.translationsXmlParser = translationsXmlParser;
    }

    public static TranslationsRepository getInstance(Context context) {
        if (translationsRepositoryInstance == null) {
            translationsRepositoryInstance =
                new TranslationsRepository(new TranslationsModel(), new TranslationsXmlParser(context));
            translationsRepositoryInstance.fetchTranslations();
        }
        return translationsRepositoryInstance;
    }

    public String getString(TranslationsKeys key) {
        return translationsModel.getString(key);
    }

    private void fetchTranslations() {
        Locale locale = Locale.getDefault();
        if (needsToRefreshModel(locale.getLanguage(), locale.getCountry())) {
            translate(locale.getLanguage(), locale.getCountry());
        }
    }

    private void translate(String languageCode, String countryCode) {
        this.languageCode = languageCode;
        this.countryCode = countryCode;
        List<String> translationList = translationsXmlParser.parseTranslationXml(languageCode, countryCode);
        translationsModel.mapStrings(translationList);
    }

    private boolean needsToRefreshModel(String languageCode, String countryCode) {
        return !this.languageCode.equalsIgnoreCase(languageCode) && !this.countryCode.equalsIgnoreCase(countryCode);
    }

    private boolean languageFromIran() {
        return !this.languageCode.equalsIgnoreCase("fa");
    }
}
