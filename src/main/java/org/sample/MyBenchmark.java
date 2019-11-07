package org.sample;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class MyBenchmark {

    public static void main(String[] args) {

//        Just "Hello " >>= \str1 ->  Just "World" >>= \str2 ->  Just (str1 ++ str2)
//        (>>=) (Just "Hello ") (\str1 ->  (>>=) (Just "World") (\str2 ->  Just (str1 ++ str2)))
//
        final var eitherMonad = new EitherMonad<String>();
        final var pure14 = eitherMonad.pure(14);

        final Function<? super Integer, ? extends Boolean> isEven = e -> e % 2 == 0;

        final Function<? super Integer, Higher<EitherKind.mu<String>, ? extends Boolean>> toBool =
                e ->
                        new EitherKind<>(new Either.Right<>(isEven.apply(e)));

        final Higher<EitherKind.mu<String>, ? extends Boolean> apply = eitherMonad.<Integer, Boolean>flatMap(pure14).apply(toBool);
    }

    private static void eitherApplicative() {
        final var eitherApp = new EitherApplicaitve<String>();

        final Either<String, Integer> left = new Either.Left<>("Hello");
        final Either<String, Integer> right = new Either.Right<>(15);

        final var leftK = new EitherKind<>(left);
        final var rightK = new EitherKind<>(right);

        final var pure14 = eitherApp.pure(14);

        final Function<? super Integer, ? extends Boolean> isEven = e -> e % 2 == 0;

        final Higher<EitherKind.mu<String>, Function<? super Integer, ? extends Boolean>> isEvenK = eitherApp.pure(isEven);

        final var ap = eitherApp.ap(isEvenK);
        Stream.of(ap.apply(leftK), ap.apply(pure14), ap.apply(rightK))
                .map(EitherKind::narrowK)
                .forEach(e ->
                        System.out.println(
                                switch (e.part()) {
                                    case LEFT -> e.left();
                                    case RIGHT -> e.right();
                                }
                        )
                );
    }

    private static void eitherFunctor() {
        final Either<String, Integer> left = new Either.Left<>("Hello");
        final Either<String, Integer> right = new Either.Right<>(15);
        final var leftK = new EitherKind<>(left);
        final var rightK = new EitherKind<>(right);
        final var eitherFunctor = new EitherFunctor<String>();
        final Function<Integer, String> toStr = "$"::repeat;
        final var toStrK = eitherFunctor.map(toStr);
        Stream.of(
                EitherKind.narrowK(toStrK.apply(leftK)),
                EitherKind.narrowK(toStrK.apply(rightK))).forEach(
                e ->
                        System.out.println(
                                switch (e.part()) {
                                    case LEFT -> e.left();
                                    case RIGHT -> e.right();
                                }
                        )
        );
    }

    private static void ex1() {
        final List<Integer> listKind = new ArrayList<>(){{add(1); add(3); add(10); add(2);}};
        ListFunctor lf = new ListFunctor();

        final Function<Integer, String> repeat = e ->
                switch(e) {
                    case 1 -> "One";
                    case 2 -> "two";
                    case 3 -> "Three";
                    default -> "Any";
                };
        final Function<String, Character> firstCh = s -> s.charAt(0);
        final var repK = new FunctionKind<>(repeat);
        final var functor = new FunctionFunctor<Integer>();
        final var map = functor.map(firstCh);
        final var fnk = FunctionKind.narrowK(map.apply(repK));

        final var mapper = lf.map(fnk);
        final var res = mapper.apply(new ListKind<>(listKind));
        ListKind.narrowK(res).forEach(System.out::println);
    }
}

interface Higher<WITNESS,T> { }

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

final class EitherKind<L, R> implements Higher<EitherKind.mu<L>, R> {

    private final Either<L, R> delegate;

    EitherKind(final Either<L, R> delegate) {
        this.delegate = delegate;
    }

    public static class mu<L> { }

    public static <L, R> Either<L, R> narrowK(Higher<EitherKind.mu<L>, R> hkt){
        return ((EitherKind<L, R>) hkt).delegate;
    }

    public <A> Function<
            Function<? super R, Higher<EitherKind.mu<L>, ? extends A>>,
            Higher<EitherKind.mu<L>, ? extends A>
            > flatMap(
    ) {
        final var eitherMonad = new EitherMonad<L>();
        return aToMb -> eitherMonad.flatMap(this, aToMb);
    }
}

final class ListKind<T> implements Higher<ListKind.mu, T> {
    private final List<T> delegate;

    ListKind(final List<T> delegate) {
        this.delegate = delegate;
    }

    public static class mu { }

    public static <T> List<T> narrowK(Higher<mu,T> hkt){
        return ((ListKind<T>) hkt).delegate;
    }
}

final class FunctionKind<A, B> implements Higher<FunctionKind.mu<A>, B> {
    public static class mu<A> { }

    private final Function<A, B> delegate;

    public FunctionKind(Function<A, B> delegate) {
        this.delegate = delegate;
    }

    public static <A, B> Function<A, B> narrowK(Higher<FunctionKind.mu<A>, B> hkt){
        return ((FunctionKind<A, B>) hkt).delegate;
    }
}

final class FunctionKind2<A, B> implements Higher2<FunctionKind2.mu, A, B> {
    public static class mu { }
    private final Function<A, B> delegate;

    public FunctionKind2(Function<A, B> delegate) {
        this.delegate = delegate;
    }

    public static <A, B> Function<A, B> narrowK(Higher2<FunctionKind2.mu, A, B> hkt){
        return ((FunctionKind2<A, B>) hkt).delegate;
    }

}

interface Functor<WITNESS> {
    <A, B> Function<Higher<WITNESS, A>, Higher<WITNESS, B>> map(Function<? super A, ? extends B> fn);
}

interface Applicative<WITNESS> extends Functor<WITNESS> {
    <A> Higher<WITNESS, A> pure(A a);

    <A, B> Function<Higher<WITNESS, A>, Higher<WITNESS, B>> ap(Higher<WITNESS, Function<? super A, ? extends B>> hfn);
}

interface Monad<WITNESS> extends Applicative<WITNESS> {

    default <A, B> Function<Function<? super A, Higher<WITNESS, ? extends B>>, Higher<WITNESS, ? extends B>> flatMap(
            Higher<WITNESS, A> ma
    ) {
        return aToMb -> flatMap(ma, aToMb);
    }
    default <A, B> Higher<WITNESS, ? extends B> flatMap(
            Higher<WITNESS, A> ma,
            Function<? super A, Higher<WITNESS, ? extends B>> aToMb
    ) {
        return this.<A, B>flatMap(ma).apply(aToMb);
    }
}

interface Profunctor<WITNESS> {
//    dimap :: (a -> b) -> (c -> d) -> p b c -> p a d
    <A, B, C, D> Function<Higher2<WITNESS, B, C>, Higher2<WITNESS, A, D>> dimap(
            Function<A, B> in, Function<C, D> out
    );
}

class ListFunctor implements Functor<ListKind.mu> {

    @Override
    public <A, B> Function<Higher<ListKind.mu, A>, Higher<ListKind.mu, B>> map(Function<? super A, ? extends B> fn) {
        final Function<Higher<ListKind.mu, A>, Higher<ListKind.mu, B>> f = lk -> ListKind.narrowK(lk).stream()
                .map(fn).collect(Collector.of(
                        (Supplier<ArrayList<B>>) ArrayList::new,
                        ArrayList::add,
                        (left, right) -> { left.addAll(right); return left; },
                        ListKind::new

                ));
        return f;
    }
}

class EitherFunctor<L> implements Functor<EitherKind.mu<L>> {

    @Override
    public <A, B> Function<Higher<EitherKind.mu<L>, A>, Higher<EitherKind.mu<L>, B>> map(Function<? super A, ? extends B> fn) {
        return start -> {
            final var value = EitherKind.narrowK(start);
            final var result = switch (value.part()) {
                case LEFT -> new EitherKind<>(new Either.Left<L, B>(value.left()));
                case RIGHT -> new EitherKind<>(new Either.Right<L, B>(fn.apply(value.right())));
            };
            return result;
        };
    }
}

class EitherApplicaitve<L> extends EitherFunctor<L> implements Applicative<EitherKind.mu<L>> {

    @Override
    public <A> Higher<EitherKind.mu<L>, A> pure(A a) {
        return new EitherKind<>(new Either.Right<>(a));
    }

    @Override
    public <A, B> Function<Higher<EitherKind.mu<L>, A>, Higher<EitherKind.mu<L>, B>> ap(
            Higher<EitherKind.mu<L>, Function<? super A, ? extends B>> hfn) {

        return hla -> {
            final var fab = EitherKind.narrowK(hfn);
            final var la = EitherKind.narrowK(hla);

            final Higher<EitherKind.mu<L>, B> res =
                    switch (fab.part()) {
                        case LEFT -> new EitherKind<>(new Either.Left<L, B>(la.left()));
                        case RIGHT -> {
                            final Function<Higher<EitherKind.mu<L>, A>, Higher<EitherKind.mu<L>, B>> mapper = map(fab.right());
                            yield mapper.apply(hla);
                        }
                    };
            return res;
        };
    }
}

class EitherMonad<L> extends EitherApplicaitve<L> implements Monad<EitherKind.mu<L>> {

    @Override
    public <A, B> Higher<EitherKind.mu<L>,? extends B> flatMap(
            Higher<EitherKind.mu<L>, A> maK,
            Function<? super A, Higher<EitherKind.mu<L>, ? extends B>> aToMb
    ) {
        final var ma = EitherKind.narrowK(maK);
        final var res = switch (ma.part()) {
            case LEFT -> new EitherKind<L, B>(new Either.Left<>(ma.left()));
            case RIGHT -> aToMb.apply(ma.right());
        };
        return res;
    }
}

class FunctionFunctor<R> implements Functor<FunctionKind.mu<R>> {

    @Override
    public <A, B> Function<Higher<FunctionKind.mu<R>, A>, Higher<FunctionKind.mu<R>, B>> map(Function<? super A, ? extends B> fn) {
        final Function<Higher<FunctionKind.mu<R>, A>, Higher<FunctionKind.mu<R>, B>> res = fun ->
                new FunctionKind<>(FunctionKind.narrowK(fun).andThen(fn));

        return res;
    }
}

class FunctionProfunctor implements Profunctor<FunctionKind2.mu> {

    @Override
    public <A, B, C, D> Function<Higher2<FunctionKind2.mu, B, C>, Higher2<FunctionKind2.mu, A, D>> dimap(
            Function<A, B> ab, Function<C, D> cd
    ) {
        return bcK -> {
            final var bc = FunctionKind2.narrowK(bcK);
            final var ad = ab.andThen(bc).andThen(cd);
            return new FunctionKind2<>(ad);
        };
    }
}
