package jibril.utils;

import java.util.Base64;

public class Snow64 {
    public static long fromSnow64(String snow64) {
        return fromByteArray(
            Base64.getUrlDecoder().decode(
                snow64.replace('-', '_')
            )
        );
    }

    public static String toSnow64(long snowflake) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(toByteArray(snowflake))
            .replace('_', '-');
    }

    /*
    Copied and flattened from class com.google.common.primitives.Longs (Guava)
     */
    private static long fromByteArray(byte[] bytes) {
        if (bytes.length < Long.BYTES) throw new IllegalArgumentException(
            "array too small: " + bytes.length + " < " + Long.BYTES
        );

        return (bytes[0] & 0xFFL) << 56 | (bytes[1] & 0xFFL) << 48 | (bytes[2] & 0xFFL) << 40
            | (bytes[3] & 0xFFL) << 32 | (bytes[4] & 0xFFL) << 24 | (bytes[5] & 0xFFL) << 16
            | (bytes[6] & 0xFFL) << 8 | (bytes[7] & 0xFFL);
    }

    /*
    Copied and flattened from class com.google.common.primitives.Longs (Guava)
     */
    private static byte[] toByteArray(long value) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xffL);
            value >>= 8;
        }
        return result;
    }
}