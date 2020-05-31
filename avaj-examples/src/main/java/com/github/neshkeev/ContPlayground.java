package com.github.neshkeev;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.mtl.ContTKind;
import com.github.neshkeev.avaj.mtl.Id;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

public class ContPlayground {
    public static void main(String[] args) {
        heckPointedTest();
    }

    private static void heckPointedTest() {
        // if add "10" until the threshold is reached
        System.out.println(Checkpointed.<Integer>runCheckpointed(a -> a < 100).apply(addTens(1)));
        System.out.println(Checkpointed.<Integer>runCheckpointed(a -> a < 40).apply(addTens(1)));
        System.out.println(Checkpointed.<Integer>runCheckpointed(a -> a < 30).apply(addTens(1)));
        System.out.println(Checkpointed.<Integer>runCheckpointed(a -> a < 20).apply(addTens(1)));
        System.out.println(Checkpointed.<Integer>runCheckpointed(a -> a < 10).apply(addTens(1)));

        // exploring callCC: if passed more than 100 then proceed with 42
        ContTKind<@NotNull Integer, Id.@NotNull mu, @NotNull Integer> test1 = test(5);
        System.out.println(test1.getDelegate().andThen(Id::narrow).andThen(Id::getValue).apply(Id::new));

        ContTKind<@NotNull Integer, Id.@NotNull mu, @NotNull Integer> test2 = test(101);
        System.out.println(test2.getDelegate().andThen(Id::narrow).andThen(Id::getValue).apply(Id::new));

        ContTKind<@NotNull String, Id.@NotNull mu, @NotNull Integer> test3 = test(101);
        System.out.println(test3.getDelegate().andThen(Id::narrow).andThen(Id::getValue).apply(r -> new Id<>("x".repeat(r))));

        ContTKind<@NotNull String, Id.@NotNull mu, @NotNull Integer> test4 = test(100);
        System.out.println(test4.getDelegate().andThen(Id::narrow).andThen(Id::getValue).apply(r -> new Id<>("x".repeat(r))));

        ContTKind<@NotNull Integer, Id.@NotNull mu, @NotNull Integer> quux = quux();
        System.out.println(quux.getDelegate().andThen(Id::narrow).andThen(Id::getValue).apply(Id::new));

        // exploring callCC: safe integer division
        ContTKind<@NotNull Integer, Id.@NotNull mu, @NotNull Integer> sdiv1 = divSafe(5,
                2,
                s -> new ContTKind<>(c -> c.apply(s.length())));
        System.out.println(sdiv1.getDelegate().andThen(Id::narrow).andThen(Id::getValue).apply(Id::new));

        ContTKind<@NotNull String, Id.@NotNull mu, @NotNull Integer> sdiv2 = divSafe(5,
                0,
                s -> new ContTKind<>(c -> new Id<>(s)));
        System.out.println(sdiv2.getDelegate().andThen(Id::narrow).andThen(Id::getValue).apply(c -> new Id<>(c.toString())));
    }

    public static <R extends @NotNull Object> ContTKind<R, Id.@NotNull mu, @NotNull Integer> test(int x) {
        final ContTKind.ContTMonad<R, Id.@NotNull mu> contMonad = new ContTKind.ContTMonad<>(Id.IdMonad.INSTANCE);
        return contMonad.<Integer, Unit>callCC(
                k -> contMonad.flatMap(contMonad.pure(3),
                        a -> contMonad.flatMap(contMonad.when(x > 100, () -> k.apply(42)),
                        b -> contMonad.pure(a + x)
                ))
        );
    }

    public static <R extends @NotNull Object> ContTKind<R, Id.@NotNull mu, @NotNull Integer> divSafe(
            final int x,
            final int y,
            final Function<? super @NotNull String, ? extends @NotNull ContTKind<R, Id.@NotNull mu, @NotNull Integer>> handler
    ) {
        final ContTKind.ContTMonad<R, Id.@NotNull mu> contMonad = new ContTKind.ContTMonad<>(Id.IdMonad.INSTANCE);
        return contMonad.<Integer, String>callCC(
                ok -> contMonad.flatMap(contMonad.<String, Unit>callCC(
                        notOk ->
                                contMonad.flatMap(contMonad.when(y == 0, () -> notOk.apply("Denominator 0")),
                                        l -> ok.apply(x / y)
                                )
                ), handler)
        );
    }

    public static <R extends @NotNull Object> ContTKind<R, Id.@NotNull mu, @NotNull Integer> quux() {
        final ContTKind.ContTMonad<R, Id.@NotNull mu> contMonad = new ContTKind.ContTMonad<>(Id.IdMonad.INSTANCE);
        return contMonad.<Integer, Integer>callCC(
                k -> contMonad.flatMap(k.apply(5),
                l -> contMonad.pure(25)
                ));
    }

    public static Checkpointed<@NotNull Integer> addTens(Integer start) {
        final var contMonad = new ContTKind.ContTMonad<@NotNull Integer, Id.@NotNull mu>(Id.IdMonad.INSTANCE);

        return checkpoint -> {
            final var kind = contMonad.flatMap(checkpoint.apply(start),
                    x1 -> contMonad.flatMap(checkpoint.apply(x1 + 10),
                    x2 -> contMonad.flatMap(checkpoint.apply(x2 + 10),
                    x3 -> contMonad.flatMap(checkpoint.apply(x3 + 10),
                    x4 -> contMonad.pure(x4 + 10)
            ))));

            return ContTKind.narrow(kind);
        };
    }

}

// (a -> Cont a a) -> Cont a a
// (a -> (a -> a) -> a) -> (a -> a) -> a
interface Checkpointed<A extends @NotNull Object>
        extends Function<Function<A, ContTKind<A, Id.@NotNull mu, A>>, ContTKind<A, Id.@NotNull mu, A>> {

    // (a -> bool)
    // -> ((a -> (a -> a) -> a) -> (a -> a) -> a)
    // -> a
    static <A extends @NotNull Object> Function<Checkpointed<A>, A> runCheckpointed(final Predicate<A> pred) {

        final Function<
                ? super A,
                ? extends Function<
                        ? super Function<
                                ? super A,
                                ? extends App<Id.@NotNull mu, A>>,
                        ? extends App<Id.@NotNull mu, A>
                        >
                > evalCont = a -> c -> {
            @NotNull final A apply = c.andThen(Id::narrow)
                    .andThen(Id::getValue)
                    .apply(a);
            return pred.test(apply) ? c.apply(a) : new Id<>(a);
        };

        final Function<A, ContTKind<A, Id.@NotNull mu, A>> aContKindFunction =
                a -> evalCont.andThen(Checkpointed.<A>getCont()).apply(a);

        return checkpointed -> {
            final ContTKind<A, Id.@NotNull mu, A> contKindAa = checkpointed.apply(aContKindFunction);
            return contKindAa.getDelegate()
                    .andThen(Id::narrow)
                    .andThen(Id::getValue)
                    .apply(Id::new)
                    ;
        };
    }

    @NotNull private static <A extends @NotNull Object>
    Function<
            ? super Function<
                    ? super Function<
                            ? super A,
                            ? extends App<Id.@NotNull mu, A>
                            >,
                    ? extends App<Id.@NotNull mu, A>
                    >, ? extends ContTKind<A, Id.@NotNull mu, A>> getCont() {
        return c -> new ContTKind<A, Id.@NotNull mu, A>(c::apply);
    }
}
