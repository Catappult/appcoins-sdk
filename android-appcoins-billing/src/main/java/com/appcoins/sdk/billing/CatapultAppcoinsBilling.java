package com.appcoins.sdk.billing;

import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESULT_CODE;
import static com.appcoins.sdk.core.logger.Logger.logDebug;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Looper;

import com.appcoins.sdk.billing.activities.BillingFlowActivity;
import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import com.appcoins.sdk.billing.helpers.AppCoinsPendingIntentCaller;
import com.appcoins.sdk.billing.helpers.EventLogger;
import com.appcoins.sdk.billing.helpers.PayloadHelper;
import com.appcoins.sdk.billing.helpers.UpdateDialogActivity;
import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.PendingPurchaseStream;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;
import com.appcoins.sdk.billing.sharedpreferences.AttributionSharedPreferences;
import com.appcoins.sdk.billing.usecases.ingameupdates.IsUpdateAvailable;
import com.appcoins.sdk.billing.usecases.ingameupdates.LaunchAppUpdate;

import org.jetbrains.annotations.Nullable;

import kotlin.Pair;

public class CatapultAppcoinsBilling implements AppcoinsBillingClient, PendingPurchaseStream.Consumer<Pair<Activity, BuyItemProperties>> {

  private final Billing billing;
  private final RepositoryConnection connection;
  private final PurchasesUpdatedListener purchaseFinishedListener;

  public CatapultAppcoinsBilling(Billing billing, RepositoryConnection connection,
      PurchasesUpdatedListener purchaseFinishedListener) {
    this.billing = billing;
    this.connection = connection;
    this.purchaseFinishedListener = purchaseFinishedListener;
  }

  @Override public PurchasesResult queryPurchases(String skuType) {
    return billing.queryPurchases(skuType);
  }

  @Override public void querySkuDetailsAsync(SkuDetailsParams skuDetailsParams,
      SkuDetailsResponseListener onSkuDetailsResponseListener) {
    billing.querySkuDetailsAsync(skuDetailsParams, onSkuDetailsResponseListener);
  }

  @Override
  public void consumeAsync(String token, ConsumeResponseListener consumeResponseListener) {
    billing.consumeAsync(token, consumeResponseListener);
  }

  @Override public int launchBillingFlow(Activity activity, BillingFlowParams billingFlowParams) {

    int responseCode;

    try {
      WalletUtils.getSdkAnalytics().sendPurchaseIntentEvent(billingFlowParams.getSku());
      String payload = PayloadHelper.buildIntentPayload(billingFlowParams.getOrderReference(),
          billingFlowParams.getDeveloperPayload(), billingFlowParams.getOrigin());
      AttributionSharedPreferences attributionSharedPreferences =
          new AttributionSharedPreferences(activity);
      String oemid = attributionSharedPreferences.getOemId();
      String guestWalletId = attributionSharedPreferences.getWalletId();

      logDebug("Message: " + payload);

      Thread eventLoggerThread = new Thread(new EventLogger(billingFlowParams.getSku(),
          activity.getApplicationContext()
              .getPackageName()));
      eventLoggerThread.start();

      LaunchBillingFlowResult launchBillingFlowResult =
          billing.launchBillingFlow(billingFlowParams, payload, oemid, guestWalletId);

      responseCode = launchBillingFlowResult.getResponseCode();

      if (responseCode != ResponseCode.OK.getValue()) {
        ApplicationUtils.handleActivityResult(
                billing,
                ResponseCode.ERROR.getValue(),
                null,
                purchaseFinishedListener
        );
        return responseCode;
      }

      PendingIntent buyIntent = launchBillingFlowResult.getBuyIntent();
      Intent webBuyIntent = launchBillingFlowResult.getWebBuyIntent();

        PaymentsResultsManager.getInstance().collectPaymentResult(this);
      if (buyIntent != null) {
          activity.startActivity(BillingFlowActivity.newIntent(activity, buyIntent));
          /*
        AppCoinsPendingIntentCaller.startPendingAppCoinsIntent(activity,
          buyIntent.getIntentSender(), REQUEST_CODE, null, 0, 0, 0);*/
      } else if (webBuyIntent != null) {
        WalletUtils.getSdkAnalytics().sendPurchaseViaWebEvent(billingFlowParams.getSku());
        activity.startActivity(webBuyIntent);
      }
    } catch (NullPointerException | ActivityNotFoundException e) {
      return handleErrorTypeResponse(ResponseCode.ERROR.getValue(), e);
    } catch (ServiceConnectionException e) {
      return handleErrorTypeResponse(ResponseCode.SERVICE_UNAVAILABLE.getValue(), e);
    }
    return ResponseCode.OK.getValue();
  }

  private int handleErrorTypeResponse(int value, Exception e) {
    e.printStackTrace();
    ApplicationUtils.handleActivityResult(
            billing,
            ResponseCode.ERROR.getValue(),
            null,
            purchaseFinishedListener
    );
    return value;
  }

  @Override public void startConnection(final AppCoinsBillingStateListener listener) {
    if (!isReady()) {
      PendingPurchaseStream.getInstance().collect(this);
      connection.startConnection(listener);
    }
  }

  @Override public void endConnection() {
    if (isReady()) {
      PendingPurchaseStream.getInstance().stopCollecting();
      connection.endConnection();
    }
  }

  @Override public boolean isReady() {
    return billing.isReady();
  }

    @Override
    public boolean isAppUpdateAvailable() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            return false;
        } else {
            return IsUpdateAvailable.INSTANCE.invoke(WalletUtils.context);
        }
    }

    @Override
    public void launchAppUpdateStore(Context context) {
        Runnable runnable = () -> {
            if (isAppUpdateAvailable()) {
                LaunchAppUpdate.INSTANCE.invoke(context);
            }
        };
        new Thread(runnable).start();
    }

    @Override
    public void launchAppUpdateDialog(Context context) {
        Runnable runnable = () -> {
            if (isAppUpdateAvailable()) {
                Intent updateDialogActivityIntent =
                        new Intent(context.getApplicationContext(), UpdateDialogActivity.class);
                updateDialogActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.getApplicationContext().startActivity(updateDialogActivityIntent);
            }
        };
        new Thread(runnable).start();
    }

    @Deprecated
  @Override public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
      return true;
  }

  public Billing getBilling() {
    return billing;
  }

  public PurchasesUpdatedListener getPurchaseFinishedListener() {
    return purchaseFinishedListener;
  }

  @Override
  public void accept(@Nullable Pair<Activity, BuyItemProperties> value) {
    Runnable runnable = () -> {
      Looper.prepare();
      resumeBillingFlow(value.component1(), value.component2().toBillingFlowParams());
      Looper.loop();
    };
    new Thread(runnable).start();
  }

    private void resumeBillingFlow(Activity activity, BillingFlowParams billingFlowParams) {
        int responseCode;
        try {
            String payload =
                    PayloadHelper.buildIntentPayload(
                            billingFlowParams.getOrderReference(),
                            billingFlowParams.getDeveloperPayload(),
                            billingFlowParams.getOrigin()
                    );
            AttributionSharedPreferences attributionSharedPreferences =
                    new AttributionSharedPreferences(activity);
            String oemid = attributionSharedPreferences.getOemId();
            String guestWalletId = attributionSharedPreferences.getWalletId();

            logDebug("Message: " + payload);

            Thread eventLoggerThread =
                    new Thread(
                            new EventLogger(
                                    billingFlowParams.getSku(),
                                    activity.getApplicationContext().getPackageName()
                            )
                    );
            eventLoggerThread.start();

            LaunchBillingFlowResult launchBillingFlowResult =
                    billing.launchBillingFlow(billingFlowParams, payload, oemid, guestWalletId);

            responseCode = launchBillingFlowResult.getResponseCode();

            if (responseCode != ResponseCode.OK.getValue()) {
                ApplicationUtils.handleActivityResult(
                        billing,
                        ResponseCode.ERROR.getValue(),
                        null,
                        purchaseFinishedListener
                );
                return;
            }

            PendingIntent buyIntent = launchBillingFlowResult.getBuyIntent();
            Intent webBuyIntent = launchBillingFlowResult.getWebBuyIntent();

            PaymentsResultsManager.getInstance().collectPaymentResult(this);
            if (buyIntent != null) {
                AppCoinsPendingIntentCaller.startPendingAppCoinsIntent(activity,
                        buyIntent.getIntentSender(), RESULT_CODE, null, 0, 0, 0);
            } else if (webBuyIntent != null) {
                activity.startActivity(webBuyIntent);
            }
        } catch (NullPointerException | IntentSender.SendIntentException e) {
            handleErrorTypeResponse(ResponseCode.ERROR.getValue(), e);
        } catch (ServiceConnectionException e) {
            handleErrorTypeResponse(ResponseCode.SERVICE_UNAVAILABLE.getValue(), e);
        }
    }
}



