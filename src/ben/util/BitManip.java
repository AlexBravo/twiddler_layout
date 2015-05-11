package ben.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by benh on 5/2/15.
 */
public class BitManip {

    //  treat byte as data
    //      sometimes like a bit set (mask stuff)
    //      sometimes like a number (unsigned, and endian issues)

    //  convert bytes (and arrays of bytes)
    //      to bit strings
    //      to hex strings
    //      to unsigned decimals

    //  query nth bit from a byte (or array of bytes)
    //  set nth bit (on/off) in a byte (or array of bytes)

    public static String padTo(final String str, final int len, final char pad){
        final StringBuilder sb = new StringBuilder();
        for(int i = 0; i < len - str.length(); ++i) {
            sb.append(pad);
        }
        sb.append(str);
        return sb.toString();
    }

    public static int unsigned(final byte b){
        return b & 0xff;
    }

    public static int unsigned(final byte[] bytes, final int offset, final int length){
        int result = 0;
        for(int i = offset; i < offset + length; ++i){
            result *= 256;
            result += unsigned(bytes[i]);
        }
        return result;
    }

    public static int unsignedLsbFirst(final byte[] bytes, final int offset, final int length){
        int result = 0;
        for(int i = offset + length - 1; i >= offset; --i){
            result *= 256;
            result += unsigned(bytes[i]);
        }
        return result;
    }

    public static String bitString(final byte b){
        return padTo(Integer.toBinaryString(unsigned(b)), 8, '0');
    }

    public static String bitString(final byte[] bytes, final int offset, final int length){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < offset + length; ++i){
            sb.append(bitString(bytes[i]));
        }
        return sb.toString();
    }

    public static String decString(final byte b){
        return padTo(Integer.toString(unsigned(b)), 3, '0');
    }

    public static String decString(final byte[] bytes, final int offset, final int length){
        return Integer.toString(unsigned(bytes, offset, length));
    }

    public static String hexString(final byte b){
        return padTo(Integer.toHexString(unsigned(b)), 2, '0');
    }

    public static String hexString(final byte[] bytes, final int offset, final int length){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < offset + length; ++i){
            sb.append(hexString(bytes[i]));
        }
        return sb.toString();
    }

    public static byte setBit(final byte b, final int bit, boolean value){
        if (bit < 0 || 7 < bit) {
            throw new IllegalArgumentException("byte only has bits 0-7, there is no [" + bit + "]");
        }
        if (value) {
            return (byte) (b | (1 << bit));
        } else {
            return (byte) (b & ~(1 << bit));
        }
    }

    public static boolean getBit(final byte b, final int bit){
        if (bit < 0 || 7 < bit) {
            throw new IllegalArgumentException("byte only has bits 0-7, there is no [" + bit + "]");
        }
        return (b & (1 << bit)) != 0;
    }

    public static boolean getBit(final byte[] bytes, final int offset, final int bit){
        return getBit(bytes[offset + bit / 8], bit % 8);
    }

    public static void setBit(final byte[] bytes, final int offset, final int bit, final boolean value){
        bytes[offset + bit / 8] = setBit(bytes[offset + bit / 8], bit % 8, value);
    }

}
