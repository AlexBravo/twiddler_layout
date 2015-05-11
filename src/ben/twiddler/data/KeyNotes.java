package ben.twiddler.data;

import ben.util.TsvLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by benh on 5/3/15.
 */
public class KeyNotes {

    // TODO: idtoNote should be immutable...
    public static final Map<Integer, String> idToNote;
    static {
        try {
            idToNote = new HashMap<>();
            TsvLoader tl = new TsvLoader("src/resources/HidNotes.tsv", 2);
            for(int i = 0; i < tl.getNumRows(); ++i){
                List<String> row = tl.getRow(i);
                idToNote.put(Integer.parseInt(row.get(0)), row.get(1));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
