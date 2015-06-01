package ben.util;

import java.math.BigInteger;
import java.util.Arrays;

import static ben.util.Guards.require;
import static java.util.Arrays.copyOfRange;

/**
 * Created by benh on 5/17/15.
 */
public class Data {

    public static int readInt(final byte[] bytes, final int from, final int length){
        require(0 <= length);
        require(from + length <= bytes.length);
        return new BigInteger(1, copyOfRange(bytes, from, from + length)).intValue();
    }

    public static void writeInt(final int intToWrite, final byte[] bytes, final int from, final int length){
        require(0 <= length);
        byte[] bytesToWrite = toByteArray(intToWrite);
        require(bytesToWrite.length <= length);
        require(from + bytesToWrite.length <= bytes.length);
        Arrays.fill(bytes, from, from + length - bytesToWrite.length, (byte) 0);
        System.arraycopy(bytesToWrite, 0, bytes, from + length - bytesToWrite.length, bytesToWrite.length);
    }

    public static int readLSBFirstInt(final byte[] bytes, final int from, final int length){
        require(0 <= length);
        require(from + length <= bytes.length);
        return new BigInteger(1, inplaceReverse(copyOfRange(bytes, from, from + length))).intValue();
    }

    public static void writeLSBFirstInt(final int intToWrite, final byte[] bytes, final int from, final int length){
        require(0 <= length);
        byte[] bytesToWrite = toByteArray(intToWrite);
        require(from + bytesToWrite.length < bytes.length);
        inplaceReverse(bytesToWrite);
        System.arraycopy(bytesToWrite, 0, bytes, from, bytesToWrite.length);
        Arrays.fill(bytes, from + bytesToWrite.length, from + length, (byte) 0);
    }

    public static byte[] toByteArray(final int unsignedInt){
        require(unsignedInt >= 0);
        final byte[] result = BigInteger.valueOf(unsignedInt).toByteArray();
        if ((result.length > 1) && (result[0] == 0)) {
            return copyOfRange(result, 1, result.length);
        } else {
            return result;
        }
    }

    public static byte[] inplaceReverse(final byte[] bytes){
        for (int i = 0; i < bytes.length / 2; ++i) {
            byte tmp = bytes[i];
            bytes[i] = bytes[bytes.length - i - 1];
            bytes[bytes.length - i - 1] = tmp;
        }
        return bytes;
    }

    public static byte[] padTo(final byte[] bytes, final int length){
        require(0 <= length);
        if (bytes.length >= length) {
            return bytes;
        }
        final byte[] result = new byte[length];
        System.arraycopy(bytes, 0, result, length - bytes.length, bytes.length);
        return result;
    }

}
