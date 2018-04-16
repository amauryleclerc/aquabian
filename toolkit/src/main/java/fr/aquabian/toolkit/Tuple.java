package fr.aquabian.toolkit;

import java.util.Objects;

public class Tuple<A,B> {

    private final A a;
    private final B b;

    private Tuple(A a, B b){
        this.a = a;
        this.b = b;
    }

    public static <A,B> Tuple<A,B> tuple(A a, B b){
        return new Tuple<>(a,b);
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(a, tuple.a) &&
                Objects.equals(b, tuple.b);
    }

    @Override
    public int hashCode() {

        return Objects.hash(a, b);
    }
}
