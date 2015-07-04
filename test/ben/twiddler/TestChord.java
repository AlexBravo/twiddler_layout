package ben.twiddler;

import ben.twiddler.enums.FingerKey;
import ben.twiddler.enums.ThumbKey;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by benh on 6/5/15.
 */
public class TestChord {

    public static List<Chord> getTestChords(){
        List<Chord> result = new ArrayList<>();

        result.add(new Chord(
                EnumSet.of(ThumbKey.NUM),
                EnumSet.of(FingerKey.MIDDLE),
                EnumSet.noneOf(FingerKey.class),
                EnumSet.noneOf(FingerKey.class),
                EnumSet.noneOf(FingerKey.class)));

        result.add(new Chord(
                EnumSet.of(ThumbKey.SHFT),
                EnumSet.of(FingerKey.LEFT),
                EnumSet.of(FingerKey.MIDDLE),
                EnumSet.of(FingerKey.RIGHT),
                EnumSet.of(FingerKey.LEFT)));

        return result;
    }

    @Test
    public void testToBinaryAndBack(){
        for(final Chord c1: getTestChords()){
            System.out.println("serializing: ["+c1+"]");
            final byte[] bytes = new byte[2];
            c1.writeTo(bytes, 0);
            final Chord c2 = Chord.parseFrom(bytes, 0);
            System.out.println("deserialized: ["+c2+"]");
            assertEquals(c1, c2);
        }
    }

    @Test
    public void testToStringAndBack(){
        for(final Chord c1: getTestChords()){
            System.out.println("serializing: ["+c1+"]");
            final StringBuilder sb = new StringBuilder();
            c1.writeTo(sb);
            final Chord c2 = Chord.parseFrom(sb.toString());
            System.out.println("deserialized: ["+c2+"]");
            assertEquals(c1, c2);
        }
    }

}
