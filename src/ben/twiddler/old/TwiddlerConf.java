package ben.twiddler.old;

import ben.twiddler.Chord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static ben.util.old.Bits.*;

/**
 * Created by benh on 5/2/15.
 */
public class TwiddlerConf {

    private byte[] data;

    public TwiddlerConf(){
        data = new byte[0];
    }

    public TwiddlerConf(final String filename) throws IOException {
        data = Files.readAllBytes(Paths.get(filename));
    }

    public enum HeaderField{
        CONFIG_FORMAT_VERSION(0, 1),
        CHORD_MAP_OFFSET(1, 2),
        MOUSE_CHORD_MAP_OFFSET(3, 2),
        STRING_TABLE_OFFSET(5, 2),
        MOUSE_MODE_TIME(7, 2),
        MOUSE_JUMP_TIME(9, 2),
        NORMAL_MOUSE_STARTING_SPEED(11, 1),
        MOUSE_JUMP_MODE_STARTING_SPEED(12, 1),
        MOUSE_ACCELERATION_FACTOR(13, 1),
        DELAY_ON_KEY_REPEAT(14, 1),
        OPTIONS(15, 1);
        final int offset;
        final int length;
        private HeaderField(final int offset, final int length){
            this.offset = offset;
            this.length = length;
        }
        public int parseValue(final byte[] data){
            return readUnsigned(data, offset, length);
        }
        public int parseValueLsbFirst(final byte[] data){
            return unsignedLsbFirst(data, offset, length);
        }
    }

    public int getConfigFormatVersion(){  return HeaderField.CONFIG_FORMAT_VERSION.parseValue(data);  }

    // dynamic lookups of other sections of file
    public int getChordMapOffset() {  return HeaderField.CHORD_MAP_OFFSET.parseValueLsbFirst(data);  }
    public int getMouseChordMapOffset() {  return HeaderField.MOUSE_CHORD_MAP_OFFSET.parseValueLsbFirst(data);  }
    public int getStringTableOffset() {  return HeaderField.STRING_TABLE_OFFSET.parseValueLsbFirst(data);  }

    public int getMouseModeTime() {  return HeaderField.MOUSE_MODE_TIME.parseValue(data);  }
    public int getMouseJumpTime() {  return HeaderField.MOUSE_JUMP_TIME.parseValue(data);  }
    public int getNormalMouseStartingSpeed() {  return HeaderField.NORMAL_MOUSE_STARTING_SPEED.parseValue(data);  }
    public int getMouseJumpModeStartingSpeed() {  return HeaderField.MOUSE_JUMP_MODE_STARTING_SPEED.parseValue(data);  }
    public int getMouseAccelerationFactor() {  return HeaderField.MOUSE_ACCELERATION_FACTOR.parseValue(data);  }
    public int getDelayOnKeyRepeat() {  return HeaderField.DELAY_ON_KEY_REPEAT.parseValue(data);  }
    public int getOptions() {  return HeaderField.OPTIONS.parseValue(data);  }


    public class ChordMapping {
        public final int offset;
        public ChordMapping(final int offset){  this.offset = offset;  }

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("config format version\t" + Integer.toHexString(getConfigFormatVersion()) + "\n");
        sb.append("chord-map offset from start of file, LSB first then MSB\t" + Integer.toHexString(getChordMapOffset()) + "\n");
        sb.append("mouse-chord-map offset, LSB first then MSB\t" + Integer.toHexString(getMouseChordMapOffset()) + "\n");
        sb.append("string table offset, LSB first then MSB\t" + Integer.toHexString(getStringTableOffset()) + "\n");
        sb.append("mouse mode time - timeout for staying in mouse mode\t" + Integer.toHexString(getMouseModeTime()) + "\n");
        sb.append("mouse jump time - allows for a quick double-tap in a\t" + Integer.toHexString(getMouseJumpTime()) + "\n");
        sb.append("normal mouse starting speed\t" + Integer.toHexString(getNormalMouseStartingSpeed()) + "\n");
        sb.append("mouse jump mode starting speed\t" + Integer.toHexString(getMouseJumpModeStartingSpeed()) + "\n");
        sb.append("mouse acceleration factor\t" + Integer.toHexString(getMouseAccelerationFactor()) + "\n");
        sb.append("delay on key repeat\t" + Integer.toHexString(getDelayOnKeyRepeat()) + "\n");
        sb.append("options byte\t" + Integer.toHexString(getOptions()) + "\n");

        // 4 bytes per mapping
        //   2 bytes for chord
        //   1 byte modifier
        //   1 byte key code
        // 00 00 00 00 ends table
        boolean tableEnded = false;
        int count = 0;
        for(int i = getChordMapOffset(); !tableEnded; i += 4){
            Chord chord = Chord.parseFrom(data, i);
            sb.append(chord.toString() + "\n");
            sb.append(toHexString(data[2]) + "\n");
            sb.append(toHexString(data[3]) + "\n");
            ++count;
            tableEnded = (data[i] == 0) && (data[i+1] == 0) && (data[i+2] == 0) && (data[i+3] == 0);
        }
        sb.append(count + " chords\n");
        sb.append("ended at " + Integer.toHexString(getChordMapOffset() + count*4) + "\n");



        return sb.toString();
    }




    public static void main(final String[] args) throws IOException {
        System.out.println("Hello World!");

        final TwiddlerConf tc = new TwiddlerConf("/Users/benh/Downloads/twiddler_default.cfg");
        System.out.println(tc.toString());

        System.out.println("Goodbye World!");
    }

}
