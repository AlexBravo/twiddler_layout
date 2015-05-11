package ben.twiddler.data;

import ben.twiddler.enums.KeyboardSection;
import ben.util.TsvLoader;

import java.io.IOException;
import java.util.*;

/**
 * Created by benh on 5/3/15.
 */
public class KeyCodes {

    // TODO: idtoCodeInfo should be immutable...
    public static SortedMap<Integer, CodeInfo> idToCodeInfo;
    static {
        try {
            idToCodeInfo = new TreeMap<>();
            TsvLoader tl = new TsvLoader("src/resources/HidCodes.tsv", 4);
            for (int i = 0; i < tl.getNumRows(); ++i) {
                final List<String> row = tl.getRow(i);

                // id may be a range, SortedMap.tailMap().firstKey() gets us to the lowId we need
                final String lowId = row.get(0).split("-")[0];
                String parsing = row.get(2);
                boolean hasSection = false;
                KeyboardSection section = null;
                if (parsing.startsWith("Keyboard ")) {
                    hasSection = true;
                    section = KeyboardSection.KEYBOARD;
                    parsing = parsing.substring("Keyboard ".length());
                } else if (parsing.startsWith("Keypad ")) {
                    hasSection = true;
                    section = KeyboardSection.KEYPAD;
                    parsing = parsing.substring("Keypad ".length());
                }
                String symbol = parsing;
                boolean isModifiable = false;
                String modifiedSymbol = null;
                final String[] symbols = parsing.split(" and ");
                if (symbols.length > 1) {
                    symbol = symbols[0];
                    isModifiable = true;
                    modifiedSymbol = symbols[1];
                }

                List<String> notes = new ArrayList<>();
                String[] noteIdStrs = row.get(3).split("[^0-9]]");
                for (String noteIdStr : noteIdStrs) {
                    try {
                        int noteId = Integer.parseInt(noteIdStr);
                        String note = KeyNotes.idToNote.get(noteId);
                        if (note != null) {
                            notes.add(note);
                        }
                    } catch (NumberFormatException nfe) {
                        // silence...? noteId was ""
                    }
                }
                final CodeInfo codeInfo = new CodeInfo(
                        hasSection,
                        section,
                        symbol,
                        isModifiable,
                        modifiedSymbol,
                        notes);
                idToCodeInfo.put(Integer.parseInt(lowId), codeInfo);
            }
        } catch (IOException ioe){
            throw new RuntimeException(ioe);
        }
    }

    public static void main(String[] args){
        System.out.println("Hello World!");

        for(Map.Entry<Integer, CodeInfo> e: idToCodeInfo.entrySet()){
            Integer id = e.getKey();
            CodeInfo ci = e.getValue();
            System.out.println(id + ": " + ci + " -- " + ci.notes);
        }

        System.out.println("Goodbye World!");
    }

}
