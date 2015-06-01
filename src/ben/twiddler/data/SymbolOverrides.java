package ben.twiddler.data;

import ben.util.TsvLoader;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static ben.util.Guards.assume;

/**
 * Created by benh on 5/14/15.
 */
public class SymbolOverrides {
    private static final Map<String, String> symbolToOverride;
    static {
        try {
            symbolToOverride = new TreeMap<String, String>();
            TsvLoader tl = TsvLoader.loadFrom("src/resources/HidSymbolOverrides.tsv", 2);
            for(int i = 0; i < tl.getNumRows(); ++i){
                final List<String> row = tl.getRow(i);
                assume(!symbolToOverride.containsKey(row.get(0)), "duplicate key in symbol overrides ["+row.get(0)+"]");
                symbolToOverride.put(row.get(0), row.get(1));
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }

    }

    public static String getSymbol(final String symbol){
        String result = symbol;
        if (symbolToOverride.containsKey(symbol)){
            result = symbolToOverride.get(symbol);
        }
        return result;
    }
}
