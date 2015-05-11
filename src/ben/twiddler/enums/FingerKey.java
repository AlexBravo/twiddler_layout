package ben.twiddler.enums;

/**
 * Created by benh on 5/3/15.
 */
public enum FingerKey {
    LEFT("L"), MIDDLE("M"), RIGHT("R");
    private final String string;
    private FingerKey(final String string) {
        this.string = string;
    }
    @Override public String toString(){
        return string;
    }
}
