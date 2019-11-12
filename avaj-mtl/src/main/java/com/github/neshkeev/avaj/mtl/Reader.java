package com.github.neshkeev.avaj.mtl;

import java.util.function.Function;

public class Reader<R, A> extends ReaderTKind<R, Id.mu, A> {
    public Reader(final Function<R, A> delegate) {
        super(r -> Id.IdMonad.INSTANCE.pure(delegate.apply(r)));
    }

    public static final class ReaderMonad<R> extends ReaderTMonad<R, Id.mu> {
        public ReaderMonad() {
            super(Id.IdMonad.INSTANCE);
        }
    }
}
