package com.appcoins.sdk.billing.oemid;

import java.io.IOException;
import java.io.RandomAccessFile;

public final class ZipExtensions {
    private static final int BUFFER_SIZE = 65047;
    public static final byte[] EOCD_SIG = new byte[]{80, 75, 5, 6};

    public ZipExtensions() {
    }

    public int getCommentSize(RandomAccessFile randomAccessFile) throws IOException {
        int bufferSize;
        if ((long) BUFFER_SIZE > randomAccessFile.length()) {
            bufferSize = (int) randomAccessFile.length();
        } else {
            bufferSize = BUFFER_SIZE;
        }

        long offset = randomAccessFile.length() - (long) bufferSize;
        randomAccessFile.seek(Math.max(offset, 0L));
        int seekAreaSize = bufferSize > 0 ? bufferSize : (int) randomAccessFile.length();
        byte[] buffer = new byte[seekAreaSize];
        randomAccessFile.readFully(buffer);

        for (int i = seekAreaSize - 4; i >= 0; --i) {
            if (buffer[i] == EOCD_SIG[0] && buffer[i + 1] == EOCD_SIG[1] && buffer[i + 2] == EOCD_SIG[2] && buffer[i + 3] == EOCD_SIG[3]) {
                return seekAreaSize - i - 22;
            }
        }

        return 0;
    }
}
