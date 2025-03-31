package com.appcoins.sdk.billing.utils;

import com.appcoins.sdk.billing.ResponseCode;

public class ServiceUtils {

    public static boolean isSuccess(int code) {
        return code >= 200 && code < 300;
    }

    public static ResponseCode responseCodeFromNetworkResponseCode(int networkResponseCode) {
        ResponseCode responseCode = ResponseCode.ERROR;
        if (ServiceUtils.isSuccess(networkResponseCode)) {
            responseCode = ResponseCode.OK;
        }
        return responseCode;
    }
}
