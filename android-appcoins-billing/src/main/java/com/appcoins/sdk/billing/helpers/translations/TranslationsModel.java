package com.appcoins.sdk.billing.helpers.translations;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.appcoins_wallet;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iab_wallet_not_installed_popup_body;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iab_wallet_not_installed_popup_close_button;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iab_wallet_not_installed_popup_close_install;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iap_wallet_and_appstore_not_installed_popup_body;
import static com.appcoins.sdk.billing.helpers.translations.TranslationsKeys.iap_wallet_and_appstore_not_installed_popup_button;

//Class covered with AndroidTests. Always run them if you change this class
public class TranslationsModel {

  //This is needed as there may be an error parsing the Xml.
  private Map<TranslationsKeys, String> defaultStringsMap;

  /**
   * Whenever you add a string to an xml file, you should create the enum with the key name in
   * the TranslationsKeys.java and then add it to the defaultStringsMap in the position as in the
   * xml. After this you should run the AndroidTests to confirm that you add it correctly.
   */
  public TranslationsModel() {
    defaultStringsMap = new LinkedHashMap<TranslationsKeys, String>() {
      { //This list needs to be in the same order as the string xml file. If not the androidTests
        // will fail.
        put(iab_wallet_not_installed_popup_body, "To buy this item you first need to get the %s.");
        put(appcoins_wallet, "AppCoins Wallet");
        put(iab_wallet_not_installed_popup_close_button, "CLOSE");
        put(iap_wallet_and_appstore_not_installed_popup_body,
            "You need the AppCoins Wallet to make this purchase. Download it from Aptoide or Play "
                + "Store"
                + " and come back to complete your purchase!");
        put(iap_wallet_and_appstore_not_installed_popup_button, "GOT IT!");
        put(iab_wallet_not_installed_popup_close_install, "INSTALL WALLET");
      }
    };
  }

  public void mapStrings(List<String> list) {
    int position = 0;
    int listSize = list.size();
    for (Map.Entry<TranslationsKeys, String> entry : defaultStringsMap.entrySet()) {
      if (position >= listSize) {
        return;
      }
      entry.setValue(list.get(position));
      position++;
    }
  }

  public String getString(TranslationsKeys key) {
    return defaultStringsMap.get(key);
  }

  //Test only methods. The following methods are only to support tests.

  public Map<TranslationsKeys, String> getDefaultStringsMap() {
    return defaultStringsMap;
  }

  public int getNumberOfTranslations() {
    return defaultStringsMap.size();
  }
}
