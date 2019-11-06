package org.sample.typeclass;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main2 {

    public static void main(String[] args) {
        ppOnad();
    }

    private static void ppOnad() {
        final var listMonad = ListKind.Instance.INSTANCE;

        final var l1234 = new ListKind<>(List.of(1, 2, 3, 4).stream()
                .map(e -> ((Function<String, String>) (String o) -> o.repeat(e)))
                .collect(Collectors.toList()));
        final var l67 = new ListKind<>(List.of("6", "7"));

        final Kind< ListKind.mu,  String> apply = listMonad.ap(l1234).apply(l67);

        System.out.println(ListKind.narrow(apply).getDelegate());

        final var res = listMonad.flatMap(l67,
                f -> listMonad.<Function<String, String>, String>flatMap(l1234,
                s -> listMonad.pure(s.apply(f)))
        );

        System.out.println(ListKind.narrow(res).getDelegate());
    }

    private static void onad() {
        final var idMonad = Identity.Instance.INSTANCE;

        final var five = idMonad.pure(5);
        final Function<
                 Function<
                         Integer,
                         Kind< Identity.mu,  Integer>
                        >,
                 Kind< Identity.mu,  Integer>> fl1 = idMonad.flatMap(five);

        final Function<
                 Integer,
                Kind< Identity.mu,  Integer>
                > aa = a -> idMonad.pure(a);

        System.out.println(Identity.narrow(fl1.apply(aa)).getValue());

        final Kind< Identity.mu,  Integer> flsum = idMonad.flatMap(five,
                a -> idMonad.<Integer, Integer>flatMap(idMonad.pure(a),
                c -> idMonad.pure(c + a))
        );

        System.out.println(Identity.narrow(flsum).getValue());
    }

    private static void unctorPp() {
        final var idApp = Identity.Instance.INSTANCE;
        final var f15 = idApp.pure(15);

        final Function< Integer,  String> repeat = "e"::repeat;

        final var itos = idApp.pure(repeat);

        final var usingFunctor = idApp.map(repeat).andThen(Identity::narrow).andThen(Identity::getValue);
        final var usingApp = idApp.ap(itos).andThen(Identity::narrow).andThen(Identity::getValue);

        System.out.println(usingFunctor.apply(f15));
        System.out.println(usingApp.apply(f15));
    }

    private static void unctor() {
        final var idFunctor = Identity.Instance.INSTANCE;
        final Function< Kind< Identity.mu,  Integer>,  Kind< Identity.mu,  Integer>> itoi =
                idFunctor.map("e"::repeat).andThen(idFunctor.map(String::hashCode));
        final Function< Kind< Identity.mu,  Integer>,  Identity< Integer>> idtoi = Identity::narrow;

        final var five = new Identity<Integer>(5);

        System.out.println(itoi.andThen(Identity::narrow).andThen(Identity::getValue).apply(five));
    }
}

interface Kind<WITNESS, T> { }

interface Functor<WITNESS> {
    <A, B> Function< Kind< WITNESS,  A>,  Kind< WITNESS,  B>> map(final Function< A,  B> fn);
}

interface Applicative<WITNESS> extends Functor<WITNESS> {
    <A> Kind< WITNESS,  A> pure(final A a);

    <A, B> Function<
             Kind< WITNESS,  A>,
             Kind< WITNESS,  B>
            > ap(final Kind< WITNESS,  Function< A,  B>> hfn);
}

interface Monad<WITNESS> extends Applicative<WITNESS> {

    default <A, B> Function<
             Function<
                     A,
                     Kind< WITNESS,  B>
                    >,
             Kind< WITNESS,  B>> flatMap(
            final Kind< WITNESS,  A> ma
    ) {
        return aToMb -> flatMap(ma, aToMb);
    }
    default <A, B> Kind< WITNESS,  B> flatMap(
            Kind< WITNESS,  A> ma,
            Function< A,  Kind< WITNESS,  B>> aToMb
    ) {
        return this.<A, B>flatMap(ma).apply(aToMb);
    }

    @Override
    default <A, B> Function< Kind< WITNESS,  A>,  Kind< WITNESS,  B>> map(
            final Function< A,  B> fn
    ) {
        final Function< A,  Kind< WITNESS,  B>> aToBk = fn.andThen(e -> this.pure(e));

        return maK -> this.<A, B>flatMap(maK, aToBk);
    }

    @Override
    default <A, B> Function< Kind< WITNESS,  A>,  Kind< WITNESS,  B>> ap(
            final Kind< WITNESS,  Function< A,  B>> hfn
    ) {
        return fa ->
                this.<Function< A,  B>, B>flatMap(hfn,
                        fab -> this.<A, B>flatMap(fa, fab.andThen(e -> this.pure(e)))
                );
    }
}

class Identity<T> implements Kind<Identity.mu, T> {
    private final T value;

    Identity(final T value) { this.value = value; }

    public final T getValue() { return value; }

    public static <T> Identity< T> narrow(final Kind< Identity.mu,  T> kind) {
        return (Identity< T>) kind;
    }

    public static final class mu { }

    public enum Instance implements Monad<Identity.mu> {
        INSTANCE;

        @Override
        public final <A, B> Function< Kind< mu,  A>,  Kind< mu,  B>> map(
                final Function< A,  B> fn
        ) {
            final Function< Kind< Identity.mu,  A>,  Identity< A>> idtoi = Identity::narrow;
            return idtoi.andThen(Identity::getValue).andThen(fn).andThen(Identity::new);
        }

        @Override
        public final <A> Kind< mu,  A> pure(final A a) {
            return new Identity<>(a);
        }

        @Override
        public final <A, B> Function< Kind< mu,  A>,  Kind< mu,  B>> ap(
                final Kind< mu,  Function< A,  B>> kfab
        ) {
            final var fab = Identity.narrow(kfab).value;
            final Function< Kind< mu,  A>,  A> katoa = ka -> Identity.narrow(ka).value;

            return katoa.andThen(fab).andThen(this::pure);
        }

        @Override
        public <A, B> Function< Function< A,  Kind< mu,  B>>,  Kind< mu,  B>> flatMap(
                final Kind< mu,  A> ma
        ) {
            final var a = Identity.narrow(ma).getValue();

            return fatoMb -> fatoMb.apply(a);
        }
    }
}

class ListKind<T> implements Kind<ListKind.mu, T> {
    private final List< T> delegate;

    ListKind(final List< T> delegate) {
        this.delegate = delegate;
    }

    public final List< T> getDelegate() {
        return delegate;
    }

    public static <T> ListKind< T> narrow(final Kind< ListKind.mu,  T> kind) {
        return (ListKind< T>) kind;
    }

    public static final class mu { }

    public enum Instance implements Monad<ListKind.mu> {
        INSTANCE;

        @Override
        public <A, B> Function< Kind< mu,  A>,  Kind< mu,  B>> map(
                final Function< A,  B> fn
        ) {
            final Function< A,  Kind< mu,  B>> aToBk = fn.andThen(this::pure);

            return maK -> this.<A, B>flatMap(maK, aToBk);
        }

        @Override
        public <A> Kind< mu,  A> pure(A a) {
            return new ListKind<>(List.of(a));
        }

        @Override
        public <A, B> Function< Kind< mu,  A>,  Kind< mu,  B>> ap(
                final Kind< mu,  Function< A,  B>> hfn
        ) {
            return fa ->
                    this.<Function< A,  B>, B>flatMap(hfn,
                            fab -> this.<A, B>flatMap(fa, fab.andThen(this::pure))
                    );
        }

        @Override
        public <A, B> Kind< mu,  B> flatMap(
                final Kind< mu,  A> ma,
                final Function< A,  Kind< mu,  B>> aToMb
        ) {
            final List< A> as = ListKind.narrow(ma).getDelegate();

            final Function<Kind< ListKind.mu,  B>, ListKind< B>> narrow = ListKind::narrow;

            final var mapper =
                    aToMb
                            .andThen(narrow)
                            .andThen(ListKind::getDelegate)
                            .andThen(Collection::stream);

            final var result = as
                    .stream()
                    .flatMap(mapper)
                    .collect(Collectors.toList());

            return new ListKind<B>(result);
        }
    }
}
