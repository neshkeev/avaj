package org.sample.typeclass;

import java.util.function.Function;

interface Reader<R, A> extends Function<R, A> {
}

public interface ReaderT<WITNESS, R, A, K extends Kind<WITNESS, A>> extends Function<R, K> {

    static<WITNESS, R, A, K extends Kind<WITNESS, A>> ReaderT<WITNESS, R, A, K> reader(
            final Function<? super R, ? extends A> f,
            final Function<? super A, ? extends K> pure
    ) {
        return f.andThen(pure)::apply;
    }
}

class ReaderTKind<WITNESS, R, A, K extends Kind<WITNESS, A>> implements Kind<ReaderTKind.mu<WITNESS, R, A>, K> {

    private final ReaderT<WITNESS, R, A, K> delegate;

    ReaderTKind(ReaderT<WITNESS, R, A, K> delegate) { this.delegate = delegate; }

    public ReaderT<WITNESS, R, A, K> getDelegate() { return delegate; }

    public static final class mu<WITNESS, R, A> { }

    public static final class Instance<WITNESS, R, A> implements Monad<ReaderTKind.mu<WITNESS, R, A>> {
        private final Monad<WITNESS> internalMonad;

        public Instance(Monad<WITNESS> internalMonad) {
            this.internalMonad = internalMonad;
        }

        @Override
        public <T> Kind<ReaderTKind.mu<WITNESS, R, A>, T> pure(T a) {
            Function<R, Kind<WITNESS, T>> frt = r -> internalMonad.pure(a);
            ReaderT<WITNESS, R, T, Kind<WITNESS, T>> c = frt::apply;

            final ReaderTKind<WITNESS, R, T, Kind<WITNESS, T>> cand = new ReaderTKind<WITNESS, R, T, Kind<WITNESS, T>>(c);
            Kind<ReaderTKind.mu<WITNESS, R, T>, Kind<WITNESS, T>> res = cand;

            return res;
        }
    }
}
