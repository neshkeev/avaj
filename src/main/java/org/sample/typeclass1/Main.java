package org.sample.typeclass1;

import java.util.function.Function;

public class Main {
    public static void main(String[] args) {
        final var e1 = new Either.Right<String, Integer>(5);
        final var eitherKind = new EitherKind<>(e1);
    }
}

interface Higher<WITNESS,T> extends Monad<WITNESS>{ }

interface Higher2<WITNESS,T, U> extends Higher<WITNESS, T> { }

interface Either<L, R> {
    enum SIGMA {
        LEFT, RIGHT
    }
    L left();
    R right();

    default SIGMA part() { return isRight() ? SIGMA.RIGHT : SIGMA.LEFT; }

    default boolean isLeft() { return !isRight(); }
    default boolean isRight() { return !isLeft(); }

    final class Left<L, R> implements Either<L, R> {
        private final L value;

        public Left(final L value) { this.value = value; }

        @Override
        public L left() { return value; }

        @Override
        public R right() { throw new UnsupportedOperationException("right is not implemented"); }

        @Override
        public boolean isLeft() { return true; }
    }

    final class Right<L, R> implements Either<L, R> {
        private final R value;

        public Right(final R value) { this.value = value; }

        @Override
        public L left() { throw new UnsupportedOperationException("right is not implemented"); }

        @Override
        public R right() { return value; }

        @Override
        public boolean isLeft() { return false; }
    }
}

interface Functor<WITNESS> {
    <A, B> Function<? super Higher<WITNESS, ? extends A>, ? extends Higher<WITNESS, ? extends B>> map(
            final Function<? super A, ? extends B> fn
    );
}

interface Applicative<WITNESS> extends Functor<WITNESS> {
    <A> Higher<WITNESS, ? extends A> pure(A a);

    <A, B> Function<? super Higher<WITNESS, ? extends A>, ? extends Higher<WITNESS, ? extends B>> ap(
            final Higher<WITNESS, Function<? super A, ? extends B>> fnK
    );
}

interface Monad<WITNESS> extends Applicative<WITNESS> {

    default <A, B> Function<? super Function<? super A, ? extends Higher<WITNESS, ? extends B>>, ? extends Higher<WITNESS, ? extends B>> flatMap(
            final Higher<WITNESS, ? extends A> maK
    ) {
        return aToMb -> flatMap(maK, aToMb);
    }

    default <A, B> Higher<WITNESS, ? extends B> flatMap(
            final Higher<WITNESS, ? extends A> maK,
            final Function<? super A, ? extends Higher<WITNESS, ? extends B>> aToMbK
    ) {
        return this.<A, B>flatMap(maK).apply(aToMbK);
    }
}

final class EitherKind<L, R> implements Higher<EitherKind.mu<L>, R> {

    private final Either<L, R> delegate;

    public EitherKind(final Either<L, R> delegate) {
        this.delegate = delegate;
    }

    @Override
    public <A, B> Function<? super Higher<mu<L>, ? extends A>, ? extends Higher<mu<L>, ? extends B>> map(
            final Function<? super A, ? extends B> fn
    ) {
        final Function<? super A, ? extends Higher<mu<L>, ? extends B>> aToBk = fn.andThen(this::pure);

        return maK -> this.<A, B>flatMap(maK, aToBk);
    }

    @Override
    public <A> Higher<mu<L>, ? extends A> pure(final A a) {
        return new EitherKind<>(new Either.Right<>(a));
    }

    @Override
    public <A, B> Function<? super Higher<mu<L>, ? extends A>, ? extends Higher<mu<L>, ? extends B>> ap(
            final Higher<mu<L>, Function<? super A, ? extends B>> fnK
    ) {
        return maK ->
                this.<Function<? super A, ? extends B>, B>flatMap(
                        fnK,
                        aToB -> {
                            final Function<? super A, ? extends Higher<mu<L>, ? extends B>> aToMbK =
                                    aToB.andThen(this::pure);

                            return this.<A, B>flatMap(maK, aToMbK);
                        }
                );
    }

    @Override
    public <A, B> Higher<mu<L>, ? extends B> flatMap(
            final Higher<mu<L>, ? extends A> maK,
            final Function<? super A, ? extends Higher<mu<L>, ? extends B>> aToMbK
    ) {
        final var ma = narrowK(maK);
        final var res = switch (ma.part()) {
            case LEFT -> new EitherKind<L, B>(new Either.Left<>(ma.left()));
            case RIGHT -> aToMbK.apply(ma.right());
        };
        return res;
    }

    public static final class mu<L> { }

    public static <L, R> Either<L, R> narrowK(Higher<EitherKind.mu<L>, R> hkt){
        return ((EitherKind<L, R>) hkt).delegate;
    }
}

