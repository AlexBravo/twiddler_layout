package test;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by benh on 5/16/15.
 */
public class TestBigIntegerForBytes {

    public static void main(final String[] args) {
        System.out.println("Hello World!");

        final byte[] bytes = new byte[2];
        bytes[0] = (byte) 0x00;
        bytes[1] = (byte) 0x0A;
        BigInteger bi = new BigInteger(1, bytes);
        System.out.println(bi);
        System.out.println(Arrays.toString(bi.toByteArray()));

        System.out.println("Goodbye World!");
    }

}
