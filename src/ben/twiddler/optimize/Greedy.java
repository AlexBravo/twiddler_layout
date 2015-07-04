package ben.twiddler.optimize;

import ben.frequencies.Frequencies;
import ben.twiddler.Chord;
import ben.twiddler.enums.FingerKey;
import ben.twiddler.enums.ThumbKey;
import ben.util.Comparators;
import ben.util.Graph;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by benh on 6/6/15.
 */
public class Greedy {
    // primary objective is to capture highest probability of walking bigrams possible
    // secondary objective is to put high frequency characters in ergonomic positions

    // being able to define the graph separate from specific key assignments would be helpful
    // then we could be greedy twice

    // are there multiple non-isomorphic graphs with 27 nodes, where each node has 10 neighbors?
    // maybe not, but how do I build one?  build the adjacencies from the chords, then unlabel them

    // option one:
    // find the letter with the highest sum of 10 neighbors (each 3 finger 2 button chord has 10 neighbors)
    // place this chord and all 10 neighbors, move to the next highest chord (which may now only have 9 neighbors)

    // option two:
    // find the highest probability transition, place both ends of the transition as neighbors in the graph
    // go to the next highest transition that can still be placed.

    // option three:
    // remove the lowest probability transition, until some letter has only it's minimum number of neighbors left

    public static void main(final String[] args){
        System.out.println("Hello World!");

        optionOne();

        System.out.println("Goodbye World!");
    }

    public static Map<Chord, String> optionOne(){
        //  fill lowest cost chord
        //      find letter with highest sum of available neighbors
        //      fill neighboring chords, lowest cost to highest
        //      for each highest neighbor (10)
        //          find highest sum of available neighbors
        //          place highest neighbor in lowest cost neighboring chord


        final Map<Chord, String> result = new HashMap<>();

        List<Map.Entry<String, Double>> ntf = new ArrayList<>(Frequencies.neighborsToFrequency.entrySet());
        Collections.sort(ntf, new Frequencies.FrequencyComparator());

        Map<String, Map<String, Double>> top10s = new HashMap<>();
        Map<String, Double> scores = new HashMap<>();
        List<String> letters = new ArrayList<>();
        letters.add("_");
        for (char i = 'A'; i <= 'Z'; ++i) {  letters.add("" + i);  }
        for(String letter: letters){
            final Map<String, Double> ns = topNeighbors(letter, 10);
            top10s.put(letter, ns);
            for(final Map.Entry<String, Double> e: ns.entrySet()) {
                if (scores.get(letter) == null) {
                    scores.put(letter, 0.0);
                }
                scores.put(letter, scores.get(letter) + e.getValue());
            }
        }

        List<Map.Entry<String, Double>> scoresDesc = new ArrayList<>(scores.entrySet());
        Collections.sort(scoresDesc, new Frequencies.FrequencyComparator<String>());
        for(Map.Entry<String, Double> e: scoresDesc){
            List<Map.Entry<String, Double>> top10 = new ArrayList<>(top10s.get(e.getKey()).entrySet());
            Collections.sort(top10, new Frequencies.FrequencyComparator<String>());
            System.out.println(e.getKey() + "\t"+e.getValue()+"\t\t");
            for(Map.Entry<String, Double> i: top10){
                System.out.println("\t\t" + i.getKey() + "\t" + i.getValue());
            }
        }

        return result;
    }

    private static Map<String, Double> topNeighbors(final String letter, final int neighbors){
        PriorityQueue<Map.Entry<String, Double>> entries =
                new PriorityQueue<>(10, Comparators.reverse(new Frequencies.FrequencyComparator()));
        for(final Map.Entry<String, Double> e: Frequencies.neighborsToFrequency.entrySet()) {
            if (e.getKey().contains(letter)) {
                entries.add(e);
            }
            while(entries.size() > 10) entries.poll();
        }
        final Map<String, Double> result = new HashMap<>();
        for(final Map.Entry<String, Double> e: entries){
            result.put(e.getKey(), e.getValue());
        }
        return result;
    }

}
