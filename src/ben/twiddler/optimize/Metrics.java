package ben.twiddler.optimize;

import ben.frequencies.Frequencies;
import ben.twiddler.*;
import ben.twiddler.data.DisambiguatedSymbolKeyCodes;
import ben.twiddler.enums.FingerKey;
import ben.twiddler.enums.ThumbKey;

import java.util.*;

import static ben.util.Guards.assume;
import static ben.util.Guards.require;

/**
 * Created by benh on 6/7/15.
 */
public class Metrics {

    // doing nothing, no movements should be a cost of 0.0
    // the most difficult chord imaginable should be a cost of 1.0 (perhaps RLRL or LRLR)
    private static final double[] COST_PRESS = {0.05, 0.025, 0.3, 0.65};

    public static double pressCost(final Chord chord){
        double cost = 0.0;
        // a cost related to using a finger
        for(int finger = Chord.POINTER; finger <= Chord.PINKY; ++finger){
            require(chord.get(finger).size() <= 1);
            if (!chord.get(finger).isEmpty())
                cost += COST_PRESS[finger];
        }
        // a cost related to a combination of fingers (gaps)
        if ((!chord.get(Chord.POINTER).isEmpty()) &&
                (chord.get(Chord.MIDDLE).isEmpty()) &&
                (!chord.get(Chord.RING).isEmpty())){
            cost += 0.1;
        }
        if ((!chord.get(Chord.POINTER).isEmpty()) &&
                (chord.get(Chord.MIDDLE).isEmpty()) &&
                (chord.get(Chord.RING).isEmpty()) &&
                (!chord.get(Chord.PINKY).isEmpty())){
            cost += 0.05;
        }
        if ((!chord.get(Chord.MIDDLE).isEmpty()) &&
                (chord.get(Chord.RING).isEmpty()) &&
                (!chord.get(Chord.PINKY).isEmpty())){
            cost += 0.1;
        }
        int lastFinger = -1;
        FingerKey lastKey = null;
        for(int finger = Chord.POINTER; finger <= Chord.PINKY; ++finger) {
            if (!chord.get(finger).isEmpty()){
                FingerKey key = chord.get(finger).iterator().next();
                if (lastFinger >= Chord.POINTER){
                    final double fingerDist = finger - lastFinger;
                    final double keyDist = lastKey.ordinal() - key.ordinal();
                    // a cost related to extremeness of steps (RROO is easier than RMOO is easier than RLOO)
                    cost += (Math.abs(keyDist) / fingerDist) / 20.0;
                    // a cost related to alignment with finger lengths (RLOO is easier than LROO)
                    if (misaligned(lastFinger, finger, keyDist))
                        cost += (Math.abs(keyDist) / fingerDist) / 20.0;
                }
                lastFinger = finger;
                lastKey = key;
            }
        }

        return cost;
    }

    private static boolean misaligned(int lastFinger, int finger, double keyDist){
        if ((lastFinger == Chord.POINTER) && (finger == Chord.MIDDLE) && (keyDist < 0)) return true;
        if ((lastFinger == Chord.POINTER) && (finger == Chord.RING) && (keyDist < 0)) return true;
        if ((lastFinger == Chord.POINTER) && (finger == Chord.PINKY) && (keyDist > 0)) return true;
        if ((lastFinger == Chord.MIDDLE) && (finger == Chord.RING) && (keyDist > 0)) return true;
        if ((lastFinger == Chord.MIDDLE) && (finger == Chord.PINKY) && (keyDist > 0)) return true;
        if ((lastFinger == Chord.RING) && (finger == Chord.PINKY) && (keyDist > 0)) return true;
        return false;
    }

    // the most difficult chord imaginable should be a cost of 1.0 (perhaps between RLRL and LRLR)
    public static double transitionCost(final Chord c1, final Chord c2){
        // cost related to a particular finger pressing (held fingers don't have to press)
        // cost related to a particular finger switching (both releasing and pressing)
        //    how far
        // if all fingers move, slightly more than  press cost of c2


        return 0.0;
    }

    private static final double PRESS_WEIGHT = 0.2;
    private static final double TRANSITION_WEIGHT = 0.8;
    public static double objective(final ChordMap chordMap){
        return PRESS_WEIGHT * pressObjective(chordMap) +
                TRANSITION_WEIGHT * transitionObjective(chordMap);
    }

    public static double pressObjective(final ChordMap chordMap){
        double sum = 0.0;
        Map<String, Chord> charToChord = charToChord(chordMap);
        for(char i = 'a'; i <= 'z'; ++i){
            final String unigram = "" + i;
            final double frequency = Frequencies.unigramToFrequency.get(unigram);
            final double cost = pressCost(charToChord.get(i));
            sum += cost * frequency;
        }
        return sum;
    }

    public static double transitionObjective(final ChordMap chordMap){
        double sum = 0.0;
        Map<String, Chord> charToChord = charToChord(chordMap);
        for(char i = 'a'; i <= 'z'; ++i){
            for(char j= 'a'; j <= 'z'; ++j) {
                final String bigram = "" + i + j;
                final double frequency = Frequencies.bigramToFrequency.get(bigram);
                final double cost = transitionCost(charToChord.get(i), charToChord.get(j));
                sum += cost * frequency;
            }
        }
        return sum;
    }

    private static Map<String, Chord> charToChord(final ChordMap chordMap){
        final Map<String, Chord> result = new HashMap<>();
        for(char i = 'a'; i <= 'z'; ++i){
            String character = "" + i;
            final KeyCode keyCode = DisambiguatedSymbolKeyCodes.getKeyCode(character);
            final ModifiedKeyCode modifiedKeyCode = new ModifiedKeyCode(keyCode);
            final KeyCodeSequence keyCodeSequence = new KeyCodeSequence(Arrays.asList(modifiedKeyCode));
            final Chord chord = chordMap.getChord(keyCodeSequence);
            result.put(character, chord);
        }
        String space = "SPC";
        final KeyCode keyCode = DisambiguatedSymbolKeyCodes.getKeyCode(space);
        final ModifiedKeyCode modifiedKeyCode = new ModifiedKeyCode(keyCode);
        final KeyCodeSequence keyCodeSequence = new KeyCodeSequence(Arrays.asList(modifiedKeyCode));
        final Chord chord = chordMap.getChord(keyCodeSequence);
        result.put("_", chord);
        return result;
    }

    public static void main(final String[] args){
        System.out.println("Hello World!");

        final EnumSet<ThumbKey> thumbKeys = EnumSet.noneOf(ThumbKey.class);
        final List<EnumSet<FingerKey>> fingerKeys = new ArrayList<>();
        fingerKeys.add(EnumSet.noneOf(FingerKey.class));
        fingerKeys.add(EnumSet.of(FingerKey.LEFT));
        fingerKeys.add(EnumSet.of(FingerKey.MIDDLE));
        fingerKeys.add(EnumSet.of(FingerKey.RIGHT));

        final List<Chord> chords = new ArrayList<>();
        for(final EnumSet<FingerKey> pointer: fingerKeys) {
            for(final EnumSet<FingerKey> middle: fingerKeys) {
                for(final EnumSet<FingerKey> ring: fingerKeys) {
                    for(final EnumSet<FingerKey> pinky: fingerKeys) {
                        final Chord chord = new Chord(thumbKeys, pointer, middle, ring, pinky);
                        if ((chord.numButtons() == 2) && (pinky.isEmpty()))
                            chords.add(chord);
                    }
                }
            }
        }
        Collections.sort(chords, PRESS_COST_ORDER);

        for(final Chord chord: chords){
            System.out.println(chord + " -> " + pressCost(chord));
        }

        System.out.println("Goodbye World!");
    }

    public static Comparator<Chord> PRESS_COST_ORDER = new Comparator<Chord>() {
        @Override public int compare(Chord c1, Chord c2) {
            return Double.compare(pressCost(c1), pressCost(c2));
        }
    };

}
