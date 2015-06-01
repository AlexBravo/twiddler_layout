package ben.twiddler;

import ben.twiddler.data.KeyNotes;
import ben.twiddler.data.SymbolOverrides;
import ben.twiddler.enums.KeyboardSection;
import ben.util.TsvLoader;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by benh on 5/15/15.
 */
public class KeyCode {
    public final int usageId;
    public final KeyboardSection section;
    public final String symbol;
    public final String modifiedSymbol;
    public final List<String> notes;

    // TODO: usageIdToKeyCode should be immutable
    public static final SortedMap<Integer, KeyCode> usageIdToKeyCode = new TreeMap<>();

    public static KeyCode getKeyCode(final int usageId){
        return usageIdToKeyCode.get(usageId);
    }

    static {
        try {
            TsvLoader tl = TsvLoader.loadFrom("src/resources/HidCodes.tsv", 4);
            for (int i = 0; i < tl.getNumRows(); ++i) {
                final List<String> row = tl.getRow(i);

                // id may be a range, SortedMap.tailMap().firstKey() gets us to the lowId we need
                final String lowId = row.get(0).split("-")[0];
                String parsing = row.get(2);
                KeyboardSection section = null;
                if (parsing.startsWith("Keyboard ")) {
                    section = KeyboardSection.KEYBOARD;
                    parsing = parsing.substring("Keyboard ".length());
                } else if (parsing.startsWith("Keypad ")) {
                    section = KeyboardSection.KEYPAD;
                    parsing = parsing.substring("Keypad ".length());
                }
                final String[] symbols = parsing.split(" and ");
                final String symbol = SymbolOverrides.getSymbol(symbols[0]);
                String modifiedSymbol = null;
                if (symbols.length > 1) {
                    modifiedSymbol = SymbolOverrides.getSymbol(symbols[1]);
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

                final KeyCode keyCode = new KeyCode(
                        Integer.parseInt(lowId),
                        section,
                        symbol,
                        modifiedSymbol,
                        notes);
                usageIdToKeyCode.put(Integer.parseInt(lowId), keyCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private KeyCode(final int usageId,
                    final KeyboardSection section,
                    final String symbol,
                    final String modifiedSymbol,
                    final List<String> notes){
        this.usageId = usageId;
        this.section = section;
        this.symbol = symbol;
        this.modifiedSymbol = modifiedSymbol;
        this.notes = notes;
    }

    public boolean isModifiable(){
        return (modifiedSymbol != null);
    }

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();
        sb.append(usageId).append(":")
                .append(section == null ? "" : section + "-")
                .append(modifiedSymbol == null ? symbol : "(" + symbol + "," + modifiedSymbol + ")")
                //.append(notes.isEmpty() ? "" :  "\t" + notes)
        ;
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyCode keyCode = (KeyCode) o;

        if (usageId != keyCode.usageId) return false;
        if (section != keyCode.section) return false;
        if (!symbol.equals(keyCode.symbol)) return false;
        if (modifiedSymbol != null ? !modifiedSymbol.equals(keyCode.modifiedSymbol) : keyCode.modifiedSymbol != null)
            return false;
        return notes.equals(keyCode.notes);

    }

    @Override
    public int hashCode() {
        int result = usageId;
        result = 31 * result + (section != null ? section.hashCode() : 0);
        result = 31 * result + symbol.hashCode();
        result = 31 * result + (modifiedSymbol != null ? modifiedSymbol.hashCode() : 0);
        result = 31 * result + notes.hashCode();
        return result;
    }

    public static void main(final String[] args){
        System.out.println("Hello World!");

        for(KeyCode kc: KeyCode.usageIdToKeyCode.values()){
            System.out.println(kc);
        }

        System.out.println("Goodbye World!");
    }

}
