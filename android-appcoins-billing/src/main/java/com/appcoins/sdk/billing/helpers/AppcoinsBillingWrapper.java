package com.appcoins.sdk.billing.helpers;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import com.appcoins.billing.AppcoinsBilling;
import com.appcoins.sdk.billing.ResponseCode;
import com.appcoins.sdk.billing.managers.ProductV2Manager;
import com.appcoins.sdk.billing.managers.VoidedPurchasesManager;
import com.appcoins.sdk.billing.mappers.PurchasesBundleMapper;
import com.appcoins.sdk.billing.mappers.PurchasesResponse;
import com.appcoins.sdk.billing.mappers.VoidedPurchasesBundleMapper;
import com.appcoins.sdk.billing.mappers.VoidedPurchasesResponse;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_DATA_SIGNATURE_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_DATA_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ID_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_PURCHASE_ITEM_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.INAPP_VOIDED_PURCHASE_DATA_LIST;
import static com.appcoins.sdk.billing.utils.AppcoinsBillingConstants.RESPONSE_CODE;
import static com.appcoins.sdk.core.logger.Logger.logWarning;

class AppcoinsBillingWrapper implements AppcoinsBilling, Serializable {

    private final AppcoinsBilling appcoinsBilling;
    private final String walletId;

    AppcoinsBillingWrapper(AppcoinsBilling appcoinsBilling, String walletId) {
        this.appcoinsBilling = appcoinsBilling;
        this.walletId = walletId;
    }

    @Override
    public int isBillingSupported(int apiVersion, String packageName, String type) throws RemoteException {
        pingConnection(apiVersion, packageName, type);
        return appcoinsBilling.isBillingSupported(apiVersion, packageName, type);
    }

    @Override
    public Bundle getSkuDetails(int apiVersion, String packageName, String type, Bundle skusBundle)
        throws RemoteException {
        return appcoinsBilling.getSkuDetails(apiVersion, packageName, type, skusBundle);
    }

    @Override
    public Bundle getBuyIntent(int apiVersion, String packageName, String sku, String type, String developerPayload,
        String oemid, String guestWalletId) throws RemoteException {
        Bundle bundle;
        bundle = WalletUtils.startServiceBind(appcoinsBilling, apiVersion, sku, type, developerPayload, oemid,
            guestWalletId);
        if (bundle == null) {
            bundle = new Bundle();
            bundle.putInt(RESPONSE_CODE, ResponseCode.SERVICE_UNAVAILABLE.getValue());
        }
        return WalletUtils.startWalletPayment(bundle, type);
    }

    @Override
    public Bundle getPurchases(int apiVersion, String packageName, String type, String continuationToken)
        throws RemoteException {
        Bundle bundle = appcoinsBilling.getPurchases(apiVersion, packageName, type, continuationToken);
        if (walletId != null) {
            ArrayList<String> idsList = bundle.getStringArrayList(INAPP_PURCHASE_ID_LIST);
            ArrayList<String> skuList = bundle.getStringArrayList(INAPP_PURCHASE_ITEM_LIST);
            ArrayList<String> dataList = bundle.getStringArrayList(INAPP_PURCHASE_DATA_LIST);
            ArrayList<String> signatureDataList = bundle.getStringArrayList(INAPP_DATA_SIGNATURE_LIST);
            PurchasesResponse purchasesResponse =
                ProductV2Manager.INSTANCE.getPurchasesSync(packageName, walletId, type);
            bundle =
                new PurchasesBundleMapper().mapGuestPurchases(bundle, purchasesResponse, idsList, skuList, dataList,
                    signatureDataList);
        }
        return bundle;
    }

    @Override
    public Bundle getVoidedPurchases(int apiVersion, String packageName, String startTime) throws RemoteException {
        Bundle bundle = appcoinsBilling.getVoidedPurchases(apiVersion, packageName, startTime);
        if (walletId != null) {
            ArrayList<String> dataList = new ArrayList<>();
            if (bundle != null && bundle.containsKey(INAPP_VOIDED_PURCHASE_DATA_LIST)) {
                dataList = bundle.getStringArrayList(INAPP_VOIDED_PURCHASE_DATA_LIST);
            }
            if (bundle == null) {
                bundle = new Bundle();
            }
            VoidedPurchasesResponse voidedPurchasesResponse =
                VoidedPurchasesManager.INSTANCE.getVoidedPurchasesSync(packageName, walletId, startTime);
            bundle =
                new VoidedPurchasesBundleMapper().mapGuestVoidedPurchases(bundle, voidedPurchasesResponse, dataList);
        }
        return bundle;
    }

    @Override
    public int consumePurchase(int apiVersion, String packageName, String purchaseToken) throws RemoteException {
        int responseCode = appcoinsBilling.consumePurchase(apiVersion, packageName, purchaseToken);
        int guestResponseCode = consumeGuestPurchase(walletId, apiVersion, packageName, purchaseToken);
        if (responseCode == ResponseCode.OK.getValue() || guestResponseCode == ResponseCode.OK.getValue()) {
            return ResponseCode.OK.getValue();
        } else {
            return ResponseCode.ERROR.getValue();
        }
    }

    @Override
    public IBinder asBinder() {
        return appcoinsBilling.asBinder();
    }

    private int consumeGuestPurchase(String walletId, int apiVersion, String packageName, String purchaseToken) {
        int responseCode = ResponseCode.ERROR.getValue();
        if (walletId != null && apiVersion == 3) {
            responseCode = ProductV2Manager.INSTANCE.consumePurchase(this.walletId, packageName, purchaseToken);
        }
        return responseCode;
    }

    private void pingConnection(final int apiVersion, final String packageName, final String type) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(() -> {
            try {
                appcoinsBilling.isBillingSupported(apiVersion, packageName, type);
            } catch (RemoteException e) {
                logWarning("Failed to ping Billing connection: " + e);
            }
        }, 0L, 60L, TimeUnit.SECONDS);
    }
}
