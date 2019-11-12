package com.github.neshkeev;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.data.Cont;
import com.github.neshkeev.avaj.data.kinds.ContKind;
import com.github.neshkeev.avaj.mtl.Id;
import com.github.neshkeev.avaj.mtl.Reader;
import com.github.neshkeev.avaj.mtl.ReaderT;
import com.github.neshkeev.avaj.mtl.ReaderTKind;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ContPlayground {
    public static void main(String[] args) {
        heckPointedTest();
    }

    private static void heckPointedTest() {
        System.out.println(Checkpointed.<Integer>runCheckpointed(a -> a < 100).apply(addTens(1)));
        System.out.println(Checkpointed.<Integer>runCheckpointed(a -> a < 40).apply(addTens(1)));
        System.out.println(Checkpointed.<Integer>runCheckpointed(a -> a < 30).apply(addTens(1)));
        System.out.println(Checkpointed.<Integer>runCheckpointed(a -> a < 20).apply(addTens(1)));
        System.out.println(Checkpointed.<Integer>runCheckpointed(a -> a < 10).apply(addTens(1)));
    }

    public static Checkpointed<Integer> addTens(Integer start) {
        final var contMonad = new ContKind.Instance<Integer>();

        return checkpoint -> {
            final var kind = contMonad.flatMap(checkpoint.apply(start),
                    x1 -> contMonad.flatMap(checkpoint.apply(x1 + 10),
                            x2 -> contMonad.flatMap(checkpoint.apply(x2 + 10),
                                    x3 -> contMonad.flatMap(checkpoint.apply(x3 + 10),
                                            x4 -> contMonad.pure(x4 + 10)
                                    ))));

            final var res = ContKind.narrow(kind);
            return res;
        };
    }

    private static void ainallCC() {
        final var contMonad = new ContKind.Instance<Boolean>();

        final var kind = withCallCC(contMonad, -5);
        System.out.println(kind.getDelegate().apply(e -> e % 2 == 0));

        final var kind2 = withCallCC(contMonad, 9);
        System.out.println(kind2.getDelegate().apply(e -> e % 2 == 0));

        final var mnd2 = new ContKind.Instance<Integer>();

        final var kind3 = withCallCC(mnd2, 15);
        System.out.println(kind3.getDelegate().apply(e -> e));

        final var mnd3 = new ContKind.Instance<List<Integer>>();
        final var kind4 = withCallCC(mnd3, -1);
        System.out.println(kind4.getDelegate().apply(List::of));
    }

    public static <R> ContKind<R, Integer> withCallCC(ContKind.Instance<R> contMonad, int val) {
        final var five = contMonad.pure(val);

        return ContKind.Instance.<Integer, Integer, R>callCC(
                k -> ContKind.narrow(
                        contMonad.flatMap(five,
                                a -> a < 0 ? k.apply(0) : contMonad.pure(a)
                        )
                )
        );
    }

    private static void ontList() {
        final var contMonad = new ContKind.Instance<List<Integer>>();
        final var five = contMonad.pure(5);

        final var const4 = new ContKind<List<Integer>, Integer>(
                c -> Stream.of(c.apply(4), c.apply(5), c.apply(4), c.apply(5))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList())
        );

        final var muIntegerKind = contMonad.flatMap(five,
                a -> contMonad.flatMap(const4,
                        b -> contMonad.pure(a + b))
        );
        System.out.println(ContKind.narrow(muIntegerKind).getDelegate().apply(List::of));
    }

    private static void allCC() {
        final var contMonad = new ContKind.Instance<Integer>();
        final var five = contMonad.pure(15);
        final var kind1 = ContKind.Instance.<Integer, Integer, Integer>callCC(
                k -> {
                    final var muIntegerKind = contMonad.flatMap(five,
                            e -> {
                                if (e > 5) {
                                    System.out.println("Greater");
                                    k.apply(42);
                                }
                                System.out.println("Keep");
                                return contMonad.pure(e);
                            });
                    return ContKind.narrow(muIntegerKind);
                }
        );
        System.out.println(ContKind.narrow(kind1).getDelegate().apply(Function.identity()));
    }

    private static void withMethodGen() {
        final var contMonad = new ContKind.Instance<Integer>();
        System.out.println(ff(contMonad).getDelegate().apply(Function.identity()));

        final var contMonad1 = new ContKind.Instance<String>();
        System.out.println(ff(contMonad1).getDelegate().apply(Object::toString));

        final var contMonad2 = new ContKind.Instance<List<Integer>>();
        System.out.println(ff(contMonad2).getDelegate().apply(List::of));
    }

    public static <R> ContKind<R, Integer> ff(ContKind.Instance<R> contMonad) {
        final var pure5 = contMonad.pure(5);

        final var muIntegerKind = contMonad.flatMap(pure5,
                b -> contMonad.pure(5 + b)
        );

        return ContKind.narrow(muIntegerKind);
    }

    private static void ontIntList() {
        final var contMonad = new ContKind.Instance<Integer>();
        final var cont3 = contMonad.pure(3);
        final var cont5 = contMonad.pure(5);

        final var kind = contMonad.flatMap(cont3,
                a -> contMonad.flatMap(cont5,
                        b -> contMonad.pure(a + b))
        );
        final var simple = ContKind.narrow(kind).getDelegate().apply(Function.identity());
        System.out.println(simple);

        final Cont<List<Integer>, Integer> cont = c ->
                Stream
                        .of(c.apply(4), c.apply(5), c.apply(4), c.apply(5))
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());

        final var contMonad1 = new ContKind.Instance<List<Integer>>();
        final var cont31 = contMonad1.pure(3);
        final var cont51 = contMonad1.pure(5);

        final var kind1 = contMonad1.flatMap(cont31,
                b -> contMonad1.flatMap(cont51,
                        a -> contMonad1.flatMap(new ContKind<>(cont),
                                c -> contMonad1.pure(List.of(a + b + c))
                        )));

        final var simple1 = ContKind.narrow(kind1).getDelegate().apply(Function.identity());
        System.out.println(simple1);
    }

    private static void umsquares() {
        final String r = ContPlayground.<String>sumSquares(3, 4).apply(e -> String.valueOf(e * e));
        System.out.println(r);
    }

    public static <T> Function<Function<Integer, ? extends T>, ? extends T> sumSquares(
            Integer a, Integer b
    ) {
        return cont -> ContPlayground.<T>square(a).apply(
                r1 -> ContPlayground.<T>square(b).apply(
                        r2 -> cont.apply(r1 + r2)
                ));
    }

    private static void urried35() {
        final String twelve = ContPlayground.<Function<Function<Integer, ? extends String>, ? extends String>>square(2)
                .apply(ContPlayground::add3)
                .apply(e -> ContPlayground.<String>add5(e).apply(Object::toString));
        System.out.println(twelve);

        final Function<Integer, ? extends Function<Integer, ? extends Function<Function<Integer, ? extends String>, ? extends String>>> curriedAdd =
                ContPlayground.<Integer, Integer, Function<Function<Integer, ? extends String>, ? extends String>>curry(ContPlayground::add);

        final Function<Integer, ? extends Function<Function<Integer, ? extends String>, ? extends String>> add31 = curriedAdd.apply(3);
        final Function<Integer, ? extends Function<Function<Integer, ? extends String>, ? extends String>> add51 = curriedAdd.apply(5);

        final Function<Integer, Function<Function<Integer, ? extends String>, ? extends String>> add3 =
                e -> add(3, e);

        final Function<Integer, Function<Function<Integer, ? extends String>, ? extends String>> add5 =
                e -> add(5, e);

        final String twelve2 = ContPlayground.<Function<Function<Integer, ? extends String>, ? extends String>>square(2)
                .apply(add3)
                .apply(e -> add5.apply(e).apply(Object::toString));

        final String twelve3 = ContPlayground.<Function<Function<Integer, ? extends String>, ? extends String>>square(2)
                .apply(add31)
                .apply(e -> add51.apply(e).apply(Object::toString));

        System.out.println(twelve2);
    }

    private static void ont35() {
        final Function<Integer, Function<Function<Integer, ? extends Integer>, ? extends Integer>> add3 =
                e -> add(3, e);

        final Function<Integer, Function<Function<Integer, ? extends Integer>, ? extends Integer>> add5 =
                e -> add(5, e);

        final Integer square = square(2, e -> add3.apply(e).apply(a -> add5.apply(a).apply(Function.identity())));
        System.out.println(square);

        final Function<Integer, Integer> integerObjectFunction = e -> add3.apply(e).apply(a -> add5.apply(a).apply(Function.identity()));
        final Integer apply = ContPlayground.<Integer>square(2).apply(integerObjectFunction);
        System.out.println(apply);
    }

    public static <T> Function<Function<Integer, ? extends T>, ? extends T> add3(Integer a) {
        return add(3, a);
    }

    public static <T> Function<Function<Integer, ? extends T>, ? extends T> add5(Integer a) {
        return add(5, a);
    }

    public static <T> Function<Function<Integer, ? extends T>, ? extends T> square(Integer e) {
        return cont -> cont.apply(e * e);
    }

    public static <T> T square(Integer a, Function<Integer, ? extends T> cont) {
        return cont.apply(a * a);
    }

    public static <T> Function<Function<Integer, ? extends T>, ? extends T> add(Integer a, Integer b) {
        return cont -> cont.apply(a + b);
    }

    public static <T> T add(Integer a, Integer b, Function<Integer, T> cont) {
        return cont.apply(a + b);
    }

    public static <A, B, R> Function<A, ? extends Function<B, ? extends R>> curry(BiFunction<A, B, R> func) {
        return a -> b -> func.apply(a, b);
    }

    private static void eader() {
        final var idMonad = Id.IdMonad.INSTANCE;
        final var rm = new Reader.ReaderMonad<Integer>();
        final var kind = rm.flatMap(rm.pure("hello"),
            h -> rm.flatMap(rm.lift(idMonad.pure("World")),
            w -> rm.flatMap(rm.asks(n -> n * 2),
            n -> rm.pure((h + ", " + w + "!\n").repeat(n))
        )));

        final var delegate = ReaderTKind.narrow(kind).getDelegate();
        final var lK = delegate.apply(3);

        System.out.println(Id.narrow(lK).getValue());
    }

    private static void rans() {
        final var idMonad = Id.IdMonad.INSTANCE;
        final ReaderTKind.ReaderTMonad<Integer, Id.mu> readerMonadT = new ReaderTKind.ReaderTMonad<Integer, Id.mu>(idMonad);
        final var greets = new Id<>("World");
        final ReaderT<Integer, Id.mu, String> gen = r -> greets;
        final var start = new ReaderTKind<>(gen);

        final App<ReaderTKind.mu<Integer, Id.mu>, String> kind = readerMonadT.flatMap(start,
                a -> readerMonadT.flatMap(readerMonadT.lift(idMonad.pure("World")),
                b -> readerMonadT.flatMap(readerMonadT.ask(),
                n -> readerMonadT.pure((a + ", " + b + "!\n").repeat(n))
        )));

        final var delegate = ReaderTKind.narrow(kind).getDelegate();
        final var lK = delegate.apply(2);

        System.out.println(Id.narrow(lK).getValue());
    }
}

interface Checkpointed<A> extends Function<Function<A, ContKind<A, A>>, ContKind<A, A>> {

    static <A> Function<Checkpointed<A>, A> runCheckpointed(final Predicate<A> pred) {
        final Function<A, Function<Function<A, A>, A>> evalCont =
                a -> c -> pred.test(c.apply(a)) ? c.apply(a) : a;

        final Function<Function<Function<A, A>, A>, ContKind<A, A>> cont =
                c -> new ContKind<>(c::apply);

        final Function<A, ContKind<A, A>> aContKindFunction =
                a -> evalCont.andThen(cont).apply(a);

        return checkpointed -> {
            final var contKindAa = checkpointed.apply(aContKindFunction);
            return contKindAa.getDelegate().apply(Function.identity());
        };
    }
}
