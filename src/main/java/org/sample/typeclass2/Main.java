package org.sample.typeclass2;

import java.util.function.Function;

public class Main {

    public static void main(String... args) {
        final var functor = Id.Instance.INSTANCE;

        final var itoS = functor.map("a"::repeat);
        final var stoI = functor.map(String::hashCode);

        final var function = itoS.andThen(stoI).andThen(Id::narrow).andThen(Id::getValue);

        final var id = new Id<>(5);

        System.out.println(function.apply(id));

        System.out.println(function.apply(new AA<>(52)));
        System.out.println(function.apply(new BB<>(55)));
    }

    public static class AA<T> extends Id<T> {

        AA(T value) {
            super(value);
        }
    }

    public static class BB<T> extends AA<T> {
        BB(T value) {
            super(value);
        }
    }
}

class Id<A> implements App<Id.Mu, A> {
    private final A value;

    Id(final A value) {
        this.value = value;
    }

    public static <R> Id<R> narrow(final App<? super Id.Mu,? extends R> app) {
        return (Id<R>) app;
    }

    public final A getValue() { return value; }

    public static final class Mu implements K1 {}

    public enum Instance implements Functor<Id.Mu, Instance.Mu> {
        INSTANCE;

        @Override
        public <A, B> Function<? super App<? super Id.Mu, ? extends A>, ? extends App<? super Id.Mu, ? extends B>> map(
                final Function<? super A, ? extends B> fab
        ) {
            final Function<? super App<? super Id.Mu, ? extends A>, ? extends Id<? extends A>> appToId =
                    app -> (Id<? extends A>) app;

            final Function<? super Id<? extends A>, ? extends A> toA = Id::getValue;

            return appToId.andThen(toA).andThen(fab).andThen(Id::new);
        }

        public static final class Mu implements Functor.Mu {}
    }
}

interface K1 {}
interface App<F extends K1, A> { }

interface Kind1<F extends K1, Mu extends Kind1.Mu> extends App<Mu, F> {

    interface Mu extends K1 {}
}

interface Functor<F extends K1, Mu extends Functor.Mu> extends Kind1<F, Mu> {

    <A, B> Function<? super App<? super F, ? extends A>, ? extends App<? super F, ? extends B>> map(
            Function<? super A, ? extends B> fab
    );

    interface Mu extends Kind1.Mu {}
}
