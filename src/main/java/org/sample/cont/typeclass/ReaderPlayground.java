package org.sample.cont.typeclass;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReaderPlayground {

    public static void main(String[] args) {
        final var listMonad = new ListKind.ListMonad();
        final var readerMonadT = new ReaderTKind.ReaderTMonad<Integer, ListKind.mu>(listMonad);
        final var greets = new ListKind<>(List.of("Hello, ", "Goodbye, "));
        final ReaderT<Integer, ListKind.mu, String> gen = r -> greets;
        final var start = new ReaderTKind<>(gen);

        final var kind = readerMonadT.flatMap(start,
                a -> readerMonadT.flatMap(readerMonadT.lift(listMonad.pure("World")),
                b -> readerMonadT.flatMap(readerMonadT.ask(),
                n -> readerMonadT.pure((a + b + "\n").repeat(n))
        )));

        final var delegate = ReaderTKind.narrow(kind).getDelegate();
        final var lK = delegate.apply(2);

        ListKind.narrow(lK).getDelegate().forEach(System.out::println);
    }

    private static void plainEader() {
        final var readerMonad = new Reader.ReaderMonad<Integer>();
        final var idMonad = Id.Instance.INSTANCE;

        final var start = new Reader<>("Hello, "::repeat);

        final var kind = readerMonad.flatMap(readerMonad.local(e -> e * 2, start),
                a -> readerMonad.flatMap(readerMonad.lift(idMonad.pure("World")),
                b -> readerMonad.flatMap(readerMonad.ask(),
                r -> readerMonad.flatMap(readerMonad.asks(e -> "!".repeat(e) + "\n"),
                excl -> readerMonad.pure((a + b + excl).repeat(r))
        ))));

        final var delegate = ReaderTKind.narrow(kind).getDelegate();
        final var idK = delegate.apply(2);
        final var narrow = Id.narrow(idK);
        System.out.println(narrow.getValue());
    }

    private static void eaderTrans() {
        final var idMonad = Id.Instance.INSTANCE;
        final var readerMonadT = new ReaderTKind.ReaderTMonad<Integer, Id.mu>(idMonad);
        final var start = new ReaderTKind<Integer, Id.mu, String>(r -> idMonad.pure("Hello, ".repeat(r)));

        final var muIntegerApp = readerMonadT.flatMap(readerMonadT.local(e -> e * 2, start),
                a -> readerMonadT.flatMap(readerMonadT.lift(idMonad.pure("World!")),
                b -> readerMonadT.flatMap(readerMonadT.ask(),
                r -> readerMonadT.flatMap(readerMonadT.asks(e -> "!".repeat(e) + "\n"),
                excl -> readerMonadT.pure((a + b + excl).repeat(r))
        ))));
        final var delegate = ReaderTKind.narrow(muIntegerApp).getDelegate();
        final var idK = delegate.apply(2);
        final var narrow = Id.narrow(idK);
        System.out.println(narrow.getValue());
    }
}

class ListKind<T> implements App<ListKind.mu, T> {
    private final List<T> delegate;

    ListKind(List<T> delegate) { this.delegate = delegate; }

    public List<T> getDelegate() { return delegate; }

    public static <T> ListKind<T> narrow(App<ListKind.mu, T> kind) { return (ListKind<T>) kind; }

    public static final class mu implements Monad.mu { }

    public static class ListMonad implements Monad<ListKind.mu> {

        @Override
        public <A> App<ListKind.mu, A> pure(A a) {
            return new ListKind<>(List.of(a));
        }

        @Override
        public <A, B> App<ListKind.mu, B> flatMap(App<ListKind.mu, A> ma, Function<A, App<ListKind.mu, B>> aToMb) {
            final var mas = narrow(ma).getDelegate();
            final var bs = mas.stream()
                    .map(aToMb)
                    .map(ListKind::narrow)
                    .map(ListKind::getDelegate)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            return new ListKind<>(bs);
        }
    }
}
