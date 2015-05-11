package ben.twiddler;

import ben.twiddler.data.CodeInfo;
import ben.twiddler.data.KeyCodes;
import ben.twiddler.enums.Modifier;
import ben.util.BitManip;

import java.util.*;

/**
 * Created by benh on 5/10/15.
 */
public class KeyCode {

    private static Map<String, Set<CodeInfo>> symbolToCodeInfos;
    static {
        symbolToCodeInfos = new HashMap<>();
        for(CodeInfo ci: KeyCodes.idToCodeInfo.values()){
            {
                Set<CodeInfo> codes = symbolToCodeInfos.get(ci.symbol);
                if (codes == null) {
                    codes = new HashSet<>();
                    symbolToCodeInfos.put(ci.symbol, codes);
                }
                codes.add(ci);
            }
            if (ci.isModifiable){
                Set<CodeInfo> codes = symbolToCodeInfos.get(ci.modifiedSymbol);
                if (codes == null) {
                    codes = new HashSet<>();
                    symbolToCodeInfos.put(ci.modifiedSymbol, codes);
                }
                codes.add(ci);
            }
        }
        for(Map.Entry<String, Set<CodeInfo>> e: symbolToCodeInfos.entrySet()){
            if (e.getValue().size() > 1){
                List<String> symbols = new ArrayList<>();
                for(CodeInfo ci: e.getValue()){
                    symbols.add(ci.toString());
                }
                System.out.println("ambiguous symbols: " + symbols);
            }
        }
    }

    private final Modifier modifier;
    private final int code;

    public KeyCode(final Modifier modifier, final int code){
        this.modifier = modifier;
        this.code = code;
    }

    public KeyCode(final int code){
        this(Modifier.NONE, code);
    }

    public boolean isValid(){
        if (!KeyCodes.idToCodeInfo.containsKey(code)){
            return false;
        }
        CodeInfo ci = KeyCodes.idToCodeInfo.get(code);
        return ((modifier == Modifier.NONE) || ci.isModifiable);
    }

    @Override
    public String toString(){
        if (modifier == Modifier.NONE) {
            return KeyCodes.idToCodeInfo.get(code).symbol;
        } else {
            return KeyCodes.idToCodeInfo.get(code).modifiedSymbol;
        }
    }

    public byte[] toBytes(){
        final byte[] result = new byte[2];
        writeTo(result, 0);
        return result;
    }

    public void writeTo(final byte[] bytes, final int offset){
        bytes[offset] = modifier.serialized;
        bytes[offset+1] = (byte) code;
    }

    public static KeyCode parseFrom(final byte[] bytes){
        return parseFrom(bytes, 0);
    }

    public static KeyCode parseFrom(final byte[] bytes, final int offset){
        Modifier modifier = Modifier.byteToModifier.get(bytes[offset]);
        int code = BitManip.unsigned(bytes[offset+1]);
        return new KeyCode(modifier, code);
    }

    public static KeyCode parseFrom(final String pattern){
        // not all KeyCodes have chars associated with them
        // some chars have multiple KeyCodes associated with them

    }

    public static void main(String[] args){
        System.out.println("Hello World!");



        System.out.println("Goodbye World!");
    }

}
