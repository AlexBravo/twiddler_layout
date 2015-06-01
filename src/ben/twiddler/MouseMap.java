package ben.twiddler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by benh on 5/28/15.
 */
public class MouseMap {

    private final Map<MouseChord, MouseAction> mouseChordMap;

    private MouseMap(final Map<MouseChord, MouseAction> mouseChordMap){
        this.mouseChordMap = mouseChordMap;
    }

    public static MouseMap parseFrom(final byte[] bytes, final int offset){
        final Map<MouseChord, MouseAction> mouseChordMap = new HashMap<>();
        int entryOffset = offset;
        MouseChord mc = MouseChord.parseFrom(bytes, entryOffset);
        MouseAction ma = MouseAction.parseFrom(bytes, entryOffset + 2);
        entryOffset += 3;
        while(!mc.zeroed() || !ma.zeroed()){
            mouseChordMap.put(mc, ma);
            entryOffset += 3;
            mc = MouseChord.parseFrom(bytes, entryOffset);
            ma = MouseAction.parseFrom(bytes, entryOffset + 2);
        }
        return new MouseMap(mouseChordMap);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(final Map.Entry<MouseChord, MouseAction> e: mouseChordMap.entrySet()){
            sb.append(e.getKey()).append('\t').append(e.getValue()).append('\n');
        }
        return sb.toString();
    }

    public static class MouseChord{

        public final byte[] chord = new byte[2];

        private MouseChord(){}

        public static MouseChord parseFrom(final byte[] bytes, final int offset) {
            final MouseChord mouseChord = new MouseChord();
            mouseChord.chord[0] = bytes[offset];
            mouseChord.chord[1] = bytes[offset + 1];
            return mouseChord;
        }

        public void writeTo(final byte[] bytes, final int offset){
            bytes[offset] = this.chord[0];
            bytes[offset + 1] = this.chord[1];
        }

        public boolean zeroed(){
            return ((chord[0] == 0) && (chord[1] == 0));
        }

        @Override
        public String toString(){
            return Integer.toHexString(chord[0]) + Integer.toHexString(chord[1]);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MouseChord that = (MouseChord) o;
            return Arrays.equals(chord, that.chord);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(chord);
        }

    }

    public static class MouseAction{

        public final byte action;

        private MouseAction(final byte action){
            this.action = action;
        }

        public static MouseAction parseFrom(final byte[] bytes, final int offset){
            final MouseAction result = new MouseAction(bytes[offset]);
            return result;
        }

        public void writeTo(final byte[] bytes, final int offset){
            bytes[offset] = this.action;
        }

        public boolean zeroed(){
            return action == 0;
        }

        @Override
        public String toString(){
            return Integer.toHexString(action);
        }

    }

}
