package ben.twiddler;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 *
 * Created by benh on 5/14/15.
 */
public class TwiddlerConf {

    private final ChordMap chordMap;

    private static final int MOUSE_CHORDS_SIZE = 3;

    private TwiddlerConf(final ChordMap chordMap){
        this.chordMap = chordMap;
    }

    public static TwiddlerConf parseFromBinaryFile(final Path path) throws IOException {
        return parseFrom(Files.readAllBytes(path));
    }

    public static TwiddlerConf parseFrom(final byte[] data){
        final Header header = Header.parseFrom(data, 0);
        final StringTable stringTable = StringTable.parseFrom(data, header.stringTableOffset);
        final ChordMap chordMap = ChordMap.parseFrom(stringTable, data, header.chordsOffset);
//        final MouseMap mouseMap = MouseMap.parseFrom(data, header.mouseChordsOffset);
        return new TwiddlerConf(chordMap);
    }

    public static TwiddlerConf parseFromTextFile(final Path path) throws IOException {
        final StringBuilder sb = new StringBuilder();
        for(final String line: Files.readAllLines(path, Charset.defaultCharset())){
            sb.append(line).append("\n");
        }
        return parseFrom(sb.toString());
    }

    public static TwiddlerConf parseFrom(final String text) throws IOException {
        return new TwiddlerConf(ChordMap.parseFrom(text));
    }

    public void writeToTextFile(final Path path) throws IOException {
        final BufferedWriter bw = Files.newBufferedWriter(path, Charset.defaultCharset());
        final StringBuilder sb = new StringBuilder();
        writeTo(sb);
        final String s = sb.toString();
        bw.write(s, 0, s.length());
        bw.close();
    }

    public void writeTo(final StringBuilder stringBuilder){
        chordMap.writeTo(stringBuilder);
    }

    public void writeToBinaryFile(final Path path) throws IOException {
        Files.write(path, toBytes());
    }

    public byte[] toBytes(){
        final StringTable stringTable = chordMap.buildStringTable();
        final byte[] stringTableBytes = stringTable.toBytes();
        final byte[] chordMapBytes = chordMap.toBytes(stringTable);

        final byte[] mouseMapBytes = new byte[MOUSE_CHORDS_SIZE];
        for(int i = 0; i < MOUSE_CHORDS_SIZE; ++i)
            mouseMapBytes[i] = 0;

        final Header header = new Header();
        header.chordsOffset = Header.SIZE;
        header.mouseChordsOffset = Header.SIZE + chordMapBytes.length;
        header.stringTableOffset = Header.SIZE + chordMapBytes.length + mouseMapBytes.length;
        final byte[] headerBytes = header.toBytes();

        final byte[] result = new byte[headerBytes.length + chordMapBytes.length + mouseMapBytes.length + stringTableBytes.length];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        System.arraycopy(chordMapBytes, 0, result, header.chordsOffset, chordMapBytes.length);
        System.arraycopy(mouseMapBytes, 0, result, header.mouseChordsOffset, mouseMapBytes.length);
        System.arraycopy(stringTableBytes, 0, result, header.stringTableOffset, stringTableBytes.length);

        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TwiddlerConf that = (TwiddlerConf) o;

        return chordMap.equals(that.chordMap);
    }

    @Override
    public int hashCode() {
        return chordMap.hashCode();
    }

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();
        sb.append(chordMap.toString());
        return sb.toString();
    }

    private String diff(final TwiddlerConf that){
        final StringBuilder sb = new StringBuilder();
        sb.append(this.chordMap.diff(that.chordMap));
        return sb.toString();
    }

    // support translation, read from whichever exists and write to whichever doesn't
    // --cfg <file>
    // --tsv <file>
    public static void main(final String[] args) throws IOException {
        if (args.length != 4){
            printUsage();
        } else {
            Path cfgPath = null;
            Path tsvPath = null;
            for (int i = 0; i < args.length; ++i) {
                if ("--cfg".equals(args[i])) {
                    ++i;
                    cfgPath = FileSystems.getDefault().getPath(args[i]);
                }
                if ("--tsv".equals(args[i])) {
                    ++i;
                    tsvPath = FileSystems.getDefault().getPath(args[i]);
                }
            }

            if (Files.exists(cfgPath) && Files.exists(tsvPath)) {
                System.out.println("both files exist already");
                printUsage();
            } else if (!Files.exists(cfgPath) && !Files.exists(tsvPath)) {
                System.out.println("neither file exists");
                printUsage();
            } else if (Files.exists(cfgPath) && !Files.exists(tsvPath)){
                final TwiddlerConf tc = TwiddlerConf.parseFromBinaryFile(cfgPath);
                System.out.println("loaded ["+ cfgPath.toString() +"]");
                System.out.println("writing ["+tsvPath.toString()+"]");
                tc.writeToTextFile(tsvPath);
            } else { // (!Files.exists(cfgPath) && Files.exists(tsvPath))
                final TwiddlerConf tc = TwiddlerConf.parseFromTextFile(tsvPath);
                System.out.println("loaded ["+ tsvPath.toString() +"]");
                System.out.println("writing ["+cfgPath.toString()+"]");
                tc.writeToBinaryFile(cfgPath);
            }
        }
        System.out.println("Goodbye World!");
    }

    private static void printUsage(){
        System.out.println("usage:");
        System.out.println("\tTwiddlerConf --cfg <cfg file> --tsv <tsv file>");
        System.out.println("one of the cfg and tsv should exist and one shouldn't, the one that exists will be translated and written to the one that doesn't");
        System.out.println("valid special symbols in tsv file: (see http://www.usb.org/developers/hidpage/Hut1_12v2.pdf)");
        for(final String symbol: Symbol.getAllSymbols()){
            if (symbol.length() > 1){
                System.out.println("\t[" + symbol + "]");
            }
        }
    }

}
