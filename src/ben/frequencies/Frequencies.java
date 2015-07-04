package ben.frequencies;

import ben.util.TsvLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static ben.util.Guards.assume;

/**
 * Created by benh on 6/6/15.
 */
public class Frequencies {

    public static final Map<String, BigInteger> unigramToCount = new HashMap<>();
    public static final Map<String, Double> unigramToFrequency = new HashMap<>();

    public static final Map<String, BigInteger> bigramToCountNoBounds = new HashMap<>();
    public static final Map<String, Double> bigramToFrequencyNoBounds = new HashMap<>();

    // with bounds
    public static final String WORD_BOUND = "_";
    public static final Map<String, BigInteger> bigramToCount = new HashMap<>();
    public static final Map<String, Double> bigramToFrequency = new HashMap<>();

    public static final Map<String, BigInteger> neighborsToCount = new HashMap<>();
    public static final Map<String, Double> neighborsToFrequency = new HashMap<>();

    static {
        TsvLoader wordCounts = null;
        try {
            wordCounts = TsvLoader.loadFrom("src/resources/google-books-common-words.txt", 2);
            BigInteger unigramCount = BigInteger.ZERO;
            BigInteger bigramCountNoBounds = BigInteger.ZERO;
            BigInteger bigramCount = BigInteger.ZERO;
            for (int row = 0; row < wordCounts.getNumRows(); ++row) {
                final String word = wordCounts.getCell(row, 0);
                final String boundedWord = WORD_BOUND + word + WORD_BOUND;
                BigInteger count = new BigInteger(wordCounts.getCell(row, 1));

                for(int i = 0; i < word.length(); ++i) {
                    final String unigram = word.substring(i, i + 1);
                    BigInteger current = unigramToCount.get(unigram);
                    if (current == null) current = BigInteger.ZERO;
                    unigramToCount.put(unigram, current.add(count));
                    unigramCount = unigramCount.add(count);
                }

                for(int i = 0; i < word.length()-1; ++i){
                    final String bigram = word.substring(i, i + 2);
                    BigInteger current = bigramToCountNoBounds.get(bigram);
                    if (current == null) current = BigInteger.ZERO;
                    bigramToCountNoBounds.put(bigram, current.add(count));
                    bigramCountNoBounds = bigramCountNoBounds.add(count);
                }

                for(int i = 0; i < boundedWord.length()-1; ++i){
                    final String bigram = boundedWord.substring(i, i + 2);
                    BigInteger current = bigramToCount.get(bigram);
                    if (current == null) current = BigInteger.ZERO;
                    bigramToCount.put(bigram, current.add(count));
                    bigramCount = bigramCount.add(count);
                }
            }

            for(final Map.Entry<String, BigInteger> e: bigramToCount.entrySet()) {
                final char[] chars = e.getKey().toCharArray();
                assume(chars.length == 2);
                Arrays.sort(chars);
                final String neighbors = new String(chars);
                BigInteger current = neighborsToCount.get(neighbors);
                if (current == null) current = BigInteger.ZERO;
                neighborsToCount.put(neighbors, current.add(e.getValue()));
            }

            final BigDecimal unigramTotal = new BigDecimal(unigramCount);
            final BigDecimal bigramTotalNoBounds = new BigDecimal(bigramCountNoBounds);
            final BigDecimal bigramTotal = new BigDecimal(bigramCount);
            final BigDecimal neighborsTotal = bigramTotal;
            for(final Map.Entry<String, BigInteger> e: unigramToCount.entrySet()){
                unigramToFrequency.put(e.getKey(), new BigDecimal(e.getValue()).
                        divide(unigramTotal, 10, BigDecimal.ROUND_DOWN).doubleValue());
            }
            for(final Map.Entry<String, BigInteger> e: bigramToCountNoBounds.entrySet()){
                bigramToFrequencyNoBounds.put(e.getKey(), new BigDecimal(e.getValue()).
                        divide(bigramTotalNoBounds, 10, BigDecimal.ROUND_DOWN).doubleValue());
            }
            for(final Map.Entry<String, BigInteger> e: bigramToCount.entrySet()){
                bigramToFrequency.put(e.getKey(), new BigDecimal(e.getValue()).
                        divide(bigramTotal, 10, BigDecimal.ROUND_DOWN).doubleValue());
            }
            for(final Map.Entry<String, BigInteger> e: neighborsToCount.entrySet()){
                neighborsToFrequency.put(e.getKey(), new BigDecimal(e.getValue()).
                        divide(bigramTotal, 10, BigDecimal.ROUND_DOWN).doubleValue());
            }
        } catch (IOException ioe) {
            throw new RuntimeException("exception caught in static initialization", ioe);
        }
    }

    public static void main(final String[] args) {
        System.out.println("Hello World!");
        System.out.println("--------------------");
        System.out.println("unigram frequencies");
        System.out.println("--------------------");
        final List<Map.Entry<String, Double>> unigramFrequencies = new ArrayList<>(unigramToFrequency.entrySet());
        Collections.sort(unigramFrequencies, new FrequencyComparator<String>());
        double unigramSum = 0;
        for(final Map.Entry<String, Double> e: unigramFrequencies){
            unigramSum += e.getValue();
            System.out.println(e.getKey() + "\t" + e.getValue());
        }
        assume(unigramSum < 1.0 && 1.0 < unigramSum + 0.001);
        System.out.println("--------------------");
        System.out.println("bigram frequencies (no word bounds '_')");
        System.out.println("--------------------");
        double bigramSumNoBounds = 0;
        final List<Map.Entry<String, Double>> bigramFrequenciesNoBounds = new ArrayList<>(bigramToFrequencyNoBounds.entrySet());
        Collections.sort(bigramFrequenciesNoBounds, new FrequencyComparator<String>());
        for(final Map.Entry<String, Double> e: bigramFrequenciesNoBounds){
            bigramSumNoBounds += e.getValue();
            System.out.println(e.getKey() + "\t" + e.getValue());
        }
        assume(bigramSumNoBounds < 1.0 && 1.0 < bigramSumNoBounds + 0.001);
        System.out.println("--------------------");
        System.out.println("bigram frequencies (with word bounds '_')");
        System.out.println("--------------------");
        double bigramSum = 0;
        final List<Map.Entry<String, Double>> bigramFrequencies = new ArrayList<>(bigramToFrequency.entrySet());
        Collections.sort(bigramFrequencies, new FrequencyComparator<String>());
        for(final Map.Entry<String, Double> e: bigramFrequencies){
            bigramSum += e.getValue();
            System.out.println(e.getKey() + "\t" + e.getValue());
        }
        assume(bigramSum < 1.0 && 1.0 < bigramSum + 0.001);
        System.out.println("--------------------");
        System.out.println("neighbor frequencies (with word bounds '_')");
        System.out.println("--------------------");
        double neighborsSum = 0;
        final List<Map.Entry<String, Double>> neighborsFrequencies = new ArrayList<>(neighborsToFrequency.entrySet());
        Collections.sort(neighborsFrequencies, new FrequencyComparator<String>());
        for(final Map.Entry<String, Double> e: neighborsFrequencies){
            neighborsSum += e.getValue();
            System.out.println(e.getKey() + "\t" + e.getValue());
        }
        assume(neighborsSum < 1.0 && 1.0 < neighborsSum + 0.001);
        System.out.println("----------");
        System.out.println("Goodbye World!");
    }

    public static class CountComparator<T> implements Comparator<Map.Entry<T, BigInteger>> {
        @Override
        public int compare(Map.Entry<T, BigInteger> e1, Map.Entry<T, BigInteger> e2) {
            return -e1.getValue().subtract(e2.getValue()).signum();
        }
    }

    public static class FrequencyComparator<T> implements Comparator<Map.Entry<T, Double>> {
        @Override
        public int compare(Map.Entry<T, Double> d1, Map.Entry<T, Double> d2) {
            return (int) Math.signum(-(d1.getValue() - d2.getValue()));
        }
    }

}
