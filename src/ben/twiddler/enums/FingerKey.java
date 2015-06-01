package ben.twiddler.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;

import static ben.util.Guards.assume;
import static ben.util.Guards.require;

/**
 * Created by benh on 5/3/15.
 */
public enum FingerKey {
    LEFT("L"), MIDDLE("M"), RIGHT("R");

    private final String string;

    private FingerKey(final String string) {
        this.string = string;
    }
    public static final String EMPTY_SET_STRING = "O";
    public static final Map<String, FingerKey> stringToFingerKey = new TreeMap<>();
    static {
        for(final FingerKey fk: FingerKey.values()){
            stringToFingerKey.put(fk.string, fk);
        }
    }

    public static FingerKey parseFrom(final String pattern){
        require(stringToFingerKey.containsKey(pattern));
        return stringToFingerKey.get(pattern);
    }

    public static EnumSet<FingerKey> parseSetFrom(final String fingerString){
        require(fingerString != null);
        require(fingerString.length() <= 3);
        EnumSet<FingerKey> result = EnumSet.noneOf(FingerKey.class);
        if (EMPTY_SET_STRING.equals(fingerString)){
            return result;
        } else {
            for(int i = 0; i < fingerString.length(); ++i){
                result.add(parseFrom(fingerString.substring(i, i+1)));
            }
        }
        return result;
    }

    public void writeTo(final StringBuilder stringBuilder){
        stringBuilder.append(string);
    }

    public static void writeSetTo(final EnumSet<FingerKey> finger, final StringBuilder stringBuilder){
        if (finger.isEmpty()){
            stringBuilder.append("O");
        } else {
            for (final FingerKey fk : finger) {
                fk.writeTo(stringBuilder);
            }
        }
    }

    @Override public String toString(){
        return string;
    }

}
