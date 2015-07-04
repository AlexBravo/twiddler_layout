package ben.twiddler;

import ben.twiddler.enums.ThumbKey;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static ben.util.Guards.require;

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

    private static class Config{
        public static class FileArg{
            public enum Format{ CFG, TSV }
            public final Format format;
            public final Path path;
            public FileArg(final Format format, final Path path){
                this.format = format;
                this.path = path;
            }
        }
        public List<FileArg> inFiles = new ArrayList<>();
        public FileArg outFile = null;
        public boolean translate = false;
        public boolean xray = false;
        public boolean swapHands = false;
        public Map<ThumbKey, KeyCodeModifier> thumbRemap = new HashMap<>(4);
        public boolean combine = false;
        public boolean fillImplied = false;
        public Config(final String[] args){
            for(int i = 0; i < args.length; ++i){
                if ("--cfg".equals(args[i])){
                    ++i;
                    final Path inPath = FileSystems.getDefault().getPath(args[i]);
                    require(Files.exists(inPath));
                    inFiles.add(new FileArg(FileArg.Format.CFG, inPath));
                } else if ("--tsv".equals(args[i])){
                    ++i;
                    final Path inPath = FileSystems.getDefault().getPath(args[i]);
                    require(Files.exists(inPath));
                    inFiles.add(new FileArg(FileArg.Format.TSV, inPath));
                } else if ("--outTsv".equals(args[i])){
                    ++i;
                    final Path outPath = FileSystems.getDefault().getPath(args[i]);
                    require(!Files.exists(outPath));
                    outFile = new FileArg(FileArg.Format.TSV, outPath);
                } else if ("--outCfg".equals(args[i])){
                    ++i;
                    final Path outPath = FileSystems.getDefault().getPath(args[i]);
                    require(!Files.exists(outPath));
                    outFile = new FileArg(FileArg.Format.CFG, outPath);
                } else if ("--thumbRemap".equals(args[i])){
                    ++i;
                    //;
                } else if ("--translate".equals(args[i])){
                    translate = true;
                } else if ("--combine".equals(args[i])){
                    combine = true;
                } else if ("--xray".equals(args[i])){
                    xray = true;
                } else if ("--swapHands".equals(args[i])){
                    swapHands = true;
                } else if ("--fillImplied".equals(args[i])){
                    fillImplied = true;
                }
            }
        }

        public boolean validate(){
            // must be doing exactly one of: combine, xray, swapHands, fillImplied
            int commands = 0;
            if (translate) ++commands;
            if (combine) ++commands;
            if (xray) ++commands;
            if (swapHands) ++commands;
            if (fillImplied) ++commands;
            if (!thumbRemap.isEmpty()) ++commands;
            if (commands != 1){
                System.out.println("["+commands+"] commands specified");
                printUsage();
                return false;
            }
            if ((translate || xray || swapHands || fillImplied || !thumbRemap.isEmpty()) && inFiles.size() > 1){
                System.out.println("cannot execute command with more than 1 file, ["+inFiles.size()+"] were specified");
                printUsage();
                return false;
            }
            return true;
        }

        public void printUsage(){
            System.out.println("usage:");
            System.out.println("TwiddlerConf");
            System.out.println("\t[--cfg <cfgFile>]... optional cfg input file(s)");
            System.out.println("\t[--tsv <tsvFile>]... optional tsv input file(s)");
            System.out.println("\t--outTsv|--outCfg <outFile> output file and format choice");
            System.out.println("\tchoice of one of four commands:");
            System.out.println("\t\t--translate");
            System.out.println("\t\t--combine");
            System.out.println("\t\t--xray");
            System.out.println("\t\t--swapHands");
            System.out.println("\t\t--fillImplied");
            System.out.println("\t\t--thumbRemap NUM=[LGUI],ALT=[LALT],CTRL=[LCTRL],SHFT=[LSHFT]");
        }
    }

    // other use cases:
    // mirror image (see-through vs facing)
    // mirror image (swap hands)
    // remap thumb keys (requires lots of additional chords)

    // overwrite one map with another smaller map (to "add" them together)
    // output cheat sheet based on a pattern like "O ..L."
    // read in cheat sheets, make conflicts obvious
    // support translation, read from whichever exists and write to whichever doesn't
    // --cfg <file>
    // --tsv <file>
    public static void main(final String[] args) throws IOException {
        final Config config = new Config(args);
        if (config.validate()){
            // convert list of inFiles to list of TwiddlerConfs
            final List<TwiddlerConf> tcs = new ArrayList<>();
            for(final Config.FileArg inFile: config.inFiles){
                if (inFile.format == Config.FileArg.Format.CFG){
                    tcs.add(TwiddlerConf.parseFromBinaryFile(inFile.path));
                } else {
                    tcs.add(TwiddlerConf.parseFromTextFile(inFile.path));
                }
            }
            if (config.translate){
                final TwiddlerConf tc = tcs.get(0);
                if (config.outFile.format == Config.FileArg.Format.CFG) {
                    tc.writeToBinaryFile(config.outFile.path);
                } else {
                    tc.writeToTextFile(config.outFile.path);
                }
            } else if (config.combine){
                System.out.println("combine not currently supported");
            } else if (config.xray){
                System.out.println("xray not currently supported");
            } else if (config.swapHands){
                System.out.println("swapHands not currently supported");
            } else if (config.fillImplied){
                System.out.println("fillImplied not currently supported");
            } else if (!config.thumbRemap.isEmpty()){
                System.out.println("thumbRemap not currently supported");
            }

        }
    }

}
