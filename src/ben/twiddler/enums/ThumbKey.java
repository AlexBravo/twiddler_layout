package ben.twiddler.enums;

import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;

import static ben.util.Guards.assume;
import static ben.util.Guards.require;

/**
 * Created by benh on 5/3/15.
 */
public enum ThumbKey {
    NUM("N"), ALT("A"), CTRL("C"), SHFT("S");

    private final String string;

    private ThumbKey(final String string) {
        this.string = string;
    }

    public static final String EMPTY_SET_STRING = "O";
    public static final Map<String, ThumbKey> stringToThumbKey = new TreeMap<>();
    static {
        for(final ThumbKey tk: ThumbKey.values()){
            stringToThumbKey.put(tk.string, tk);
        }
    }

    public static ThumbKey parseFrom(final String pattern){
        require(stringToThumbKey.containsKey(pattern));
        return stringToThumbKey.get(pattern);
    }

    public static EnumSet<ThumbKey> parseSetFrom(final String thumbString){
        require(thumbString != null);
        require(thumbString.length() <= 4);
        EnumSet<ThumbKey> result = EnumSet.noneOf(ThumbKey.class);
        if (EMPTY_SET_STRING.equals(thumbString)){
            return result;
        } else {
            for(int i = 0; i < thumbString.length(); ++i){
                result.add(parseFrom(thumbString.substring(i, i+1)));
            }
        }
        return result;
    }

    public void writeTo(final StringBuilder stringBuilder){
        stringBuilder.append(string);
    }

    public static void writeSetTo(final EnumSet<ThumbKey> thumb, final StringBuilder stringBuilder){
        if (thumb.isEmpty()){
            stringBuilder.append("O");
        } else {
            for (final ThumbKey tk : thumb) {
                tk.writeTo(stringBuilder);
            }
        }
    }

    @Override public String toString(){
        return string;
    }

}
