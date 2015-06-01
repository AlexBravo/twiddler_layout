package ben.twiddler;

import ben.twiddler.data.DisambiguatedSymbolKeyCodes;

import java.util.*;

import static ben.util.Data.*;
import static ben.util.Guards.assume;

/**
 * Created by benh on 5/15/15.
 */
public class ModifiedKeyCode {

    public final EnumSet<KeyCodeModifier> modifiers;
    public final KeyCode keyCode;

    public ModifiedKeyCode(final EnumSet<KeyCodeModifier> modifiers, final KeyCode keyCode){
        this.modifiers = modifiers;
        this.keyCode = keyCode;
    }

    public ModifiedKeyCode(final KeyCode keyCode){
        this(EnumSet.noneOf(KeyCodeModifier.class), keyCode);
    }

    public ModifiedKeyCode(final KeyCodeModifier modifier, final KeyCode keyCode){
        this(EnumSet.of(modifier), keyCode);
    }

    public static ModifiedKeyCode parseFrom(final byte[] data, final int offset) {
        EnumSet<KeyCodeModifier> modifiers = KeyCodeModifier.parseFromBinary(data, offset);
        int usageId = readInt(data, offset + 1, 1);
        KeyCode keyCode = KeyCode.getKeyCode(usageId);
        return new ModifiedKeyCode(modifiers, keyCode);
    }

    public static ModifiedKeyCode parseFrom(final String pattern){
        final Parser parser = new Parser();
        for(int i = 0; i < pattern.length(); ++i){
            char c = pattern.charAt(i);
            parser.update(c);
        }
        parser.terminate();
        final EnumSet<KeyCodeModifier> modifiers = EnumSet.noneOf(KeyCodeModifier.class);
        KeyCode keyCode = null;
        boolean symbolWasShifted = false;
        for(final String token: parser.getTokens()) {
            try {
                KeyCodeModifier modifier = KeyCodeModifier.valueOf(token);
                modifiers.add(modifier);
            } catch (final IllegalArgumentException iae) {
                // if not a modifier, it must be a keyCode
                keyCode = DisambiguatedSymbolKeyCodes.getKeyCode(token);
                if (!token.equals(keyCode.symbol)){
                    assume(token.equals(keyCode.modifiedSymbol));
                    symbolWasShifted = true;
                }
            }
        }
        assume(keyCode != null);
        if (symbolWasShifted && !modifiers.contains(KeyCodeModifier.LSHFT) && !modifiers.contains(KeyCodeModifier.RSHFT)){
            modifiers.add(KeyCodeModifier.RSHFT);
        }
        return new ModifiedKeyCode(modifiers, keyCode);
    }

    // "\[", "\]", and "\\" are the only special character sequences
    // all instances of "\", "[", and "]" when not part of above sequences are simply those characters
    // tokenize into leading Modifiers, and final symbol
    // {level: int, escaping: bool} = {0, false}
    // a                    (1 token)
    // [DEL]                (1 token)
    // [[LCTRL]c]           (2 tokens)
    // [[LCTRL][LALT][DEL]] (3 tokens)
    private static final Set<Character> ESCAPABLE = new HashSet<>(Arrays.asList('\\', '[', ']'));
    private static final class Parser {
        private boolean terminated = false;
        private int level = 0;
        private boolean escaping = false;
        private StringBuilder token = null;
        private List<String> tokens = new ArrayList<>();

        public void update(final Character c) {
            assume(!terminated);
            if (escaping){
                if (ESCAPABLE.contains(c)) {
                    addChar(c);
                } else {
                    addChar('\\');
                    addChar(c);
                }
                escaping = false;
            } else { // !escaping
                if (c == '\\') {
                    escaping = true;
                } else {
                    if (c == '['){
                        level += 1;
                        assume(level <= 2);
                        endToken();
                    } else if (c == ']'){
                        level -= 1;
                        assume(level >= 0);
                        endToken();
                    } else {
                        addChar(c);
                    }
                }
            }
        }

        private void addChar(final char c){
            if (token == null){
                token = new StringBuilder();
            }
            token.append(c);
        }

        private void endToken(){
            if (token != null) {
                tokens.add(token.toString());
            }
            token = null;
        }

        public void terminate() {
            assume(!terminated);
            assume(level == 0);
            if (escaping){
                addChar('\\');
            }
            if (token != null){
                endToken();
            }
            terminated = true;
        }

        public List<String> getTokens(){
            assume(terminated);
            return tokens;
        }
    }

    public void writeTo(final StringBuilder stringBuilder){
        EnumSet<KeyCodeModifier> effectiveModifiers = EnumSet.copyOf(modifiers);
        boolean isModified = false;
        if (keyCode.isModifiable()) { // we will modify the keyCode, and not output SHFTs
            effectiveModifiers.remove(KeyCodeModifier.LSHFT);
            effectiveModifiers.remove(KeyCodeModifier.RSHFT);
            isModified = modifiers.contains(KeyCodeModifier.LSHFT) ||
                    modifiers.contains(KeyCodeModifier.RSHFT);
        }
        final boolean outer = !effectiveModifiers.isEmpty();

        if (outer){  stringBuilder.append("[");  }
        for(KeyCodeModifier kcm: effectiveModifiers){
            stringBuilder.append("[").append(kcm).append("]");
        }
        String symbol = keyCode.symbol;
        if (isModified){  symbol = keyCode.modifiedSymbol;  }
        if (symbol.length() == 1){
            if (ESCAPABLE.contains(symbol.charAt(0))){
                stringBuilder.append('\\');
            }
            stringBuilder.append(symbol);
        } else { // multi-character symbol
            stringBuilder.append("[").append(symbol).append("]");
        }
        if (outer){  stringBuilder.append("]");  }
    }

    @Override
    public String toString(){
        final StringBuilder sb = new StringBuilder();
        EnumSet<KeyCodeModifier> effectiveModifiers = EnumSet.copyOf(modifiers);
        boolean isModified = false;
        if (keyCode.isModifiable()) { // we will modify the keyCode, and not output SHFTs
            effectiveModifiers.remove(KeyCodeModifier.LSHFT);
            effectiveModifiers.remove(KeyCodeModifier.RSHFT);
            isModified = modifiers.contains(KeyCodeModifier.LSHFT) ||
                    modifiers.contains(KeyCodeModifier.RSHFT);
        }
        final boolean outer = !effectiveModifiers.isEmpty();

        if (outer){  sb.append("[");  }
        for(KeyCodeModifier kcm: effectiveModifiers){
            sb.append("[").append(kcm).append("]");
        }
        String symbol = keyCode.symbol;
        if (isModified){  symbol = keyCode.modifiedSymbol;  }
        sb.append("[").append(keyCode.usageId).append(":").append(symbol).append("]");
        if (outer){  sb.append("]");  }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModifiedKeyCode that = (ModifiedKeyCode) o;

        if (!modifiers.equals(that.modifiers)) return false;
        return keyCode.equals(that.keyCode);

    }

    @Override
    public int hashCode() {
        int result = modifiers.hashCode();
        result = 31 * result + keyCode.hashCode();
        return result;
    }

    public int writeTo(final byte[] bytes, final int offset){
        int thisOffset = offset;
        KeyCodeModifier.writeTo(modifiers, bytes, thisOffset);
        thisOffset += 1;
        writeLSBFirstInt(keyCode.usageId, bytes, thisOffset, 1);
        thisOffset += 1;
        return thisOffset - offset;
    }

}
