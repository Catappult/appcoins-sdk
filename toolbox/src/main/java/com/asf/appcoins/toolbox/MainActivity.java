package com.asf.appcoins.toolbox;

import static com.appcoins.sdk.core.logger.Logger.logDebug;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.appcoins.sdk.billing.AppcoinsBillingClient;
import com.appcoins.sdk.billing.BillingFlowParams;
import com.appcoins.sdk.billing.Purchase;
import com.appcoins.sdk.billing.PurchasesResult;
import com.appcoins.sdk.billing.PurchasesUpdatedListener;
import com.appcoins.sdk.billing.ResponseCode;
import com.appcoins.sdk.billing.SkuDetails;
import com.appcoins.sdk.billing.SkuDetailsParams;
import com.appcoins.sdk.billing.helpers.CatapultBillingAppCoinsFactory;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.types.SkuType;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

  private AppcoinsBillingClient cab;
  private String token;
  private AppCoinsBillingStateListener listener;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    PurchasesUpdatedListener purchaseFinishedListener = (responseCode, purchases) -> {
      if (responseCode == ResponseCode.OK.getValue()) {
        for (Purchase purchase : purchases) {
          token = purchase.getToken();
        }
      } else {
        new AlertDialog.Builder(this).setMessage(
            String.format(Locale.ENGLISH, "response code: %d -> %s", responseCode,
                ResponseCode.values()[responseCode].name()))
            .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
            .create()
            .show();
      }
    };
    cab = CatapultBillingAppCoinsFactory.BuildAppcoinsBilling(this, BuildConfig.IAB_KEY,
        purchaseFinishedListener);

    listener = new AppCoinsBillingStateListener() {
      @Override public void onBillingSetupFinished(int responseCode) {
        logDebug("Is Billing Setup Finished:  Connected-" + responseCode);
      }

      @Override public void onBillingServiceDisconnected() {
        logDebug("Message: Disconnected");
      }
    };
    cab.startConnection(listener);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    logDebug("(" + requestCode + "," + resultCode + "," + data + ")");
    cab.onActivityResult(requestCode, resultCode, data);
    if (data != null && data.getExtras() != null) {
      Bundle bundle = data.getExtras();
      if (bundle != null) {
        for (String key : bundle.keySet()) {
          Object value = bundle.get(key);
          if (value != null) {
            logDebug("Message Key: " + key);
            logDebug("Message value: " + value);
          }
        }
      }
    }
  }

  public void onBuyGasButtonClicked(View arg0) {
    BillingFlowParams billingFlowParams =
        new BillingFlowParams("gas", SkuType.inapp.toString(), null, null, null);

    Activity act = this;
    Thread t = new Thread(() -> {
      int launchBillingFlowResponse = cab.launchBillingFlow(act, billingFlowParams);
      logDebug("BillingFlowResponse: " + launchBillingFlowResponse);
    });
    t.start();
  }

  public void onUpgradeAppButtonClicked(View arg0) {

    Thread t = new Thread(() -> {
      PurchasesResult pr = cab.queryPurchases(SkuType.inapp.toString());
      if (!pr.getPurchases().isEmpty()) {
        for (Purchase p : pr.getPurchases()) {
          logDebug("Purchase result token: " + p.getToken());
          logDebug("Purchase result sku: " + p.getSku());
        }
        token = pr.getPurchases()
            .get(0)
            .getToken();
      } else {
        logDebug("Message: No Available Purchases");
      }
    });
    t.start();
  }

  public void onSkuDetailsButtonClicked(View view) {
    SkuDetailsParams skuDetailsParams = new SkuDetailsParams();
    skuDetailsParams.setItemType(SkuType.inapp.toString());
    ArrayList<String> skusList = new ArrayList<>();

    skusList.add("gas");

    skuDetailsParams.setMoreItemSkus(skusList);

    Thread t = new Thread(
        () -> cab.querySkuDetailsAsync(skuDetailsParams, (responseCode, skuDetailsList) -> {
          logDebug("responseCode: " + responseCode);
          for (SkuDetails sd : skuDetailsList) {
            logDebug(sd.toString());
          }
        }));

    t.start();
  }

  public void makePaymentButtonClicked(View view) {

    Thread t = new Thread(() -> {
      if (token != null) {
        cab.consumeAsync(token, (responseCode, purchaseToken) -> {
          logDebug("consume response: "
              + responseCode
              + " "
              + "Consumed purchase with token: "
              + purchaseToken);
          token = null;
        });
      } else {
        logDebug("Message: No purchase tokens available");
      }
    });
    t.start();
  }

  public void onCloseChannelButtonClicked(View view) {
    cab.endConnection();
  }

  private boolean checkChannelAvailable() {
    return cab.isReady();
  }

  public void checkChannelAvailable(View view) {
    Toast.makeText(this, "Is Ready: " + checkChannelAvailable(), Toast.LENGTH_SHORT)
        .show();
  }

  public void onOpenChannelButtonClicked(View view) {
    cab.startConnection(listener);
  }
}