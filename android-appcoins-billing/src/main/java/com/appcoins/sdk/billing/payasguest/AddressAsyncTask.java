package com.appcoins.sdk.billing.payasguest;

import android.os.AsyncTask;
import com.appcoins.sdk.billing.listeners.AddressRetrievedListener;
import com.appcoins.sdk.billing.models.AddressModel;
import com.appcoins.sdk.billing.service.BdsService;
import com.appcoins.sdk.billing.service.address.AddressService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AddressAsyncTask extends AsyncTask<Object, Object, Object> {

  private String oemAddress;
  private String storeAddress;
  private String developerAddress;
  private AddressService addressService;
  private AddressRetrievedListener addressRetrievedListener;
  private String packageName;

  AddressAsyncTask(AddressService addressService, AddressRetrievedListener addressRetrievedListener,
      String packageName) {

    this.addressService = addressService;
    this.addressRetrievedListener = addressRetrievedListener;
    this.packageName = packageName;
  }

  @Override protected Object doInBackground(Object[] objects) {
    CountDownLatch countDownLatch = new CountDownLatch(3);
    getOemAddress(countDownLatch);
    getStoreAddress(countDownLatch);
    getDeveloperAddress(countDownLatch);
    try {
      countDownLatch.await(BdsService.TIME_OUT_IN_MILLIS, TimeUnit.MILLISECONDS);
      addressRetrievedListener.onAddressRetrieved(oemAddress, storeAddress, developerAddress);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return null;
  }

  private void getOemAddress(final CountDownLatch countDownLatch) {
    AdyenPaymentInteract.AddressListener addressListener =
        new AdyenPaymentInteract.AddressListener() {
          @Override public void onResponse(AddressModel addressModel) {
            oemAddress = addressModel.getAddress();
            countDownLatch.countDown();
          }
        };
    addressService.getOemAddressForPackage(packageName, addressListener);
  }

  private void getStoreAddress(final CountDownLatch countDownLatch) {
    AdyenPaymentInteract.AddressListener addressListener =
        new AdyenPaymentInteract.AddressListener() {
          @Override public void onResponse(AddressModel addressModel) {
            storeAddress = addressModel.getAddress();
            countDownLatch.countDown();
          }
        };
    addressService.getStoreAddressForPackage(packageName, addressListener);
  }

  private void getDeveloperAddress(final CountDownLatch countDownLatch) {
    AdyenPaymentInteract.AddressListener addressListener =
        new AdyenPaymentInteract.AddressListener() {
          @Override public void onResponse(AddressModel addressModel) {
            developerAddress = addressModel.getAddress();
            countDownLatch.countDown();
          }
        };
    addressService.getDeveloperAddress(packageName, addressListener);
  }
}
