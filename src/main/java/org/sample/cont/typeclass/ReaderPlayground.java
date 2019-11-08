package org.sample.cont.typeclass;

public class ReaderPlayground {

    public static void main(String[] args) {
        final var readerMonadT = new ReaderTKind.ReaderMonad<Integer, Id.mu>(Id.Instance.INSTANCE);
        final var muIntegerApp = readerMonadT.flatMap(readerMonadT.pure(5),
                a -> readerMonadT.pure(a + 65));
        final ReaderT<Integer, Id.mu, Integer> delegate = ReaderTKind.narrow(muIntegerApp).getDelegate();
        final App<Id.mu, Integer> idK = delegate.apply(0);
        final Id<Integer> narrow = Id.narrow(idK);
        System.out.println(narrow.getValue());
    }
}
