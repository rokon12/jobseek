package com.bazlur.jobseek;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.IntFunction;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/27/16.
 */
public interface Tuple<T extends Tuple> extends IntFunction, Cloneable, Serializable, Comparable<T> {

    static <T0, T1> Tuple2<T0, T1> valueOf(T0 _0, T1 _1) {
        return Tuple2.valueOf(_0, _1);
    }

    int size();

    @Override
    default int compareTo(T o) {
        Objects.requireNonNull(o);
        if (!getClass().equals(o.getClass())) {
            throw new ClassCastException(o.getClass() + " must equal " + getClass());
        }

        for (int i = 0; i < size(); i++) {
            @SuppressWarnings("unchecked")
            Comparable<Object> l = (Comparable<Object>) apply(i);
            Object r = o.apply(i);
            int c = l.compareTo(r);
            if (c != 0) {
                return c;
            }
        }

        return 0;
    }
}
