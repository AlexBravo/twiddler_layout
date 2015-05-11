package ben.twiddler.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by benh on 5/11/15.
 */
public enum Modifier {
    NONE((byte) 0), SHIFT((byte) 20);
    public final byte serialized;

    private Modifier(final byte serialized) {
        this.serialized = serialized;
    }

    //TODO: byteToModifier should immutable
    public static Map<Byte, Modifier> byteToModifier;
    static {
        byteToModifier = new HashMap<>();
        for(Modifier m: Modifier.values()){
            byteToModifier.put(m.serialized, m);
        }
    }
}
