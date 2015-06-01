package ben.twiddler;

import ben.twiddler.enums.KeyboardSection;

import java.util.*;

import static ben.util.Guards.assume;

/**
 * Created by benh on 5/16/15.
 */
public class Symbol {
    private static final SortedMap<String, SortedSet<KeyCode>> symbolToKeyCodes = new TreeMap<>();
    static {
        for (KeyCode keyCode : KeyCode.usageIdToKeyCode.values()) {
            { // scope block
                SortedSet<KeyCode> keyCodes = symbolToKeyCodes.get(keyCode.symbol);
                if (keyCodes == null) {
                    keyCodes = new TreeSet<>(new KeyCodePreference(keyCode.symbol));
                    symbolToKeyCodes.put(keyCode.symbol, keyCodes);
                }
                keyCodes.add(keyCode);
            }
            if (keyCode.modifiedSymbol != null) {
                SortedSet<KeyCode> keyCodes = symbolToKeyCodes.get(keyCode.modifiedSymbol);
                if (keyCodes == null) {
                    keyCodes = new TreeSet<>(new KeyCodePreference(keyCode.modifiedSymbol));
                    symbolToKeyCodes.put(keyCode.modifiedSymbol, keyCodes);
                }
                keyCodes.add(keyCode);
            }

        }
    }

    public static List<String> getAllSymbols(){
        return new ArrayList<>(symbolToKeyCodes.keySet());
    }

    public static Set<KeyCode> getKeyCodes(final String symbol){
        Set<KeyCode> result =  symbolToKeyCodes.get(symbol);
        if (result == null){
            result = new TreeSet<>();
        }
        return result;
    }

    private static class KeyCodePreference implements Comparator<KeyCode> {

        private final String forSymbol;

        public KeyCodePreference(final String forSymbol){
            this.forSymbol = forSymbol;
        }

        @Override
        public int compare(KeyCode kc1, KeyCode kc2) {
            // prefer keyboard to keypad to neither
            if (!Objects.equals(kc1.section, kc2.section)){
                if (KeyboardSection.KEYBOARD.equals(kc1.section)) {
                    return -1;
                } else if (KeyboardSection.KEYBOARD.equals(kc2.section)) {
                    return 1;
                } else if (KeyboardSection.KEYPAD.equals(kc1.section)) {
                    return -1;
                } else if (KeyboardSection.KEYPAD.equals(kc2.section)) {
                    return 1;
                }
                assume(false);
            }
            return kc1.usageId - kc2.usageId;
        }
    }

    public static void main(final String[] args){
        System.out.println("Hello World!");

        for(final Map.Entry<String, SortedSet<KeyCode>> e: Symbol.symbolToKeyCodes.entrySet()){
            final String symbol = e.getKey();
            final Set<KeyCode> keyCodes = e.getValue();
            System.out.println(symbol + "\t:\t" + keyCodes);
        }

        System.out.println("\n\nAmbiguous:");
        for(final Map.Entry<String, SortedSet<KeyCode>> e: Symbol.symbolToKeyCodes.entrySet()){
            final String symbol = e.getKey();
            final Set<KeyCode> keyCodes = e.getValue();
            if (keyCodes.size() > 1){
                System.out.println(symbol + "\t:");
                for(final KeyCode keyCode: keyCodes){
                    System.out.println("\t" + keyCode);
                }
            }
        }

        System.out.println("Goodbye World!");
    }
}
