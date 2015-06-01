package ben.twiddler;

import ben.util.TsvLoader;

import java.io.IOException;
import java.util.*;

import static ben.util.Data.readInt;
import static ben.util.Data.writeInt;
import static ben.util.Guards.assume;

/**
 * Created by benh on 5/17/15.
 */
public class ChordMap {

    private final Map<Chord, KeyCodeSequence> chordToSequence;

    public ChordMap(final Map<Chord, KeyCodeSequence> chordToSequence){
        this.chordToSequence = chordToSequence;
    }

    public static ChordMap parseFrom(final StringTable stringTable, final byte[] data, final int offset){
        final Map<Chord, KeyCodeSequence> chordToSequence = new HashMap<>();
        int entryOffset = offset;
        int terminationCheck = readInt(data, entryOffset, 4);
        while(terminationCheck != 0){
            Chord chord = Chord.parseFrom(data, entryOffset);

            KeyCodeSequence keyCodeSequence = null;
            final int stringBranch = readInt(data, entryOffset + 2, 1);
            if (stringBranch == 0xFF){
                final int index = readInt(data, entryOffset + 3, 1);
                keyCodeSequence = stringTable.get(index);
            } else {
                keyCodeSequence = KeyCodeSequence.parseFrom(data, entryOffset + 2, 2);
            }

            assume(!chordToSequence.containsKey(chord));

            chordToSequence.put(chord, keyCodeSequence);
            entryOffset += 4;
            terminationCheck = readInt(data, entryOffset, 4);
        }
        return new ChordMap(chordToSequence);
    }

    public static ChordMap parseFrom(final String text) throws IOException {
        final TsvLoader tsv = TsvLoader.loadFromText(text, 2);
        final Map<Chord, KeyCodeSequence> chordToSequence = new HashMap<>();
        for(int row = 0; row < tsv.getNumRows(); ++row){
            Chord chord = Chord.parseFrom(tsv.getCell(row, 0));
            KeyCodeSequence sequence = KeyCodeSequence.parseFrom(tsv.getCell(row, 1));
            chordToSequence.put(chord, sequence);
        }
        return new ChordMap(chordToSequence);
    }

    public void writeTo(final StringBuilder stringBuilder){
        final List<Chord> orderedChords = new ArrayList<>(chordToSequence.keySet());
        Collections.sort(orderedChords);

        for(final Chord chord: orderedChords){
            final KeyCodeSequence keyCodeSequence = chordToSequence.get(chord);
            chord.writeTo(stringBuilder);
            stringBuilder.append("\t");
            keyCodeSequence.writeTo(stringBuilder);
            stringBuilder.append("\n");
        }
    }

    public byte[] toBytes(final StringTable stringTable){
        final byte[] bytes = new byte[4 * chordToSequence.size()];
        int offset = 0;
        for(final Map.Entry<Chord, KeyCodeSequence> e: chordToSequence.entrySet()){
            final Chord chord = e.getKey();
            final KeyCodeSequence sequence = e.getValue();
            offset += chord.writeTo(bytes, offset);
            if (sequence.sequence.size() > 1){
                int index = stringTable.get(sequence);
                bytes[offset] = (byte) 0xFF;
                offset += 1;
                writeInt(index, bytes, offset, 1);
                offset += 1;
            } else {
                offset += sequence.writeTo(bytes, offset);
            }
        }

        return bytes;
    }

    public StringTable buildStringTable(){
        final StringTable stringTable = new StringTable();
        for(KeyCodeSequence sequence: chordToSequence.values()) {
            if (sequence.sequence.size() > 1){
                stringTable.add(sequence);
            }
        }
        return stringTable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChordMap chordMap = (ChordMap) o;

        return !(chordToSequence != null ? !chordToSequence.equals(chordMap.chordToSequence) : chordMap.chordToSequence != null);

    }

    @Override
    public int hashCode() {
        return chordToSequence != null ? chordToSequence.hashCode() : 0;
    }

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();

        final List<Chord> orderedChords = new ArrayList<>(chordToSequence.keySet());
        Collections.sort(orderedChords);

        for(Chord chord: orderedChords) {
            KeyCodeSequence keyCodeSequence = chordToSequence.get(chord);
            sb.append(chord.toString()).append("\t").append(keyCodeSequence.toString()).append("\n");
        }

        return sb.toString();
    }

    public String diff(final ChordMap that){
        final StringBuilder sb = new StringBuilder();
        // TODO: add symmetric difference to sb?
        for(final Map.Entry<Chord, KeyCodeSequence> e: chordToSequence.entrySet()){
            final Chord thisChord = e.getKey();
            if (that.chordToSequence.containsKey(thisChord)) {
                final KeyCodeSequence thisSequence = e.getValue();
                final KeyCodeSequence thatSequence = that.chordToSequence.get(thisChord);
                if (!Objects.equals(thisSequence, thatSequence)) {
                    sb.append("sequences not equal for [" + thisChord + "]\n");
                    sb.append("\t" + thisSequence + "\n");
                    sb.append("\t" + thatSequence + "\n");
                }
            }
        }
        return sb.toString();
    }

}
