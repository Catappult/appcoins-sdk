package com.appcoins.sdk.billing.payasguest;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.sdk.billing.BuyItemProperties;
import com.appcoins.sdk.billing.DeveloperPayload;
import com.appcoins.sdk.billing.analytics.AdyenAnalyticsInteract;
import com.appcoins.sdk.billing.analytics.BillingAnalytics;
import com.appcoins.sdk.billing.listeners.NoInfoResponseListener;
import com.appcoins.sdk.billing.listeners.billing.GetTransactionListener;
import com.appcoins.sdk.billing.listeners.billing.LoadPaymentInfoListener;
import com.appcoins.sdk.billing.listeners.billing.MakePaymentListener;
import com.appcoins.sdk.billing.listeners.billing.PurchaseListener;
import com.appcoins.sdk.billing.managers.ApiKeysManager;
import com.appcoins.sdk.billing.mappers.BillingMapper;
import com.appcoins.sdk.billing.models.Transaction.Status;
import com.appcoins.sdk.billing.models.billing.AdyenPaymentInfo;
import com.appcoins.sdk.billing.models.billing.AdyenPaymentMethodsModel;
import com.appcoins.sdk.billing.models.billing.AdyenPaymentParams;
import com.appcoins.sdk.billing.models.billing.AdyenTransactionModel;
import com.appcoins.sdk.billing.models.billing.PurchaseModel;
import com.appcoins.sdk.billing.models.billing.TransactionInformation;
import com.appcoins.sdk.billing.models.billing.TransactionModel;
import com.appcoins.sdk.billing.service.adyen.AdyenPaymentMethod;
import com.sdk.appcoins_adyen.card.EncryptedCard;
import com.sdk.appcoins_adyen.encryption.CardEncryptorImpl;
import com.sdk.appcoins_adyen.models.ExpiryDate;
import com.sdk.appcoins_adyen.utils.CardValidationUtils;
import com.sdk.appcoins_adyen.utils.RedirectUtils;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

class AdyenPaymentPresenter {

  private final static String WAITING_RESULT_KEY = "waiting_result";
  private final AdyenPaymentView fragmentView;
  private final AdyenPaymentInfo adyenPaymentInfo;
  private final AdyenPaymentInteract adyenPaymentInteract;
  private AdyenErrorCodeMapper adyenErrorCodeMapper;
  private AdyenAnalyticsInteract analytics;
  private String returnUrl;
  private boolean waitingResult;
  private Map<Handler, Runnable> handlerRunnableMap;

  AdyenPaymentPresenter(AdyenPaymentView fragmentView, AdyenPaymentInfo adyenPaymentInfo,
      AdyenPaymentInteract adyenPaymentInteract, AdyenAnalyticsInteract adyenAnalyticsInteract,
      AdyenErrorCodeMapper adyenErrorCodeMapper, String returnUrl) {
    this.fragmentView = fragmentView;
    this.adyenPaymentInfo = adyenPaymentInfo;
    this.adyenPaymentInteract = adyenPaymentInteract;
    this.adyenErrorCodeMapper = adyenErrorCodeMapper;
    this.analytics = adyenAnalyticsInteract;
    this.returnUrl = returnUrl;
    this.handlerRunnableMap = new HashMap<>();
    waitingResult = false;
  }

  void loadPaymentInfo() {
    if (!adyenPaymentInfo.shouldResumeTransaction()) {
      if (!waitingResult) {
        fragmentView.showLoading();
        AdyenPaymentMethod method = mapPaymentToService(adyenPaymentInfo.getPaymentMethod());
        LoadPaymentInfoListener loadPaymentInfoListener = createLoadPaymentInfoListener();
        adyenPaymentInteract.loadPaymentInfo(method, adyenPaymentInfo.getFiatPrice(),
            adyenPaymentInfo.getFiatCurrency(), adyenPaymentInfo.getWalletAddress(),
            loadPaymentInfoListener);
      }
    } else {
      resumeTransaction(adyenPaymentInfo.getToResumeTransactionId());
    }
  }

  void onSaveInstanceState(Bundle outState) {
    outState.putBoolean(WAITING_RESULT_KEY, waitingResult);
  }

  void onSavedInstance(Bundle savedInstance) {
    waitingResult = savedInstance.getBoolean(WAITING_RESULT_KEY);
  }

  void onPositiveClick(String cardNumber, String expiryDate, String cvv, String storedPaymentId,
      BigDecimal serverFiatPrice, String serverCurrency) {
    fragmentView.lockRotation();
    fragmentView.showLoading();
    fragmentView.disableBack();
    CardEncryptorImpl cardEncryptor = new CardEncryptorImpl(ApiKeysManager.INSTANCE.getAdyenApiKey());
    ExpiryDate mExpiryDate = CardValidationUtils.getDate(expiryDate);
    String encryptedCard;
    if (storedPaymentId.equals("")) {
      EncryptedCard encryptedCardModel =
          cardEncryptor.encryptFields(cardNumber, mExpiryDate.getExpiryMonth(),
              mExpiryDate.getExpiryYear(), cvv);
      encryptedCard = new EncryptedCardMapper().map(encryptedCardModel);
    } else {
      encryptedCard = cardEncryptor.encryptStoredPaymentFields(cvv, storedPaymentId, "scheme");
    }
    analytics.sendConfirmationEvent(adyenPaymentInfo, BillingAnalytics.EVENT_BUY);
    makePayment(encryptedCard, serverFiatPrice, serverCurrency);
  }

  void onCancelClick() {
    analytics.sendConfirmationEvent(adyenPaymentInfo, BillingAnalytics.EVENT_CANCEL);
    fragmentView.close(false);
  }

  void onErrorButtonClick() {
    fragmentView.close(true);
  }

  void onChangeCardClick() {
    fragmentView.showLoading();
    NoInfoResponseListener noInfoResponseListener = new NoInfoResponseListener() {
      @Override public void onResponse(boolean error) {
        if (error) {
          fragmentView.showError();
        } else {
          fragmentView.clearCreditCardInput();
          fragmentView.showCreditCardView(null);
        }
      }
    };
    adyenPaymentInteract.forgetCard(adyenPaymentInfo.getWalletAddress(), noInfoResponseListener);
  }

  void onMorePaymentsClick() {
    fragmentView.navigateToPaymentSelection();
  }

  private void makePayment(String encryptedCard, BigDecimal serverFiatPrice,
      String serverCurrency) {
    BuyItemProperties buyItemProperties = adyenPaymentInfo.getBuyItemProperties();
    DeveloperPayload developerPayload = buyItemProperties.getDeveloperPayload();
    AdyenPaymentMethod method = mapPaymentToService(adyenPaymentInfo.getPaymentMethod());
    String packageName = buyItemProperties.getPackageName();
    MakePaymentListener makePaymentListener = new MakePaymentListener() {
      @Override public void onResponse(AdyenTransactionModel adyenTransactionModel) {
        onMakePaymentResponse(adyenTransactionModel);
      }
    };
    AdyenPaymentParams adyenPaymentParams = new AdyenPaymentParams(encryptedCard, true, returnUrl);
    TransactionInformation transactionInformation =
        new TransactionInformation(serverFiatPrice.toString(), serverCurrency,
            developerPayload.getOrderReference(), method.getTransactionType(), "BDS", packageName,
            developerPayload.getDeveloperPayload(), buyItemProperties.getSku(), null,
            buyItemProperties.getType());
    adyenPaymentInteract.makePayment(adyenPaymentParams, transactionInformation,
        adyenPaymentInfo.getWalletAddress(), packageName, makePaymentListener);
  }

  private void launchPayment(AdyenPaymentMethodsModel adyenPaymentMethodsModel) {
    if (adyenPaymentInfo.getPaymentMethod()
        .equals(IabActivity.CREDIT_CARD)) {
      fragmentView.showCreditCardView(adyenPaymentMethodsModel.getStoredMethodDetails());
    } else {
      fragmentView.lockRotation();
      fragmentView.showLoading();
      launchPaypal(adyenPaymentMethodsModel);
    }
  }

  private void launchPaypal(AdyenPaymentMethodsModel paymentMethod) {
    BuyItemProperties buyItemProperties = adyenPaymentInfo.getBuyItemProperties();
    DeveloperPayload developerPayload = buyItemProperties.getDeveloperPayload();
    AdyenPaymentMethod method = mapPaymentToService(adyenPaymentInfo.getPaymentMethod());
    String packageName = buyItemProperties.getPackageName();
    MakePaymentListener makePaymentListener = new MakePaymentListener() {
      @Override public void onResponse(AdyenTransactionModel adyenTransactionModel) {
        if (!waitingResult) {
          handlePaypalModel(adyenTransactionModel);
        }
      }
    };
    AdyenPaymentParams adyenPaymentParams =
        new AdyenPaymentParams(paymentMethod.getPaymentMethod(), false, returnUrl);
    TransactionInformation transactionInformation = new TransactionInformation(
        paymentMethod.getValue()
            .toString(), paymentMethod.getCurrency(), developerPayload.getOrderReference(),
        method.getTransactionType(), "BDS", packageName, developerPayload.getDeveloperPayload(),
        buyItemProperties.getSku(), null, buyItemProperties.getType());
    adyenPaymentInteract.makePayment(adyenPaymentParams, transactionInformation,
        adyenPaymentInfo.getWalletAddress(), packageName, makePaymentListener);
  }

  private void handlePaypalModel(final AdyenTransactionModel adyenTransactionModel) {
    if (adyenTransactionModel.hasError()) {
      fragmentView.showError();
    } else {
      fragmentView.navigateToUri(adyenTransactionModel.getUrl(), adyenTransactionModel.getUid());
      waitingResult = true;
    }
  }

  void onActivityResult(Uri data, String uid, boolean success) {
    if (success) {
      JSONObject details = RedirectUtils.parseRedirectResult(data);
      MakePaymentListener makePaymentListener = new MakePaymentListener() {
        @Override public void onResponse(AdyenTransactionModel adyenTransactionModel) {
          onMakePaymentResponse(adyenTransactionModel);
        }
      };
      adyenPaymentInteract.submitRedirect(uid, adyenPaymentInfo.getWalletAddress(), details, null,
          makePaymentListener);
      fragmentView.disableBack();
    } else {
      analytics.sendConfirmationEvent(adyenPaymentInfo, BillingAnalytics.EVENT_CANCEL);
      fragmentView.close(false);
    }
  }

  void onHelpTextClicked() {
    BuyItemProperties buyItemProperties = adyenPaymentInfo.getBuyItemProperties();
    String packageName = buyItemProperties.getPackageName();
    String walletAddress = adyenPaymentInfo.getWalletAddress();
    String sku = buyItemProperties.getSku();
    String sdkVersionName = BuildConfig.VERSION_NAME;
    int mobileVersion = Build.VERSION.SDK_INT;
    fragmentView.redirectToSupportEmail(walletAddress, packageName, sku, sdkVersionName,
        mobileVersion);
  }

  private void onMakePaymentResponse(AdyenTransactionModel adyenTransactionModel) {
    if (adyenTransactionModel.hasError()) {
      sendGenericErrorEvent(String.valueOf(adyenTransactionModel.getResponseCode()));
      fragmentView.showError();
      fragmentView.enableBack();
    } else {
      handlePaymentResult(adyenTransactionModel.getUid(), adyenTransactionModel.getResultCode(),
          adyenTransactionModel.getRefusalReasonCode(), adyenTransactionModel.getRefusalReason(),
          adyenTransactionModel.getStatus());
    }
  }

  private void handlePaymentResult(String uid, String resultCode, int refusalReasonCode,
      String refusalReason, String status) {
    if (status.equalsIgnoreCase(Status.CANCELED.toString())) {
      analytics.sendConfirmationEvent(adyenPaymentInfo, BillingAnalytics.EVENT_CANCEL);
      fragmentView.close(false);
    } else {
      handleConfirmationEvent();
      if (resultCode.equalsIgnoreCase("AUTHORISED")) {
        handleSuccessAdyenTransaction(uid);
      } else if (isAdyenError(refusalReasonCode, refusalReason)) {
        handleAdyenTransactionError(refusalReason, refusalReasonCode);
      } else {
        sendGenericErrorEvent("UNKNOWN ADYEN ERROR");
        fragmentView.showError();
      }
    }
  }

  private boolean isAdyenError(int refusalReasonCode, String refusalReason) {
    return refusalReason != null && refusalReasonCode != -1;
  }

  private void handleConfirmationEvent() {
    if (adyenPaymentInfo.getPaymentMethod()
        .equals(IabActivity.PAYPAL)) {
      analytics.sendConfirmationEvent(adyenPaymentInfo, BillingAnalytics.EVENT_BUY);
    }
  }

  private void handleAdyenTransactionError(String refusalReason, int refusalReasonCode) {
    sendAdyenPaymentErrorEvent(String.valueOf(refusalReasonCode), refusalReason);
    if (refusalReasonCode == 24) {
      fragmentView.unlockRotation();
      fragmentView.enableBack();
      fragmentView.showCvvError();
    } else {
      String errorMessage = adyenErrorCodeMapper.map(refusalReasonCode);
      fragmentView.showError(errorMessage);
    }
  }

  private void handleSuccessAdyenTransaction(final String uid) {
    GetTransactionListener getTransactionListener = new GetTransactionListener() {
      @Override public void onResponse(TransactionModel transactionModel) {
        if (transactionModel.hasError()) {
          sendGenericErrorEvent(String.valueOf(transactionModel.getResponseCode()));
          fragmentView.showError();
        } else {
          if (isTransactionCompleted(transactionModel.getStatus())) {
            analytics.sendPaymentSuccessEvent(adyenPaymentInfo);
            createBundle(transactionModel);
          } else if (paymentFailed(transactionModel.getStatus())) {
            sendGenericErrorEvent("Transaction Status: " + transactionModel.getStatus());
            fragmentView.showError();
          } else {
            requestTransaction(uid, 5000, this);
          }
        }
      }
    };
    adyenPaymentInteract.getTransaction(uid, adyenPaymentInfo.getWalletAddress(),
        adyenPaymentInfo.getSignature(), getTransactionListener);
  }

  private boolean isTransactionCompleted(Status status) {
    return status == Status.COMPLETED;
  }

  private void createBundle(final TransactionModel transactionModel) {
    PurchaseListener purchaseListener = new PurchaseListener() {
      @Override public void onResponse(PurchaseModel purchaseModel) {
        if (purchaseModel.hasError()) {
          fragmentView.showError();
        } else {
          BillingMapper billingMapper = new BillingMapper();
          final Bundle bundle =
              billingMapper.map(purchaseModel, transactionModel.getOrderReference());
          handleCompletedPurchaseScreen(bundle);
        }
      }
    };
    BuyItemProperties buyItemProperties = adyenPaymentInfo.getBuyItemProperties();
    adyenPaymentInteract.getCompletedPurchaseBundle(buyItemProperties.getType(),
        buyItemProperties.getPackageName(), buyItemProperties.getSku(),
        adyenPaymentInfo.getWalletAddress(), adyenPaymentInfo.getSignature(), purchaseListener);
  }

  private void handleCompletedPurchaseScreen(final Bundle bundle) {
    fragmentView.showCompletedPurchase();
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
      @Override public void run() {
        fragmentView.finish(bundle);
      }
    };
    handlerRunnableMap.put(handler, runnable);
    handler.postDelayed(runnable, 3000);
  }

  private boolean paymentFailed(Status status) {
    return status == Status.FAILED
        || status == Status.CANCELED
        || status == Status.INVALID_TRANSACTION;
  }

  private AdyenPaymentMethod mapPaymentToService(String paymentType) {
    if (paymentType.equals(IabActivity.CREDIT_CARD)) {
      return AdyenPaymentMethod.CREDIT_CARD;
    } else {
      return AdyenPaymentMethod.PAYPAL;
    }
  }

  private void requestTransaction(final String uid, long delayInMillis,
      final GetTransactionListener getTransactionListener) {
    final Handler handler = new Handler();
    Runnable runnable = new Runnable() {
      @Override public void run() {
        adyenPaymentInteract.getTransaction(uid, adyenPaymentInfo.getWalletAddress(),
            adyenPaymentInfo.getSignature(), getTransactionListener);
        handler.removeCallbacks(this);
      }
    };
    handlerRunnableMap.put(handler, runnable);
    handler.postDelayed(runnable, delayInMillis);
  }

  private void sendAdyenPaymentErrorEvent(String refusalCode, String refusalDetails) {
    analytics.sendPaymentErrorEvent(adyenPaymentInfo, null, refusalCode, refusalDetails);
  }

  private void sendGenericErrorEvent(String errorCode) {
    analytics.sendPaymentErrorEvent(adyenPaymentInfo, errorCode, null, null);
  }

  private void resumeTransaction(String toResumeTransactionId) {
    fragmentView.showLoading();
    fragmentView.lockRotation();
    handleSuccessAdyenTransaction(toResumeTransactionId);
  }

  private LoadPaymentInfoListener createLoadPaymentInfoListener() {
    return new LoadPaymentInfoListener() {
      @Override public void onResponse(AdyenPaymentMethodsModel paymentMethodsModel) {
        if (paymentMethodsModel.hasError()) {
          fragmentView.showError();
        } else {
          fragmentView.updateFiatPrice(paymentMethodsModel.getValue(),
              paymentMethodsModel.getCurrency());
          launchPayment(paymentMethodsModel);
        }
      }
    };
  }

  void onDestroy() {
    for (Map.Entry<Handler, Runnable> entry : handlerRunnableMap.entrySet()) {
      entry.getKey()
          .removeCallbacks(entry.getValue());
    }
    adyenPaymentInteract.cancelRequests();
  }
}
