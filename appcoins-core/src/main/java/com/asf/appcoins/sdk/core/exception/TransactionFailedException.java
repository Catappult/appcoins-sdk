package com.asf.appcoins.sdk.core.exception;

public class TransactionFailedException extends Exception {

  public TransactionFailedException(String message) {
    super(message);
  }

  public TransactionFailedException(Throwable cause) {
    super(cause);
  }

  public TransactionFailedException(String message, Exception e) {
    super(message, e);
  }
}
