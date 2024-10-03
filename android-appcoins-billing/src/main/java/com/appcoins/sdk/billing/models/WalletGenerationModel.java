package com.appcoins.sdk.billing.models;

public class WalletGenerationModel {

  private final String walletAddress;
  private final String signature;
  private final String ewt;
  private final boolean error;

  public WalletGenerationModel(String walletAddress, String signature, String ewt, boolean error) {
    this.walletAddress = walletAddress;
    this.signature = signature;
    this.ewt = ewt;
    this.error = error;
  }

  private WalletGenerationModel() {
    this.walletAddress = "";
    this.signature = "";
    this.ewt = "";
    this.error = true;
  }

  public static WalletGenerationModel createErrorWalletGenerationModel() {
    return new WalletGenerationModel();
  }

  public String getWalletAddress() {
    return walletAddress;
  }

  public String getSignature() {
    return signature;
  }

  public String getEwt() {
    return ewt;
  }

  public boolean hasError() {
    return error;
  }
}
