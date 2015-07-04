package ben.twiddler;

import java.util.EnumSet;

/**
 * Created by benh on 5/15/15.
 */
public enum KeyCodeModifier {
    LCTRL((byte) 0x01),
    LSHFT((byte) 0x02),
    LALT((byte) 0x04),
    LGUI((byte) 0x08),

    RCTRL((byte) 0x10),
    RSHFT((byte) 0x20),
    RALT((byte) 0x40),
    RGUI((byte) 0x80);

    public final byte mask;
    KeyCodeModifier(final byte mask) {
        this.mask = mask;
    }

    public static EnumSet<KeyCodeModifier> parseFromBinary(final byte[] data, final int offset){
        EnumSet<KeyCodeModifier> result = EnumSet.noneOf(KeyCodeModifier.class);
        for(KeyCodeModifier kcm: KeyCodeModifier.values()){
            if ((data[offset] & kcm.mask) == kcm.mask) result.add(kcm);
        }
        return result;
    }

    public static byte toByte(final EnumSet<KeyCodeModifier> modifiers){
        byte result = (byte) 0x00;
        for(final KeyCodeModifier kcm: modifiers){
            result |= kcm.mask;
        }
        return result;
    }

    public static void writeTo(final EnumSet<KeyCodeModifier> modifiers, final byte[] bytes){
        writeTo(modifiers, bytes, 0);
    }

    public static void writeTo(final EnumSet<KeyCodeModifier> modifiers, final byte[] bytes, final int offset){
        bytes[offset] = toByte(modifiers);
    }

}
