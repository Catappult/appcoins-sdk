package com.appcoins.sdk.billing.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.appcoins.billing.AppcoinsBilling;
import com.appcoins.billing.sdk.BuildConfig;
import com.appcoins.communication.SyncIpcMessageRequester;
import com.appcoins.communication.requester.MessageRequesterFactory;
import com.appcoins.sdk.billing.BuyItemProperties;
import com.appcoins.sdk.billing.DeveloperPayload;
import com.appcoins.sdk.billing.ResponseCode;
import com.appcoins.sdk.billing.SharedPreferencesRepository;
import com.appcoins.sdk.billing.SkuDetails;
import com.appcoins.sdk.billing.SkuDetailsResult;
import com.appcoins.sdk.billing.UriCommunicationAppcoinsBilling;
import com.appcoins.sdk.billing.WSServiceController;
import com.appcoins.sdk.billing.WalletBinderUtil;
import com.appcoins.sdk.billing.listeners.StartPurchaseAfterBindListener;
import com.appcoins.sdk.billing.payasguest.BillingRepository;
import com.appcoins.sdk.billing.payflow.PayflowManager;
import com.appcoins.sdk.billing.payflow.PaymentFlowMethod;
import com.appcoins.sdk.billing.service.BdsService;
import com.indicative.client.android.Indicative;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import static com.appcoins.sdk.billing.helpers.WalletUtils.context;

public final class AppcoinsBillingStubHelper implements AppcoinsBilling, Serializable {
  final static String INAPP_PURCHASE_ID_LIST = "INAPP_PURCHASE_ID_LIST";
  final static String INAPP_PURCHASE_ITEM_LIST = "INAPP_PURCHASE_ITEM_LIST";
  final static String INAPP_PURCHASE_DATA_LIST = "INAPP_PURCHASE_DATA_LIST";
  final static String INAPP_DATA_SIGNATURE_LIST = "INAPP_DATA_SIGNATURE_LIST";
  private static final String TAG = AppcoinsBillingStubHelper.class.getSimpleName();
  private static AppcoinsBilling serviceAppcoinsBilling;
  private static AppcoinsBillingStubHelper appcoinsBillingStubHelper;
  private static int SUPPORTED_API_VERSION = 3;
  private static int MAX_SKUS_SEND_WS = 49; // 0 to 49
  private static SkuDetails skuDetails;
  private static BuyItemProperties buyItemProperties;

  private AppcoinsBillingStubHelper() {
    appcoinsBillingStubHelper = this;
  }

  public static AppcoinsBillingStubHelper getInstance() {
    if (appcoinsBillingStubHelper == null) {
      appcoinsBillingStubHelper = new AppcoinsBillingStubHelper();
      Indicative.launch(context, BuildConfig.INDICATIVE_API_KEY);
    }
    return appcoinsBillingStubHelper;
  }

  @Override public int isBillingSupported(int apiVersion, String packageName, String type) {
    int responseCode = ResponseCode.SERVICE_UNAVAILABLE.getValue();
    if (isDeviceVersionSupported()) {
      if (WalletUtils.hasBillingServiceInstalled()) {
        try {
          responseCode = serviceAppcoinsBilling.isBillingSupported(apiVersion, packageName, type);
        } catch (RemoteException e) {
          e.printStackTrace();
        }
      } else if (isTypeSupported(type, apiVersion)) {
        responseCode = ResponseCode.OK.getValue();
      }
    }
    return responseCode;
  }

  @Override
  public Bundle getSkuDetails(final int apiVersion, final String packageName, final String type,
      final Bundle skusBundle) {
    final CountDownLatch latch = new CountDownLatch(1);
    final Bundle responseWs = new Bundle();
    if (WalletUtils.hasBillingServiceInstalled()) {
      try {
        return serviceAppcoinsBilling.getSkuDetails(apiVersion, packageName, type, skusBundle);
      } catch (RemoteException e) {
        e.printStackTrace();
        responseWs.putInt(Utils.RESPONSE_CODE, ResponseCode.SERVICE_UNAVAILABLE.getValue());
      }
    } else {
      if (Looper.myLooper() == Looper.getMainLooper()) {
        Thread t = new Thread(new Runnable() {
          @Override public void run() {
            getSkuDetailsFromService(packageName, type, skusBundle, responseWs);
            latch.countDown();
          }
        });
        t.start();
        try {
          latch.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
          responseWs.putInt(Utils.RESPONSE_CODE, ResponseCode.SERVICE_UNAVAILABLE.getValue());
        }
      } else {
        getSkuDetailsFromService(packageName, type, skusBundle, responseWs);
      }
    }
    return responseWs;
  }

  @Override public Bundle getBuyIntent(int apiVersion, final String packageName, final String sku,
      final String type, String developerPayload) {

    new PayflowManager(packageName).getPayflowPriority();

    Bundle bundle = new Bundle();
    if (WalletUtils.hasBillingServiceInstalled()) {
      for (PaymentFlowMethod method : WalletUtils.getPayflowMethodsList()) {
        if (method instanceof PaymentFlowMethod.Wallet ||
            method instanceof PaymentFlowMethod.GamesHub) {
          bundle = WalletUtils.startServiceBind(method, serviceAppcoinsBilling,
              apiVersion, sku, type, developerPayload);
          if (bundle != null) {
            break;
          }
        }
      }
      if (bundle == null) {
        bundle = new Bundle();
        bundle.putInt(Utils.RESPONSE_CODE, ResponseCode.SERVICE_UNAVAILABLE.getValue());
      }
      return bundle;
    } else {
      setBuyItemPropertiesForPayflow(packageName, apiVersion, sku, type, developerPayload);
      for (PaymentFlowMethod method : WalletUtils.getPayflowMethodsList()) {
        if (method instanceof PaymentFlowMethod.PayAsAGuest) {
          bundle = WalletUtils.startPayAsGuest(buyItemProperties);
        } else if (method instanceof PaymentFlowMethod.WebFirstPayment) {
          // TODO Perform action for WebFirstPayment
        }
        return bundle;
      }
      return WalletUtils.startInstallFlow(buyItemProperties);
    }
  }

  private void setBuyItemPropertiesForPayflow(String packageName, int apiVersion, String sku,
      String type, String developerPayload) {

    if (Looper.myLooper() == Looper.getMainLooper()) {
      new Handler(Looper.getMainLooper()).post(new Runnable() {
        @Override public void run() {
          skuDetails = getMappedSkuDetails(sku, packageName, type);
        }
      });
    } else {
      skuDetails = getMappedSkuDetails(sku, packageName, type);
    }

    DeveloperPayload developerPayloadObject =
        new DeveloperPayload(developerPayload, PayloadHelper.getPayload(developerPayload),
            PayloadHelper.getOrderReference(developerPayload),
            PayloadHelper.getOrigin(developerPayload));

    buyItemProperties =
        new BuyItemProperties(apiVersion, packageName, sku, type, developerPayloadObject,
            skuDetails);
  }

  @Override public Bundle getPurchases(int apiVersion, final String packageName, String type,
      String continuationToken) {
    Bundle bundleResponse = buildEmptyBundle();
    if (WalletUtils.hasBillingServiceInstalled()) {
      try {
        return serviceAppcoinsBilling.getPurchases(apiVersion, packageName, type,
            continuationToken);
      } catch (RemoteException e) {
        e.printStackTrace();
        bundleResponse.putInt(Utils.RESPONSE_CODE, ResponseCode.SERVICE_UNAVAILABLE.getValue());
      }
    } else {
      String walletId = getWalletId();
      if (walletId != null && type.equalsIgnoreCase("INAPP")) {
        BillingRepository billingRepository =
            new BillingRepository(new BdsService(BuildConfig.HOST_WS, 30000));
        GuestPurchasesInteract guestPurchaseInteract =
            new GuestPurchasesInteract(billingRepository);

        bundleResponse =
            guestPurchaseInteract.mapGuestPurchases(bundleResponse, walletId, packageName, type);
      }
    }
    return bundleResponse;
  }

  @Override public int consumePurchase(int apiVersion, String packageName, String purchaseToken) {
    int responseCode = ResponseCode.SERVICE_UNAVAILABLE.getValue();
    try {
      if (WalletUtils.hasBillingServiceInstalled()) {
        responseCode =
            serviceAppcoinsBilling.consumePurchase(apiVersion, packageName, purchaseToken);
      } else {
        String walletId = getWalletId();
        if (walletId != null && apiVersion == SUPPORTED_API_VERSION) {
          responseCode = consumeGuestPurchase(walletId, packageName, purchaseToken);
        } else {
          responseCode = ResponseCode.OK.getValue();
        }
      }
    } catch (RemoteException e) {
      e.printStackTrace();
    }
    return responseCode;
  }

  private int consumeGuestPurchase(String walletId, String packageName, String purchaseToken) {
    BillingRepository billingRepository =
        new BillingRepository(new BdsService(BuildConfig.HOST_WS, BdsService.TIME_OUT_IN_MILLIS));
    GuestPurchasesInteract guestPurchaseInteract = new GuestPurchasesInteract(billingRepository);

    return guestPurchaseInteract.consumeGuestPurchase(walletId, packageName, purchaseToken);
  }

  private Bundle buildEmptyBundle() {
    Bundle bundleResponse = new Bundle();
    bundleResponse.putInt(Utils.RESPONSE_CODE, ResponseCode.OK.getValue());
    bundleResponse.putStringArrayList(INAPP_PURCHASE_ITEM_LIST, new ArrayList<String>());
    bundleResponse.putStringArrayList(INAPP_PURCHASE_DATA_LIST, new ArrayList<String>());
    bundleResponse.putStringArrayList(INAPP_DATA_SIGNATURE_LIST, new ArrayList<String>());
    return bundleResponse;
  }

  private void getSkuDetailsFromService(String packageName, String type, Bundle skusBundle,
      Bundle responseWs) {
    List<String> sku = skusBundle.getStringArrayList(Utils.GET_SKU_DETAILS_ITEM_LIST);
    ArrayList<SkuDetails> skuDetailsList = requestSkuDetails(sku, packageName, type);
    SkuDetailsResult skuDetailsResult = new SkuDetailsResult(skuDetailsList, 0);
    responseWs.putInt(Utils.RESPONSE_CODE, 0);
    ArrayList<String> skuDetails = buildResponse(skuDetailsResult);
    responseWs.putStringArrayList("DETAILS_LIST", skuDetails);
  }

  private SkuDetails getSingleSkuDetailsFromService(String packageName, String type,
      Bundle skusBundle) {
    List<String> sku = skusBundle.getStringArrayList(Utils.GET_SKU_DETAILS_ITEM_LIST);
    return requestSingleSkuDetails(sku, packageName, type);
  }

  private ArrayList<SkuDetails> requestSkuDetails(List<String> sku, String packageName,
      String type) {
    List<String> skuSendList = new ArrayList<>();
    ArrayList<SkuDetails> skuDetailsList = new ArrayList<>();

    for (int i = 1; i <= sku.size(); i++) {
      skuSendList.add(sku.get(i - 1));
      if (i % MAX_SKUS_SEND_WS == 0 || i == sku.size()) {
        String response =
            WSServiceController.getSkuDetailsService(BuildConfig.HOST_WS, packageName, skuSendList,
                WalletUtils.getUserAgent());
        skuDetailsList.addAll(AndroidBillingMapper.mapSkuDetailsFromWS(type, response));
        skuSendList.clear();
      }
    }
    return skuDetailsList;
  }

  private SkuDetails requestSingleSkuDetails(List<String> sku, String packageName, String type) {
    String response =
        WSServiceController.getSkuDetailsService(BuildConfig.HOST_WS, packageName, sku,
            WalletUtils.getUserAgent());
    return AndroidBillingMapper.mapSingleSkuDetails(type, response);
  }

  private SkuDetails getMappedSkuDetails(String sku, String packageName, String type) {
    List<String> skuList = new ArrayList<>();
    skuList.add(sku);
    Bundle skuBundle = AndroidBillingMapper.mapArrayListToBundleSkuDetails(skuList);
    return getSingleSkuDetailsFromService(packageName, type, skuBundle);
  }

  private ArrayList<String> buildResponse(SkuDetailsResult skuDetailsResult) {
    ArrayList<String> list = new ArrayList<>();
    for (SkuDetails skuDetails : skuDetailsResult.getSkuDetailsList()) {
      list.add(AndroidBillingMapper.mapSkuDetailsResponse(skuDetails));
    }
    return list;
  }

  @Override public IBinder asBinder() {
    return null;
  }

  public void createRepository(
      final StartPurchaseAfterBindListener startPurchaseAfterConnectionListener) {

    String packageName = WalletUtils.getBillingServicePackageName();
    String iabAction = WalletUtils.getBillingServiceIabAction();

    Intent serviceIntent = new Intent(iabAction);
    serviceIntent.setPackage(packageName);

    if (WalletUtils.isAppAvailableToBind(iabAction)) {
      WalletBinderUtil.bindService(context, serviceIntent, new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName name, IBinder service) {
          serviceAppcoinsBilling = Stub.asInterface(service);
          startPurchaseAfterConnectionListener.startPurchaseAfterBind();
          Log.d(TAG, "onServiceConnected() called service = [" + serviceAppcoinsBilling + "]");
        }

        @Override public void onServiceDisconnected(ComponentName name) {
          Log.d(TAG, "onServiceDisconnected() called = [" + name + "]");
        }
      }, Context.BIND_AUTO_CREATE);
    }
  }

  private boolean hasRequiredFields(String type, String sku) {
    return type.equalsIgnoreCase("inapp") && sku != null && !sku.isEmpty();
  }

  private String getWalletId() {
    SharedPreferencesRepository sharedPreferencesRepository =
        new SharedPreferencesRepository(WalletUtils.getContext(),
            SharedPreferencesRepository.TTL_IN_SECONDS);
    return sharedPreferencesRepository.getWalletId();
  }

  private boolean isTypeSupported(String type, int apiVersion) {
    return type.equalsIgnoreCase("inapp") && apiVersion == SUPPORTED_API_VERSION;
  }

  private boolean isDeviceVersionSupported() {
    return Build.VERSION.SDK_INT >= BuildConfig.MIN_SDK_VERSION;
  }

  public static abstract class Stub {
    public static AppcoinsBilling asInterface(IBinder service) {
      Log.d(TAG, "Stub: asInterface: bindType " + WalletBinderUtil.getBindType() + " service " + service);

      if (WalletBinderUtil.getBindType() == BindType.BILLING_SERVICE_NOT_INSTALLED) {
        return AppcoinsBillingStubHelper.getInstance();
      } else {
        SharedPreferencesRepository sharedPreferencesRepository =
            new SharedPreferencesRepository(WalletUtils.getContext(),
                SharedPreferencesRepository.TTL_IN_SECONDS);
        AppcoinsBilling appcoinsBilling;
        if (WalletBinderUtil.getBindType() == BindType.URI_CONNECTION) {
          String priorityPackage = BuildConfig.APPCOINS_WALLET_PACKAGE_NAME;
          if (!WalletUtils.getPayflowMethodsList().isEmpty()) {
            priorityPackage = WalletUtils.getPayflowMethodsList().get(0).getName();
          }
          SyncIpcMessageRequester messageRequester =
              MessageRequesterFactory.create(WalletUtils.getLifecycleActivityProvider(),
                  priorityPackage, "appcoins://billing/communication/processor/1",
                  "appcoins://billing/communication/requester/1", BdsService.TIME_OUT_IN_MILLIS);
          appcoinsBilling = new UriCommunicationAppcoinsBilling(messageRequester);
        } else {
          appcoinsBilling = AppcoinsBilling.Stub.asInterface(service);
        }
        return new AppcoinsBillingWrapper(appcoinsBilling,
            AppCoinsPendingIntentCaller.getInstance(), sharedPreferencesRepository.getWalletId(),
            BdsService.TIME_OUT_IN_MILLIS);
      }
    }
  }
}