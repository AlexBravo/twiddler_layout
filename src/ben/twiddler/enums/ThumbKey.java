package ben.twiddler.enums;

/**
 * Created by benh on 5/3/15.
 */
public enum ThumbKey {
    NUM("N"), ALT("A"), CTRL("C"), SHIFT("S");
    private final String string;
    private ThumbKey(final String string) {
        this.string = string;
    }
    @Override public String toString(){
        return string;
    }
}
