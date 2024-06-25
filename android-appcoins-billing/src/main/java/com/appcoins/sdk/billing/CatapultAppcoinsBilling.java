package com.appcoins.sdk.billing;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Looper;
import android.util.Log;

import com.appcoins.sdk.billing.exceptions.ServiceConnectionException;
import com.appcoins.sdk.billing.helpers.AppCoinsPendingIntentCaller;
import com.appcoins.sdk.billing.helpers.EventLogger;
import com.appcoins.sdk.billing.helpers.PayloadHelper;
import com.appcoins.sdk.billing.helpers.WalletUtils;
import com.appcoins.sdk.billing.listeners.AppCoinsBillingStateListener;
import com.appcoins.sdk.billing.listeners.ConsumeResponseListener;
import com.appcoins.sdk.billing.listeners.PendingPurchaseStream;
import com.appcoins.sdk.billing.listeners.SDKWebResponse;
import com.appcoins.sdk.billing.listeners.SkuDetailsResponseListener;

import org.jetbrains.annotations.Nullable;

import kotlin.Pair;

public class CatapultAppcoinsBilling implements AppcoinsBillingClient, PendingPurchaseStream.Consumer<Pair<Activity, BuyItemProperties>> {
  private static final int REQUEST_CODE = 51;

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

      Log.d("Message: ", payload);

      Thread eventLoggerThread = new Thread(new EventLogger(billingFlowParams.getSku(),
          activity.getApplicationContext()
              .getPackageName()));
      eventLoggerThread.start();

      LaunchBillingFlowResult launchBillingFlowResult =
          billing.launchBillingFlow(billingFlowParams, payload);

      responseCode = launchBillingFlowResult.getResponseCode();

      if (responseCode != ResponseCode.OK.getValue()) {
        ApplicationUtils.handleWebBasedResult(
                new SDKWebResponse(ResponseCode.ERROR.getValue(), null, null),
                billingFlowParams,
                billingFlowParams.getDeveloperPayload(),
                purchaseFinishedListener
        );
        return responseCode;
      }

      PendingIntent buyIntent = launchBillingFlowResult.getBuyIntent();
      Intent webBuyIntent = launchBillingFlowResult.getWebBuyIntent();

      if (buyIntent != null) {
        AppCoinsPendingIntentCaller.startPendingAppCoinsIntent(activity,
          buyIntent.getIntentSender(), REQUEST_CODE, null, 0, 0, 0);
      } else if (webBuyIntent != null) {
        WalletUtils.getSdkAnalytics().sendPurchaseViaWebEvent(billingFlowParams.getSku());
        PaymentsResultsManager.getInstance()
                .collectPaymentResult(
                        billingFlowParams,
                        payload,
                        this
                );
        activity.startActivity(webBuyIntent);
      }
    } catch (NullPointerException e) {
      return handleErrorTypeResponse(ResponseCode.ERROR.getValue(), e, billingFlowParams);
    } catch (IntentSender.SendIntentException e) {
      return handleErrorTypeResponse(ResponseCode.ERROR.getValue(), e, billingFlowParams);
    } catch (ActivityNotFoundException e) {
      return handleErrorTypeResponse(ResponseCode.ERROR.getValue(), e, billingFlowParams);
    } catch (ServiceConnectionException e) {
      return handleErrorTypeResponse(ResponseCode.SERVICE_UNAVAILABLE.getValue(), e, billingFlowParams);
    }
    return ResponseCode.OK.getValue();
  }

  private int handleErrorTypeResponse(int value, Exception e, BillingFlowParams billingFlowParams) {
    e.printStackTrace();
    ApplicationUtils.handleWebBasedResult(
            new SDKWebResponse(ResponseCode.ERROR.getValue(), null, null),
            billingFlowParams,
            billingFlowParams.getDeveloperPayload(),
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

  @Override public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE) {
      ApplicationUtils.handleActivityResult(billing, resultCode, data, purchaseFinishedListener);
      return true;
    }
    return false;
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

            Log.d("Message: ", payload);

            Thread eventLoggerThread =
                    new Thread(
                            new EventLogger(
                                    billingFlowParams.getSku(),
                                    activity.getApplicationContext().getPackageName()
                            )
                    );
            eventLoggerThread.start();

            LaunchBillingFlowResult launchBillingFlowResult =
                    billing.launchBillingFlow(billingFlowParams, payload);

            responseCode = launchBillingFlowResult.getResponseCode();

            if (responseCode != ResponseCode.OK.getValue()) {
                ApplicationUtils.handleWebBasedResult(
                        new SDKWebResponse(ResponseCode.ERROR.getValue(), null, null),
                        billingFlowParams,
                        billingFlowParams.getDeveloperPayload(),
                        purchaseFinishedListener
                );
                return;
            }

            PendingIntent buyIntent = launchBillingFlowResult.getBuyIntent();
            Intent webBuyIntent = launchBillingFlowResult.getWebBuyIntent();

            if (buyIntent != null) {
                AppCoinsPendingIntentCaller.startPendingAppCoinsIntent(activity,
                        buyIntent.getIntentSender(), REQUEST_CODE, null, 0, 0, 0);
            } else if (webBuyIntent != null) {
                PaymentsResultsManager.getInstance()
                        .collectPaymentResult(
                                billingFlowParams,
                                payload,
                                this
                        );
                activity.startActivity(webBuyIntent);
            }
        } catch (NullPointerException e) {
            handleErrorTypeResponse(ResponseCode.ERROR.getValue(), e, billingFlowParams);
        } catch (IntentSender.SendIntentException e) {
            handleErrorTypeResponse(ResponseCode.ERROR.getValue(), e, billingFlowParams);
        } catch (ServiceConnectionException e) {
            handleErrorTypeResponse(ResponseCode.SERVICE_UNAVAILABLE.getValue(), e, billingFlowParams);
        }
    }
}



