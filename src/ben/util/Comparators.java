package ben.util;

import java.util.Comparator;

/**
 * Created by benh on 6/28/15.
 */
public class Comparators {

    public static <T> Comparator<T> reverse(final Comparator<T> comparator){
        return new Comparator<T>(){
            @Override
            public int compare(T t1, T t2) {
                return -comparator.compare(t1, t2);
            }
        };
    }

}
