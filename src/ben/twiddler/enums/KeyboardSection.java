package ben.twiddler.enums;

/**
 * Created by benh on 5/10/15.
 */
public enum KeyboardSection {
    KEYBOARD("Kybd", ""), KEYPAD("Kypd", "#");
    private final String longString;
    private final String shortString;

    private KeyboardSection(final String longString, final String shortString) {
        this.longString = longString;
        this.shortString = shortString;
    }

    public String toLongString() {
        return this.longString;
    }

    public String toShortString() {
        return this.shortString;
    }

    @Override
    public String toString() {
        return toLongString();
    }
}
