package iti.kukumo.util;

import java.util.function.Function;

public class Pair<T,U> {

    private final T key;
    private final U value;

    public Pair(T key, U value) {
        this.key = key;
        this.value = value;
    }

    public T key() {
        return key;
    }

    public U value() {
        return value;
    }

    @Override
    public String toString() {
        return "{"+key.toString()+":"+value.toString()+"}";
    }

    
    public static <T,U> Function<T,Pair<T,U>> computeValue(Function<T,U> function) {
        return key -> new Pair<>(key, function.apply(key));
    }

}
