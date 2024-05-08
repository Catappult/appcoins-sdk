package com.appcoins.sdk.billing.payasguest.oemid;

public class Constants {
    public static final byte[] SIGNING_BLOCK_MAGIC = new byte[]{65, 80, 75, 32, 83, 105, 103, 32, 66, 108, 111, 99, 107, 32, 52, 50};
    public static final byte[] PADDING_START = new byte[]{119, 101, 114, 66};
    public static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    public static final String OEMID_SEPARATOR = ",";

}
