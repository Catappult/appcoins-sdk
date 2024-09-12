package com.appcoins.sdk.billing.oemid;

import static com.appcoins.sdk.billing.oemid.Constants.HEX_ARRAY;
import static com.appcoins.sdk.billing.oemid.Constants.OEMID_SEPARATOR;
import static com.appcoins.sdk.billing.oemid.Constants.PADDING_START;
import static com.appcoins.sdk.billing.oemid.Constants.SIGNING_BLOCK_MAGIC;
import static com.appcoins.sdk.core.logger.Logger.logDebug;
import static com.appcoins.sdk.core.logger.Logger.logWarning;

import android.content.Context;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OemIdExtractorV2 implements OemIdExtractor {

    private final Context context;
    public ZipExtensions zipExtensions = new ZipExtensions();

    public OemIdExtractorV2(Context context) {
        this.context = context;
    }

    @Override
    public String extract(String packageName) {
        String oemId = null;
        try {
            String sourceDir = getPackageName(context, packageName);
            File file = new File(sourceDir);
            oemId = readValueFromFile(file);
        } catch (Exception e) {
            logWarning("Failed to obtain OEMID from Extractor V2: " + e);
        }
        if (oemId != null) {
            return oemId.split(OEMID_SEPARATOR)[0];
        }
        return null;
    }

    private String getPackageName(Context context, String packageName)
            throws PackageManager.NameNotFoundException {
        return context.getPackageManager()
                .getPackageInfo(packageName, 0).applicationInfo.sourceDir;
    }

    private String readValueFromFile(File file) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        int cdOffset = this.getCdOffset(file.length(), randomAccessFile);
        byte[] buffer = new byte[24];
        long pointer = (long) cdOffset - 16L - 8L;
        randomAccessFile.seek(pointer);
        randomAccessFile.readFully(buffer);
        byte[] apkSigningBlocks = Arrays.copyOfRange(buffer, 8, buffer.length);
        this.assertSigningMagic(apkSigningBlocks);
        int fileSize = (int) randomAccessFile.length();
        byte[] paddingBuffer = new byte[Math.min(65536, fileSize)];
        randomAccessFile.seek(pointer - (long) paddingBuffer.length);
        randomAccessFile.readFully(paddingBuffer);
        this.assertPadding(paddingBuffer);
        return this.readValue(cdOffset, buffer, randomAccessFile);
    }

    private void assertPadding(byte[] paddingBuffer) {
        int firstZero = -1;

        int i;
        for (i = paddingBuffer.length - 1; i >= 0; --i) {
            if (paddingBuffer[i] == 0) {
                firstZero = i;
                break;
            }
        }

        if (firstZero != -1) {
            for (i = firstZero - 4; i >= 0; --i) {
                if (paddingBuffer[i] == 119 && paddingBuffer[i + 1] == 101 && paddingBuffer[i + 2] == 114 && paddingBuffer[i + 3] == 66) {
                    return;
                }
            }
        }

        throw new IllegalStateException("Failed to validate padding!");
    }

    private void assertSigningMagic(byte[] apkSigningBlocks) {
        if (!Arrays.equals(apkSigningBlocks, SIGNING_BLOCK_MAGIC)) {
            throw new IllegalArgumentException("Can't find Apk Signing Block Magic!");
        }
    }

    private String readValue(int cdOffset, byte[] buffer, RandomAccessFile randomAccessFile) throws IOException {
        ByteBuffer wrap = ByteBuffer.wrap(buffer);
        wrap.order(ByteOrder.LITTLE_ENDIAN);
        int blockSize = wrap.getInt();
        int injected = Math.min(blockSize, 1024);
        randomAccessFile.seek((long) cdOffset - 16L - 8L - (long) injected);
        byte[] byteArray = new byte[injected];
        randomAccessFile.readFully(byteArray);
        byteArray = Arrays.copyOfRange(byteArray, getPaddingStart(byteArray), byteArray.length);
        int headerIndex = getHeaderIndex(byteArray);
        return headerIndex == -1 ? this.getOldOemidFormat(byteArray) : this.getOemid(byteArray, headerIndex);
    }

    private String getOldOemidFormat(byte[] bytes) {
        List<Byte> byteList = new ArrayList<>();

        for (int i = 0; i != bytes.length; ++i) {
            if (bytes[i] != 0) {
                byteList.add(bytes[i]);
            }
        }

        byte[] trimmed = toPrimitive(byteList.toArray(new Byte[0]));
        if (trimmed.length == 0) {
            throw new IllegalStateException("Could not extract oemid");
        } else {
            logDebug("Extractor - Old oemid format trimmed: " + Arrays.toString(trimmed));
            return new String(trimmed);
        }
    }

    private String getOemid(byte[] bytes, int header) {
        int oemidSize = bytes.length - header - 1;
        logDebug("Extractor - Getting oemid of size " + oemidSize);
        if (oemidSize > 16) {
            throw new IllegalStateException("Could not extract oemid");
        } else if (oemidSize == 16) {
            return hexToString(Arrays.copyOfRange(bytes, header + 1, bytes.length));
        } else {
            throw new IllegalStateException("Could not extract oemid");
        }
    }

    public static byte[] toPrimitive(final Byte[] array) {
        if (array == null) {
            return null;
        } else if (array.length == 0) {
            return new byte[0];
        }
        final byte[] result = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    private static int getHeaderIndex(byte[] bytes) {
        for (int i = 0; i != bytes.length; ++i) {
            byte b = bytes[i];
            if (b == -5) {
                return i;
            }

            if (b != 0) {
                return -1;
            }
        }

        return -1;
    }

    private int getPaddingStart(byte[] bytes) {
        for (int i = bytes.length - 4; i >= 0; --i) {
            if (bytes[i] == PADDING_START[0] && bytes[i + 1] == PADDING_START[1] && bytes[i + 2] == PADDING_START[2] && bytes[i + 3] == PADDING_START[3]) {
                return i + 4;
            }
        }

        return 0;
    }

    private String hexToString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; ++j) {
            int v = bytes[j] & 255;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 15];
        }

        return new String(hexChars);
    }

    private int getCdOffset(long fileSize, RandomAccessFile randomAccessFile) throws IOException {
        int commentSize = this.zipExtensions.getCommentSize(randomAccessFile);
        int offset = 6 + commentSize;
        randomAccessFile.seek(fileSize - (long) offset);
        byte[] byteArray = new byte[4];
        randomAccessFile.readFully(byteArray);
        ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer.getInt();
    }
}
