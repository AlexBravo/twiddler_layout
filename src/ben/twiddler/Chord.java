package ben.twiddler;

import ben.twiddler.enums.FingerKey;
import ben.twiddler.enums.ThumbKey;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.EnumSet;

import static ben.util.Guards.assume;
import static ben.util.Guards.require;
import static ben.util.old.Bits.*;
import static java.util.Arrays.copyOfRange;

/**
 * Chord represents a chord.
 *
 * It's capable of representing any chord from a configuration file (even invalid ones).
 * It's capable of representing any real life set of keys pressed on the Twiddler (even invalid ones).
 *
 * It can read values in from the bytes of a file.
 * It can write values out in bytes for storage in a file.
 *
 * Created by benh on 5/2/15.
 */
public class Chord implements Comparable<Chord> {

    private final EnumSet<ThumbKey> thumb;
    private final EnumSet<FingerKey>[] finger;

    public Chord(
            final EnumSet<ThumbKey> thumb,
            final EnumSet<FingerKey> pointer,
            final EnumSet<FingerKey> middle,
            final EnumSet<FingerKey> ring,
            final EnumSet<FingerKey> pinkey){
        this.thumb = thumb;
        this.finger = (EnumSet<FingerKey>[]) new EnumSet[4];
        this.finger[POINTER] = pointer;
        this.finger[MIDDLE] = middle;
        this.finger[RING] = ring;
        this.finger[PINKEY] = pinkey;
    }

    public static Chord parseFrom(final byte[] bytes, final int offset){
        if (bytes.length <= offset + 1) {
            throw new IllegalArgumentException("need two bytes to parse a chord");
        }
        EnumSet<ThumbKey> thumb = EnumSet.noneOf(ThumbKey.class);
        if (readBit(bytes, offset, 0)) thumb.add(ThumbKey.NUM);
        if (readBit(bytes, offset, 4)) thumb.add(ThumbKey.ALT);
        if (readBit(bytes, offset, 8)) thumb.add(ThumbKey.CTRL);
        if (readBit(bytes, offset, 12)) thumb.add(ThumbKey.SHFT);
        EnumSet<FingerKey> pointer = EnumSet.noneOf(FingerKey.class);
        if (readBit(bytes, offset, 3)) pointer.add(FingerKey.LEFT);
        if (readBit(bytes, offset, 2)) pointer.add(FingerKey.MIDDLE);
        if (readBit(bytes, offset, 1)) pointer.add(FingerKey.RIGHT);
        EnumSet<FingerKey> middle = EnumSet.noneOf(FingerKey.class);
        if (readBit(bytes, offset, 7)) middle.add(FingerKey.LEFT);
        if (readBit(bytes, offset, 6)) middle.add(FingerKey.MIDDLE);
        if (readBit(bytes, offset, 5)) middle.add(FingerKey.RIGHT);
        EnumSet<FingerKey> ring = EnumSet.noneOf(FingerKey.class);
        if (readBit(bytes, offset, 11)) ring.add(FingerKey.LEFT);
        if (readBit(bytes, offset, 10)) ring.add(FingerKey.MIDDLE);
        if (readBit(bytes, offset, 9)) ring.add(FingerKey.RIGHT);
        EnumSet<FingerKey> pinkey = EnumSet.noneOf(FingerKey.class);
        if (readBit(bytes, offset, 15)) pinkey.add(FingerKey.LEFT);
        if (readBit(bytes, offset, 14)) pinkey.add(FingerKey.MIDDLE);
        if (readBit(bytes, offset, 13)) pinkey.add(FingerKey.RIGHT);
        return new Chord(thumb, pointer, middle, ring, pinkey);
    }

    public static Chord parseFrom(final String pattern){
        final String[] thumbAndFingers = pattern.split(" ");
        require(thumbAndFingers.length == 2, "not valid pattern for chord: ["+pattern+"]");

        final String thumbStr = thumbAndFingers[0];
        final String fingerStr = thumbAndFingers[1];
        require(fingerStr.length() == 4, "not valid pattern for chord: [" + pattern + "]");

        final EnumSet<ThumbKey> thumb = ThumbKey.parseSetFrom(thumbStr);
        final EnumSet<FingerKey> pointer = FingerKey.parseSetFrom(fingerStr.substring(POINTER, POINTER+1));
        final EnumSet<FingerKey> middle = FingerKey.parseSetFrom(fingerStr.substring(MIDDLE, MIDDLE+1));
        final EnumSet<FingerKey> ring = FingerKey.parseSetFrom(fingerStr.substring(RING, RING+1));
        final EnumSet<FingerKey> pinkey = FingerKey.parseSetFrom(fingerStr.substring(PINKEY, PINKEY+1));
        return new Chord(thumb, pointer, middle, ring, pinkey);
    }

    public void writeTo(final StringBuilder stringBuilder){
        ThumbKey.writeSetTo(thumb, stringBuilder);
        stringBuilder.append(" ");
        for(int i = POINTER; i <= PINKEY; ++i){
            assume(finger[i].size() <= 1);
            FingerKey.writeSetTo(finger[i], stringBuilder);
        }
    }

    public int writeTo(final byte[] bytes, final int offset){
        if (bytes.length <= offset + 2) {
            throw new IllegalArgumentException("need two bytes to parse a chord");
        }
        writeBit(bytes, offset, 0, thumb.contains(ThumbKey.NUM));
        writeBit(bytes, offset, 4, thumb.contains(ThumbKey.ALT));
        writeBit(bytes, offset, 8, thumb.contains(ThumbKey.CTRL));
        writeBit(bytes, offset, 12, thumb.contains(ThumbKey.SHFT));
        writeBit(bytes, offset, 3, finger[POINTER].contains(FingerKey.LEFT));
        writeBit(bytes, offset, 2, finger[POINTER].contains(FingerKey.MIDDLE));
        writeBit(bytes, offset, 1, finger[POINTER].contains(FingerKey.RIGHT));
        writeBit(bytes, offset, 7, finger[MIDDLE].contains(FingerKey.LEFT));
        writeBit(bytes, offset, 6, finger[MIDDLE].contains(FingerKey.MIDDLE));
        writeBit(bytes, offset, 5, finger[MIDDLE].contains(FingerKey.RIGHT));
        writeBit(bytes, offset, 11, finger[RING].contains(FingerKey.LEFT));
        writeBit(bytes, offset, 10, finger[RING].contains(FingerKey.MIDDLE));
        writeBit(bytes, offset, 9, finger[RING].contains(FingerKey.RIGHT));
        writeBit(bytes, offset, 15, finger[PINKEY].contains(FingerKey.LEFT));
        writeBit(bytes, offset, 14, finger[PINKEY].contains(FingerKey.MIDDLE));
        writeBit(bytes, offset, 13, finger[PINKEY].contains(FingerKey.RIGHT));
        return 2;
    }

    // try for simplest first?
    @Override
    public int compareTo(Chord that) {
        int result = this.thumb.size() - that.thumb.size();
        if (result == 0){
            int thisSize = 0;
            int thatSize = 0;
            for(int i = POINTER; i <= PINKEY; ++i){
                thisSize += this.finger[i].size();
                thatSize += that.finger[i].size();
            }
            result = thisSize - thatSize;
        }
        for(int i = POINTER; i <= PINKEY && result == 0; ++i){
            result = this.finger[i].size() - that.finger[i].size();
        }
        for(int i = POINTER; i <= PINKEY && result == 0; ++i){
            if (!this.finger[i].isEmpty()){
                result = this.finger[i].iterator().next().compareTo(that.finger[i].iterator().next());
            }
        }
        return result;
    }

    public boolean isValid(){
        boolean result = true;
        result = result && Chord.thumbIsValid(thumb);
        int i = POINTER;
        while(result && i <= PINKEY){
            result = result && fingerIsValid(finger[i]);
            ++i;
        }
        return result;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append(thumbToString(thumb)).append(" ");
        for(int f = POINTER; f <= PINKEY; ++f){
            sb.append(fingerToString(finger[f]));
        }

        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chord chord = (Chord) o;

        if (thumb != null ? !thumb.equals(chord.thumb) : chord.thumb != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(finger, chord.finger);
    }

    @Override
    public int hashCode() {
        int result = thumb != null ? thumb.hashCode() : 0;
        result = 31 * result + (finger != null ? Arrays.hashCode(finger) : 0);
        return result;
    }

    public static final int POINTER = 0;
    public static final int MIDDLE = 1;
    public static final int RING = 2;
    public static final int PINKEY = 3;

    private static String thumbToString(final EnumSet<ThumbKey> thumb){
        if (thumb.isEmpty()) {
            return "O";
        }
        StringBuilder sb = new StringBuilder();
        for(ThumbKey thumbKey: ThumbKey.values()){
            if (thumb.contains(thumbKey)){
                sb.append(thumbKey.toString());
            }
        }
        return sb.toString();
    }

    private static String fingerToString(final EnumSet<FingerKey> finger){
        if (finger.isEmpty()) {
            return "O";
        }
        StringBuilder sb = new StringBuilder();
        for(FingerKey fingerKey: FingerKey.values()){
            if (finger.contains(fingerKey)){
                sb.append(fingerKey.toString());
            }
        }
        return sb.toString();
    }

    public static boolean fingerIsValid(final EnumSet<FingerKey> finger){
        return (finger.size() <= 1);
    }

    public static boolean thumbIsValid(final EnumSet<ThumbKey> thumb){
        return true;
    }

}
