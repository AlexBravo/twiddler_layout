package ben.twiddler;

import java.util.*;

import static ben.util.Data.*;
import static ben.util.Guards.*;

/**
 * Created by benh on 5/17/15.
 */
public class StringTable {

    private final Map<Integer, KeyCodeSequence> indexToSequence;
    private final Map<KeyCodeSequence, Integer> sequenceToIndex;

    public StringTable(final Map<Integer, KeyCodeSequence> indexToSequence){
        this.indexToSequence = indexToSequence;
        this.sequenceToIndex = new HashMap<>();
        for(Map.Entry<Integer, KeyCodeSequence> e: indexToSequence.entrySet()){
            sequenceToIndex.put(e.getValue(), e.getKey());
        }
    }

    public StringTable(){
        this(new HashMap<Integer, KeyCodeSequence>());
    }

    public KeyCodeSequence get(final int index){
        require(indexToSequence.containsKey(index));
        return indexToSequence.get(index);
    }

    public Integer get(final KeyCodeSequence sequence){
        return sequenceToIndex.get(sequence);
    }

    public void add(final KeyCodeSequence sequence){
        require(sequence.sequence.size() > 1);
        require(!sequenceToIndex.containsKey(sequence));
        int maxIndex = 0;
        for(Integer i: indexToSequence.keySet()){
            maxIndex = Math.max(maxIndex, i);
        }
        final int newIndex = maxIndex + 1;
        indexToSequence.put(newIndex, sequence);
        sequenceToIndex.put(sequence, newIndex);
    }

    public static StringTable parseFrom(final byte[] data, final int offset){
        final Map<Integer, KeyCodeSequence> indexToSequence = new TreeMap<>();
        int offsetToLength = offset;
        int index = 0;
        // the serialization of the length uses 2 bytes
        // the value of length includes those two serialized bytes
        int length = readLSBFirstInt(data, offsetToLength, 2);
        assume(length % 2 == 0);
        while (length > 0){
            final KeyCodeSequence keyCodeSequence = KeyCodeSequence.parseFrom(data, offsetToLength + 2, length - 2);
            indexToSequence.put(index, keyCodeSequence);
            ++index;
            offsetToLength += length;
            length = readLSBFirstInt(data, offsetToLength, 2);
        }
        assume(length == 0);
        return new StringTable(indexToSequence);
    }

    public byte[] toBytes(){
        // size will be sum of:
        // 2 bytes * num entries for lengths
        // 2 bytes * sum of lengths for actual modifiers and key codes
        // 2 bytes for table terminator
        final List<Integer> indices = new ArrayList<>(indexToSequence.keySet());
        Collections.sort(indices);
        int totalCodes = 0;
        for(Integer index: indices){
            totalCodes += indexToSequence.get(index).sequence.size();
        }

        byte[] result = new byte[2 * totalCodes + 2 * indices.size() + 2];
        int offset = 0;
        for(Integer index: indices) {
            KeyCodeSequence sequence = indexToSequence.get(index);
            writeLSBFirstInt(sequence.sequence.size(), result, offset, 2);
            offset += 2;
            offset += sequence.writeTo(result, offset);
        }
        writeLSBFirstInt(0, result, offset, 2);
        offset += 2;
        assume(offset == result.length);
        return result;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(final Map.Entry<Integer, KeyCodeSequence> e: indexToSequence.entrySet()) {
            sb.append(e.getKey() + ": " + e.getValue()).append("\n");
        }
        return sb.toString();
    }

}
