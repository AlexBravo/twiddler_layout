package ben.twiddler.data;

import ben.twiddler.KeyCode;
import ben.twiddler.Symbol;
import ben.util.TsvLoader;

import java.io.IOException;
import java.util.*;

import static ben.util.Guards.assume;

/**
 * Created by benh on 5/24/15.
 */
public class DisambiguatedSymbolKeyCodes {
    private static final Map<String, Integer> symbolToCode;
    static {
        try {
            symbolToCode = new TreeMap<>();
            TsvLoader tl = TsvLoader.loadFrom("src/resources/DisambiguatedSymbols.tsv", 2);
            for (int i = 0; i < tl.getNumRows(); ++i) {
                final List<String> row = tl.getRow(i);
                assume(!symbolToCode.containsKey(row.get(0)), "duplicate key in symbol disambiguations ["+row.get(0)+"]");
                symbolToCode.put(row.get(0), Integer.parseInt(row.get(1)));
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static List<String> getAllSymbols(){
        return Symbol.getAllSymbols();
    }

    public static KeyCode getKeyCode(final String symbol){
        final Set<KeyCode> keyCodes = Symbol.getKeyCodes(symbol);
        assume(keyCodes.size() > 0);
        KeyCode keyCode = null;
        if (keyCodes.size() == 1){
            keyCode = keyCodes.iterator().next();
        } else {
            assume(symbolToCode.containsKey(symbol), "no disambiguation for ["+symbol+"]: " + keyCodes);
            keyCode = KeyCode.getKeyCode(symbolToCode.get(symbol));
        }
        assume(keyCode != null);
        return keyCode;
    }

    public static void main(final String[] args){
        System.out.println("Hello World!");

        for(final String symbol: Symbol.getAllSymbols()){
            if (!"Reserved".equals(symbol)) {
                final KeyCode keyCode = getKeyCode(symbol);
                System.out.println(symbol + "\t:\t" + keyCode);
            }
        }

        System.out.println("Goodbye World!");
    }
}
