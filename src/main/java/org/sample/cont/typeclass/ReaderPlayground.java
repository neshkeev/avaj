package org.sample.cont.typeclass;

public class ReaderPlayground {

    public static void main(String[] args) {
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
