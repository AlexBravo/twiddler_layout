package ben.twiddler.optimize;

import ben.frequencies.Frequencies;
import ben.twiddler.Chord;
import ben.twiddler.ChordMap;
import ben.twiddler.KeyCodeSequence;
import ben.twiddler.enums.FingerKey;
import ben.twiddler.enums.ThumbKey;
import ben.util.Graph;
import ben.util.LocalSearch;

import static ben.util.LocalSearch.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

import static ben.util.Guards.assume;

/**
 * Created by benh on 6/28/15.
 */
public class WalkingTransitions {

    public static void main(final String[] args) throws IOException {
        System.out.println("Hello World!");

//        final ChordMap initialMap = loadChordMap("/Users/benh/mine/twiddler_layout/my_layout.tsv"); // .6423
//        final ChordMap initialMap = loadChordMap("/Users/benh/mine/twiddler_layout/my_letters.tsv"); // [SPC], e, .6550 //***
//        final ChordMap initialMap = loadChordMap("/Users/benh/mine/twiddler_layout/tmp.tsv");
//        final PFM initialPfm = new PFM(initialMap);

        final Map<String, String> fixedChordToSymbol = new HashMap<>();
        fixedChordToSymbol.put("O MMOO", "[SPC]");
        fixedChordToSymbol.put("O MLOO", "e");
//        fixedChordToSymbol.put("O RMOO", "t");
//        fixedChordToSymbol.put("O LMOO", "a");
//        fixedChordToSymbol.put("O MROO", "o");
        final PFM initialPfm = randomChordMap(fixedChordToSymbol);

        LocalSearch<PFM> ls = new LocalSearch<>(maxWalkingTransitionSum, twoSwapNeighborhood);
        Solution<PFM> optima = ls.search(initialPfm);
        for(int i = 0; i < 1000; ++i) {
            System.out.println("testing random solution " + i);
            Solution<PFM> solution = ls.search(randomChordMap(fixedChordToSymbol));
            if (optima.objectiveValue < solution.objectiveValue){
                optima = solution;
            }
            System.out.println("Best so far (of random): " + optima.objectiveValue);
        }

        printSolution(optima);

        final StringBuilder sbOut = new StringBuilder();
        optima.solution.map.writeTo(sbOut);
        final Path path = FileSystems.getDefault().getPath("/Users/benh/mine/twiddler_layout/tmp.tsv");
        Files.write(path, Collections.singletonList(sbOut.toString()), Charset.defaultCharset());

        final StringBuilder sbIn = new StringBuilder();
        final List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
        for(final String line: lines){
            sbIn.append(line).append("\n");
        }
        final ChordMap reloaded = ChordMap.parseFrom(sbIn.toString());
        System.out.println("rehydrated coverage: " + maxWalkingTransitionSum.eval(new PFM(reloaded)));

        System.out.println("Goodbye World!");
    }

    public static ChordMap loadChordMap(final String filename) throws IOException {
        final FileSystem fs = FileSystems.getDefault();
        final Path p = fs.getPath(filename);
        final StringBuilder sb = new StringBuilder();
        for(final String l: Files.readAllLines(p, Charset.defaultCharset())){
            sb.append(l).append("\n");
        }
        return ChordMap.parseFrom(sb.toString());
    }

    public static final PFM randomChordMap(final Map<String, String> fixedChordToSymbol) {
        final Random random = new Random();

        final Map<Chord, KeyCodeSequence> fixedChordToSeq = new HashMap();
        final ChordMap cm = new ChordMap();
        for(final Map.Entry<String, String> e: fixedChordToSymbol.entrySet()){
            final Chord c = Chord.parseFrom(e.getKey());
            final KeyCodeSequence kcs = KeyCodeSequence.parseFrom(e.getValue());
            fixedChordToSeq.put(c, kcs);
            cm.put(c, kcs);
        }

        final List<Chord> assignableChords = new ArrayList<>(CHORD_GRAPH.getNodes());
        assignableChords.removeAll(fixedChordToSeq.keySet());

        final Set<KeyCodeSequence> assignableSequences = new HashSet<>();
        for (char i = 'a'; i <= 'z'; ++i) assignableSequences.add(KeyCodeSequence.parseFrom("" + i));
        assignableSequences.removeAll(fixedChordToSeq.values());

        for(final KeyCodeSequence s: assignableSequences){
            final int index = random.nextInt(assignableChords.size());
            final Chord chord = assignableChords.get(index);
            assignableChords.remove(index);
            cm.put(chord, s);
        }
        return new PFM(cm, fixedChordToSeq.keySet());
    }

    private static final List<String> CHORD_ORDER_ERGO = Arrays.asList(
            "O MMOO",
            "O RROO",
            "O LLOO",
            "O MLOO",
            "O RMOO",
            "O LMOO",
            "O MROO",
            "O OMMO",
            "O ORRO",
            "O OLLO",
            "O OLMO",
            "O OMRO",
            "O ORMO",
            "O OMLO",
            "O MOMO",
            "O LOLO",
            "O RORO",
            "O LOMO",
            "O MORO",
            "O MOLO",
            "O ROMO",
            "O ROLO",
            "O LORO",
            "O RLOO",
            "O LROO",
            "O OLRO",
            "O ORLO");

    private static final Map<String, Integer> LETTER_TO_RANK = new HashMap<>();
    static {
        List<Map.Entry<String, Double>> list = new ArrayList<>(Frequencies.unigramToFrequency.entrySet());
        Collections.sort(list, new Frequencies.FrequencyComparator<String>());
        for(int i = 0; i < list.size(); ++i){
            LETTER_TO_RANK.put(list.get(i).getKey(), i+1);
        }
    }

    public static void printSolution(final Solution<PFM> solution){
        for(final String chordStr: CHORD_ORDER_ERGO){
            System.out.print(chordStr + "\t");
            final Chord chord = Chord.parseFrom(chordStr);
            final KeyCodeSequence kcs = solution.solution.map.getSequence(chord);
            final StringBuilder sb = new StringBuilder();
            kcs.writeTo(sb);
            System.out.println(sb.toString() + "\t" + LETTER_TO_RANK.get(sb.toString().toUpperCase()));
        }
        System.out.println("coverage: " + solution.objectiveValue);
        printWalkingTransitions(solution.solution.map);
    }

    public static class PFM { // Partially Fixed Map
        public final ChordMap map;
        public final Set<Chord> fixed;
        public PFM(final ChordMap chordMap, final Set<Chord> fixed){
            this.map = chordMap;
            this.fixed = fixed;
        }
        public PFM(final ChordMap chordMap){
            this(chordMap, new HashSet<Chord>());
        }
        public PFM swap(final Chord c1, final Chord c2){
            final ChordMap result = new ChordMap(map);
            result.put(c1, map.getSequence(c2));
            result.put(c2, map.getSequence(c1));
            return new PFM(result, fixed);
        }
    }

    private static final Objective<PFM> maxWalkingTransitionSum = new Objective<PFM>() {
        @Override public double eval(PFM pfm) {
            double result = 0.00;
            for(Graph.Edge<Chord> e: CHORD_GRAPH.getEdges()){
                final Chord c1 = e.first();
                final Chord c2 = e.second();
                StringBuilder sb = new StringBuilder();
                pfm.map.getSequence(c1).writeTo(sb);
                String n1 = sb.toString().toUpperCase();
                if ("[SPC]".equals(n1)) n1 = "_";
                assume(n1.length() == 1);
                sb = new StringBuilder();
                pfm.map.getSequence(c2).writeTo(sb);
                String n2 = sb.toString().toUpperCase();
                if ("[SPC]".equals(n2)) n2 = "_";
                assume(n2.length() == 1);
                final char[] chars = {n1.charAt(0), n2.charAt(0)};
                Arrays.sort(chars);
                final String neighbors = new String(chars);
                if (Frequencies.neighborsToFrequency.containsKey(neighbors)) {
                    double freq = Frequencies.neighborsToFrequency.get(neighbors);
                    result += freq;
                }
            }
            return result;
        }
    };

    private static final LinearObjective<PFM> minWalkingTransitionSum = new LinearObjective<>();
    static {
        minWalkingTransitionSum.putObjective(maxWalkingTransitionSum, -1.0);
    }

    public static final NeighborhoodIterator<PFM> twoSwapNeighborhood = new NeighborhoodIterator<PFM>() {
        @Override public Iterator<PFM> iterator(final PFM pfm) {
            return new Iterator<PFM>() {
                private final Set<Chord> swappableChords = new HashSet<>(CHORD_GRAPH.getNodes());
                {  swappableChords.removeAll(pfm.fixed);  }
                private final Iterator<Chord> i1 = swappableChords.iterator();
                private Iterator<Chord> i2 = swappableChords.iterator();
                private Chord c1 = i1.next();
                private Chord c2 = i2.next();
                @Override public boolean hasNext() {
                    return ((c1 != null) && (c2 != null));
                }
                @Override public PFM next() {
                    PFM result = pfm.swap(c1, c2);
                    if (i2.hasNext()){
                        c2 = i2.next();
                    } else if (i1.hasNext()){
                        c1 = i1.next();
                        i2 = swappableChords.iterator();
                        c2 = i2.next();
                    } else {
                        c1 = null;
                        c2 = null;
                    }
                    return result;
                }
                @Override public void remove() {
                    throw new UnsupportedOperationException("remove unsupported");
                }
            };
        }
    };

//    public static ChordMap best3SwapNeighbor(final ChordMap initial){
//        ChordMap bestMap = initial;
//        double bestScore = maxWalkingTransitionSum.eval(bestMap);
//        List<Chord> chords = new ArrayList<>(CHORD_GRAPH.getNodes());
//        for(Chord c1: chords){
//            for(Chord c2: chords){
//                if (!c1.equals(c2)){
//                    {
//                        ChordMap swapped = swap(initial, c1, c2);
//                        double score = maxWalkingTransitionSum.eval(swapped);
//                        if (score > bestScore) {
////                        System.out.println("improved by ["+(score - bestScore)+"] by swapping ["+initial.getSequence(c1)+"] and ["+initial.getSequence(c2)+"]");
//                            bestMap = swapped;
//                            bestScore = score;
//                        }
//                    }
//                    for(Chord c3: chords){
//                        if (!c1.equals(c3) && !c2.equals(c3)){
//                            // only need the 4 in which all of c1, c2, c3 have swapped: bac, bca, cab, cba
//                            ChordMap s1 = swap(initial, c1, c2); // b a c
//                            ChordMap s2 = swap(initial, c1, c3); // c b a
//                            ChordMap s3 = swap(s1, c2, c3); // b c a
//                            ChordMap s4 = swap(s2, c2, c3); // c a b
//                            for(ChordMap swapped: Arrays.asList(s1, s2, s3, s4)){
//                                double score = maxWalkingTransitionSum.eval(swapped);
//                                if (score > bestScore) {
////                        System.out.println("improved by ["+(score - bestScore)+"] by swapping ["+initial.getSequence(c1)+"] and ["+initial.getSequence(c2)+"]");
//                                    bestMap = swapped;
//                                    bestScore = score;
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        return bestMap;
//    }

//    public static ChordMap swap(final ChordMap initial, final Chord c1, Chord c2){
//        final ChordMap swapped = new ChordMap(initial);
//        swapped.put(c1, initial.getSequence(c2));
//        swapped.put(c2, initial.getSequence(c1));
//        return swapped;
//    }

//    public static ChordMap slightestNeighbor(final ChordMap initial){
//        ChordMap slightestMap = initial;
//        double initialScore = maxWalkingTransitionSum.eval(initial);
//        double slightestScore = Double.MAX_VALUE;
//        List<Chord> chords = new ArrayList<>(CHORD_GRAPH.getNodes());
//        for(Chord c1: chords){
//            for(Chord c2: chords){
//                if (!c1.equals(c2)){
//                    ChordMap swapped = new ChordMap(initial);
//                    swapped.put(c1, initial.getSequence(c2));
//                    swapped.put(c2, initial.getSequence(c1));
//                    double score = maxWalkingTransitionSum.eval(swapped);
//                    if ((initialScore <  score) && (score < slightestScore)){
////                        System.out.println("improved by ["+(score - bestScore)+"] by swapping ["+initial.getSequence(c1)+"] and ["+initial.getSequence(c2)+"]");
//                        slightestMap = swapped;
//                        slightestScore = score;
//                    }
//                }
//            }
//        }
//        return slightestMap;
//    }

    final static List<Map.Entry<String, Double>> ntf = new ArrayList<>(Frequencies.neighborsToFrequency.entrySet());
    static {
        Collections.sort(ntf, new Frequencies.FrequencyComparator());
    }

    public static void printWalkingTransitions(final ChordMap chordMap){
        for(final Map.Entry<String, Double> e: ntf){
            double frequency = e.getValue();
            String neighbors = e.getKey().toLowerCase();
            String n1 = neighbors.substring(0,1);
            String n2 = neighbors.substring(1);
            if ("_".equals(n1)) n1 = "[SPC]";
            if ("_".equals(n2)) n2 = "[SPC]";
            KeyCodeSequence ks1 = KeyCodeSequence.parseFrom(n1);
            KeyCodeSequence ks2 = KeyCodeSequence.parseFrom(n2);
            Chord c1 = chordMap.getChord(ks1);
            Chord c2 = chordMap.getChord(ks2);
            System.out.print(neighbors + "["+frequency+"]");
            if (CHORD_GRAPH.containsEdge(c1, c2)){
                System.out.println("+++");
            } else {
                System.out.println("-");
            }
        }
    }

    public static final Graph<Chord> CHORD_GRAPH;
    static {
        CHORD_GRAPH = new Graph<>();
        final EnumSet<ThumbKey> noThumb = EnumSet.noneOf(ThumbKey.class);
        final Set<EnumSet<FingerKey>> fingerOptions = new HashSet<>();
        fingerOptions.add(EnumSet.noneOf(FingerKey.class));
        for(final FingerKey key: FingerKey.values()){
            fingerOptions.add(EnumSet.of(key));
        }
        for(final EnumSet<FingerKey> pointer: fingerOptions){
            for(final EnumSet<FingerKey> middle: fingerOptions){
                for(final EnumSet<FingerKey> ring: fingerOptions){
                    for(final EnumSet<FingerKey> pinky: fingerOptions){
                        final Chord chord = new Chord(noThumb, pointer, middle, ring, pinky);
                        if (accept(chord)){
                            CHORD_GRAPH.addNode(chord);
                            for(final Chord other: CHORD_GRAPH.getNodes()) {
                                if (accept(chord, other)) {
                                    CHORD_GRAPH.addEdge(chord, other);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean accept(final Chord chord){
        if (!chord.getPinky().isEmpty())
            return false; // no pinky
        int numFingers = 0;
        if (!chord.getPointer().isEmpty()) ++numFingers;
        if (!chord.getMiddle().isEmpty()) ++numFingers;
        if (!chord.getRing().isEmpty()) ++numFingers;
        if (numFingers != 2)
            return false;
        return true;
    }

    private static boolean accept(final Chord source, final Chord target){
        if (source.equals(target)){
            return true;
        }
        int presses = 0;
        int releases = 0;
        for(int finger = Chord.POINTER; finger <= Chord.PINKY; ++finger){
            if (!source.get(finger).equals(target.get(finger))){
                ++presses;
                ++releases;
                if (source.get(finger).isEmpty()) --releases;
                if (target.get(finger).isEmpty()) --presses;
            }
        }
        return (releases == 1) && (presses == 1);
    }

}
