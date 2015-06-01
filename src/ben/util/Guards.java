package ben.util;

/**
 * Created by benh on 5/16/15.
 */
public class Guards {

    public static void require(final boolean condition){
        require(condition, "");
    }

    public static void require(final boolean condition, final String message){
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assume(final boolean condition){
        assume(condition, "");
    }

    public static void assume(final boolean condition, final String message){
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

}
