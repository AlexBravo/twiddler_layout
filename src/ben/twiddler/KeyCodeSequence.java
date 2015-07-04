package ben.twiddler;

import java.util.ArrayList;
import java.util.List;

import static ben.util.Guards.*;

/**
 * Created by benh on 5/14/15.
 */
public class KeyCodeSequence {

    public final List<ModifiedKeyCode> sequence;

    public KeyCodeSequence(final List<ModifiedKeyCode> sequence){
        this.sequence = sequence;
    }

    public static KeyCodeSequence parseFrom(final byte[] bytes, final int offset, final int length){
        require(length % 2 == 0);
        final List<ModifiedKeyCode> sequence = new ArrayList<>();
        int offsetToKeyCode = offset;
        while(offsetToKeyCode < offset + length){
            final ModifiedKeyCode keyCode = ModifiedKeyCode.parseFrom(bytes, offsetToKeyCode);
            sequence.add(keyCode);
            offsetToKeyCode += 2;
        }
        assume(offsetToKeyCode == offset + length);
        return new KeyCodeSequence(sequence);
    }

    public static KeyCodeSequence parseFrom(final String pattern){
        final Parser parser = new Parser();
        for(int i = 0; i < pattern.length(); ++i){
            char c = pattern.charAt(i);
            parser.update(c);
        }
        parser.terminate();
        final List<ModifiedKeyCode> sequence = new ArrayList<>();
        for(final String token: parser.getTokens()){
            sequence.add(ModifiedKeyCode.parseFrom(token));
        }
        return new KeyCodeSequence(sequence);
    }

    // "\[", "\]", and "\\" are the only special character sequences
    // all instances of "\", "[", and "]" when not part of above sequences are simply those characters
    // tokenize into ModifiedKeyCode(s) (only top level)
    // {level: int, escaping: bool} = {0, false}
    // a                     (1 token)
    // [DEL]                 (1 token)
    // a[DEL]b               (3 tokens)
    // [[LCTRL]c]            (1 token)
    // [[LCTRL]x]q[[LCTRL]a] (3 tokens)
    // [[LCTRL][LALT][DEL]]  (1 token)
    private static final class Parser {
        private boolean terminated = false;
        private int level = 0;
        private boolean escaping = false;
        private StringBuilder modifiedKeyCodeToken = null;
        private List<String> modifiedKeyCodeTokens = new ArrayList<>();

        public void update(final Character c) {
            assume(!terminated);
            if (escaping){
                // if we're escaping, we're saying the next char doesn't affect the level no matter what
                // we need to leave the escaping in for ModifiedKeyCode to (re)parse
                addChar('\\');
                addChar(c);
                escaping = false;
                if (level == 0){
                    endToken();
                }
            } else { // !escaping
                if (c == '\\') {
                    escaping = true;
                } else {
                    if (c == '['){
                        level += 1;
                        assume(level <= 2);
                    } else if (c == ']'){
                        level -= 1;
                        assume(level >= 0);
                    }
                    addChar(c);
                    if (level == 0){
                        endToken();
                    }
                }
            }
        }

        private void addChar(final char c){
            if (modifiedKeyCodeToken == null){
                modifiedKeyCodeToken = new StringBuilder();
            }
            modifiedKeyCodeToken.append(c);
        }

        private void endToken(){
            modifiedKeyCodeTokens.add(modifiedKeyCodeToken.toString());
            modifiedKeyCodeToken = null;
        }

        public void terminate() {
            assume(!terminated);
            assume(level == 0);
            if (escaping){
                addChar('\\');
            }
            if (modifiedKeyCodeToken != null){
                endToken();
            }
            terminated = true;
        }

        public List<String> getTokens(){
            assume(terminated);
            return modifiedKeyCodeTokens;
        }
    }

    public void writeTo(final StringBuilder stringBuilder){
        for(final ModifiedKeyCode mck: sequence){
            mck.writeTo(stringBuilder);
        }
    }

    public int writeTo(final byte[] bytes, final int offset){
        require(offset + 2 * sequence.size() - 1 < bytes.length); // max-index-to-write < bytes.length
        int thisOffset = offset;
        for(final ModifiedKeyCode mkc: sequence){
            thisOffset += mkc.writeTo(bytes, thisOffset);
        }
        return thisOffset - offset;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(final ModifiedKeyCode mck: sequence){
            sb.append(mck.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        KeyCodeSequence that = (KeyCodeSequence) o;

        return sequence.equals(that.sequence);
    }

    @Override
    public int hashCode() {
        return sequence.hashCode();
    }

}
