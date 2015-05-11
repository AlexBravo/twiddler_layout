package ben.twiddler;

import ben.twiddler.enums.FingerKey;
import ben.twiddler.enums.ThumbKey;

import java.util.Arrays;
import java.util.EnumSet;
import static ben.util.BitManip.*;

/**
 * Chord representds a chord.
 *
 * It's capable of representing any chord from a configuration file (even invalid ones).
 * It's capable of representing any real life set of keys pressed on the Twiddler (even invalid ones).
 *
 * It can read values in from the bytes of a file.
 * It can write values out in bytes for storage in a file.
 *
 * Created by benh on 5/2/15.
 */
public class Chord {

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

//    public Chord(){
//        this(
//                EnumSet.noneOf(ThumbKey.class),
//                EnumSet.noneOf(FingerKey.class),
//                EnumSet.noneOf(FingerKey.class),
//                EnumSet.noneOf(FingerKey.class),
//                EnumSet.noneOf(FingerKey.class));
//    }

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

    public byte[] toBytes(){
        final byte[] result = new byte[2];
        writeTo(result, 0);
        return result;
    }

    public void writeTo(final byte[] bytes, final int offset){
        if (bytes.length <= offset + 1) {
            throw new IllegalArgumentException("need two bytes to parse a chord");
        }
        setBit(bytes, offset, 0, thumb.contains(ThumbKey.NUM));
        setBit(bytes, offset, 4, thumb.contains(ThumbKey.ALT));
        setBit(bytes, offset, 8, thumb.contains(ThumbKey.CTRL));
        setBit(bytes, offset, 12, thumb.contains(ThumbKey.SHIFT));
        setBit(bytes, offset, 3, finger[POINTER].contains(FingerKey.LEFT));
        setBit(bytes, offset, 2, finger[POINTER].contains(FingerKey.MIDDLE));
        setBit(bytes, offset, 1, finger[POINTER].contains(FingerKey.RIGHT));
        setBit(bytes, offset, 7, finger[MIDDLE].contains(FingerKey.LEFT));
        setBit(bytes, offset, 6, finger[MIDDLE].contains(FingerKey.MIDDLE));
        setBit(bytes, offset, 5, finger[MIDDLE].contains(FingerKey.RIGHT));
        setBit(bytes, offset, 11, finger[RING].contains(FingerKey.LEFT));
        setBit(bytes, offset, 10, finger[RING].contains(FingerKey.MIDDLE));
        setBit(bytes, offset, 9, finger[RING].contains(FingerKey.RIGHT));
        setBit(bytes, offset, 15, finger[PINKEY].contains(FingerKey.LEFT));
        setBit(bytes, offset, 14, finger[PINKEY].contains(FingerKey.MIDDLE));
        setBit(bytes, offset, 13, finger[PINKEY].contains(FingerKey.RIGHT));
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

    public static Chord parseFrom(final byte[] bytes){
        return parseFrom(bytes, 0);
    }

    public static Chord parseFrom(final byte[] bytes, final int offset){
        if (bytes.length <= offset + 1) {
            throw new IllegalArgumentException("need two bytes to parse a chord");
        }
        EnumSet<ThumbKey> thumb = EnumSet.noneOf(ThumbKey.class);
        if (getBit(bytes, offset, 0)) thumb.add(ThumbKey.NUM);
        if (getBit(bytes, offset, 4)) thumb.add(ThumbKey.ALT);
        if (getBit(bytes, offset, 8)) thumb.add(ThumbKey.CTRL);
        if (getBit(bytes, offset, 12)) thumb.add(ThumbKey.SHIFT);
        EnumSet<FingerKey> pointer = EnumSet.noneOf(FingerKey.class);
        if (getBit(bytes, offset, 3)) pointer.add(FingerKey.LEFT);
        if (getBit(bytes, offset, 2)) pointer.add(FingerKey.MIDDLE);
        if (getBit(bytes, offset, 1)) pointer.add(FingerKey.RIGHT);
        EnumSet<FingerKey> middle = EnumSet.noneOf(FingerKey.class);
        if (getBit(bytes, offset, 7)) middle.add(FingerKey.LEFT);
        if (getBit(bytes, offset, 6)) middle.add(FingerKey.MIDDLE);
        if (getBit(bytes, offset, 5)) middle.add(FingerKey.RIGHT);
        EnumSet<FingerKey> ring = EnumSet.noneOf(FingerKey.class);
        if (getBit(bytes, offset, 11)) ring.add(FingerKey.LEFT);
        if (getBit(bytes, offset, 10)) ring.add(FingerKey.MIDDLE);
        if (getBit(bytes, offset, 9)) ring.add(FingerKey.RIGHT);
        EnumSet<FingerKey> pinkey = EnumSet.noneOf(FingerKey.class);
        if (getBit(bytes, offset, 15)) pinkey.add(FingerKey.LEFT);
        if (getBit(bytes, offset, 14)) pinkey.add(FingerKey.MIDDLE);
        if (getBit(bytes, offset, 13)) pinkey.add(FingerKey.RIGHT);
        return new Chord(thumb, pointer, middle, ring, pinkey);
    }

    public static Chord parseFrom(final String pattern){
        final String[] thumbAndFingers = pattern.split(" ");
        if (thumbAndFingers.length != 2){
            throw new IllegalArgumentException("not valid pattern for chord: ["+pattern+"]");
        }
        String thumbStr = thumbAndFingers[0];
        String fingerStr = thumbAndFingers[1];
        EnumSet<ThumbKey> thumb = EnumSet.noneOf(ThumbKey.class);
        if (thumbStr.contains("N")) thumb.add(ThumbKey.NUM);
        if (thumbStr.contains("A")) thumb.add(ThumbKey.ALT);
        if (thumbStr.contains("C")) thumb.add(ThumbKey.CTRL);
        if (thumbStr.contains("S")) thumb.add(ThumbKey.SHIFT);
        EnumSet<FingerKey> pointer = EnumSet.noneOf(FingerKey.class);
        if (fingerStr.charAt(POINTER) == 'L') pointer.add(FingerKey.LEFT);
        if (fingerStr.charAt(POINTER) == 'M') pointer.add(FingerKey.MIDDLE);
        if (fingerStr.charAt(POINTER) == 'R') pointer.add(FingerKey.RIGHT);
        EnumSet<FingerKey> middle = EnumSet.noneOf(FingerKey.class);
        if (fingerStr.charAt(MIDDLE) == 'L') pointer.add(FingerKey.LEFT);
        if (fingerStr.charAt(MIDDLE) == 'M') pointer.add(FingerKey.MIDDLE);
        if (fingerStr.charAt(MIDDLE) == 'R') pointer.add(FingerKey.RIGHT);
        EnumSet<FingerKey> ring = EnumSet.noneOf(FingerKey.class);
        if (fingerStr.charAt(RING) == 'L') pointer.add(FingerKey.LEFT);
        if (fingerStr.charAt(RING) == 'M') pointer.add(FingerKey.MIDDLE);
        if (fingerStr.charAt(RING) == 'R') pointer.add(FingerKey.RIGHT);
        EnumSet<FingerKey> pinkey = EnumSet.noneOf(FingerKey.class);
        if (fingerStr.charAt(PINKEY) == 'L') pointer.add(FingerKey.LEFT);
        if (fingerStr.charAt(PINKEY) == 'M') pointer.add(FingerKey.MIDDLE);
        if (fingerStr.charAt(PINKEY) == 'R') pointer.add(FingerKey.RIGHT);
        return new Chord(thumb, pointer, middle, ring, pinkey);
    }

}
