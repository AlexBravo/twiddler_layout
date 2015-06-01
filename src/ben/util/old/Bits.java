package ben.util.old;

import static ben.util.Guards.*;

/**
 * Created by benh on 5/2/15.
 */
public class Bits {

    //  treat byte as data
    //      sometimes like a bit set (mask stuff)
    //      sometimes like a number (readUnsigned, and endian issues)

    //  convert bytes (and arrays of bytes)
    //      to bit strings
    //      to hex strings
    //      to readUnsigned decimals

    //  query nth bit from a byte (or array of bytes)
    //  set nth bit (on/off) in a byte (or array of bytes)

    public static String padTo(final String str, final int len, final char pad){
        require(str != null);
        final StringBuilder sb = new StringBuilder();
        for(int i = 0; i < len - str.length(); ++i) {
            sb.append(pad);
        }
        sb.append(str);
        return sb.toString();
    }

    public static int readUnsigned(final byte b){
        return b & 0xff;
    }

    public static int readUnsigned(final byte[] bytes, final int offset, final int length){
        require(bytes != null);
        require((0 <= offset) && (offset + length < bytes.length));
        int result = 0;
        for(int i = offset; i < offset + length; ++i){
            result *= 256;
            result += readUnsigned(bytes[i]);
        }
        return result;
    }

//    public static int write(final byte[] bytes, final int offset, final int length, int data){
//        require(bytes != null);
//        require((0 <= offset) && (offset + length < bytes.length));
//
//    }

    public static int unsignedLsbFirst(final byte[] bytes, final int offset, final int length){
        require(bytes != null);
        require((0 <= offset) && (offset + length < bytes.length));
        int result = 0;
        for(int i = offset + length - 1; i >= offset; --i){
            result *= 256;
            result += readUnsigned(bytes[i]);
        }
        return result;
    }

    public static String toBitString(final byte b){
        return padTo(Integer.toBinaryString(readUnsigned(b)), 8, '0');
    }

    public static String toBitString(final byte[] bytes, final int offset, final int length){
        require(bytes != null);
        require((0 <= offset) && (offset + length < bytes.length));
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < offset + length; ++i){
            sb.append(toBitString(bytes[i]));
        }
        return sb.toString();
    }

    public static String toDecString(final byte b){
        return padTo(Integer.toString(readUnsigned(b)), 3, '0');
    }

    public static String toDecString(final byte[] bytes, final int offset, final int length){
        require(bytes != null);
        require((0 <= offset) && (offset + length < bytes.length));
        return Integer.toString(readUnsigned(bytes, offset, length));
    }

    public static String toHexString(final byte b){
        return padTo(Integer.toHexString(readUnsigned(b)), 2, '0');
    }

    public static String toHexString(final byte[] bytes, final int offset, final int length){
        require(bytes != null);
        require((0 <= offset) && (offset + length < bytes.length));
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < offset + length; ++i){
            sb.append(toHexString(bytes[i]));
        }
        return sb.toString();
    }

    public static byte writeBit(final byte b, final int bit, boolean value){
        require((0 <= bit) && (bit < 8), "byte only has bits 0-7, there is no [" + bit + "]");
        if (value) {
            return (byte) (b | (1 << bit));
        } else {
            return (byte) (b & ~(1 << bit));
        }
    }

    public static boolean readBit(final byte b, final int bit){
        require((0 <= bit) && (bit < 8), "byte only has bits 0-7, there is no [" + bit + "]");
        return (b & (1 << bit)) != 0;
    }

    public static boolean readBit(final byte[] bytes, final int offset, final int bit){
        require(bytes != null);
        require((0 <= offset) && (offset < bytes.length));
        return readBit(bytes[offset + bit / 8], bit % 8);
    }

    public static void writeBit(final byte[] bytes, final int offset, final int bit, final boolean value){
        require(bytes != null);
        require((0 <= offset) && (offset < bytes.length));
        bytes[offset + bit / 8] = writeBit(bytes[offset + bit / 8], bit % 8, value);
    }

}
