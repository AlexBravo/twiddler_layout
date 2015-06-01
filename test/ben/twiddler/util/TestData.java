package ben.twiddler.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ben.util.Data.readInt;
import static ben.util.Data.toByteArray;
import static ben.util.Data.writeInt;
import static java.util.Arrays.copyOfRange;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by benh on 5/20/15.
 */
public class TestData {

    private static final Map<Integer, Integer> testConfig;
    static {
        // 85  = 1+4+16+64  = 01010101
        // 170 = 2+8+32+128 = 10101010
        // 0   1   2   3   4   5   6   7
        // 0-- 85- 0-- 170 0-- 2047--- 0--
        Map<Integer, Integer> tmp = new HashMap<>();
        for(int i = 0; i < 8; ++i){
            tmp.put(i, 0);
        }
        tmp.put(1, 85);
        tmp.put(3, 170);
        tmp.put(5, 7);
        tmp.put(6, 255);
        testConfig = tmp;
    }

    private static byte[] getTestBytes(){
        final byte[] bytes = new byte[testConfig.size()];
        for(final Map.Entry<Integer, Integer> e: testConfig.entrySet()){
            bytes[e.getKey()] = e.getValue().byteValue();
        }
        return bytes;
    }

    @Test
    public void testReadInt(){
        final byte[] bytes = getTestBytes();
        assertEquals(0, readInt(bytes, 0, 1));
        assertEquals(85, readInt(bytes, 1, 1));
        assertEquals(0, readInt(bytes, 2, 1));
        assertEquals(170, readInt(bytes, 3, 1));
        assertEquals(0, readInt(bytes, 4, 1));
        assertEquals(7, readInt(bytes, 5, 1));
        assertEquals(255, readInt(bytes, 6, 1));
        assertEquals(0, readInt(bytes, 7, 1));
        assertEquals(2047, readInt(bytes, 5, 2));
    }

    @Test
    public void testToByteArray(){
        final byte[] bytes = getTestBytes();
        for(Map.Entry<Integer, Integer> e: testConfig.entrySet()){
            final byte[] actualBytes = toByteArray(e.getValue());
            assertEquals(1, actualBytes.length);
            assertEquals(bytes[e.getKey()], actualBytes[0]);
        }
        final byte[] actualBytes = toByteArray(2047);
        assertTrue(Arrays.equals(copyOfRange(bytes, 5, 7), actualBytes));
    }

    @Test
    public void testWriteInt(){
        final byte[] actualBytes = new byte[testConfig.size()];
        for(final Map.Entry<Integer, Integer> e: testConfig.entrySet()){
            writeInt(e.getValue(), actualBytes, e.getKey(), 1);
        }
        final byte[] expectedBytes = getTestBytes();
        assertTrue(Arrays.equals(expectedBytes, actualBytes));
        writeInt(2047, actualBytes, 4, 3);
        assertTrue(Arrays.equals(expectedBytes, actualBytes));
    }

}
