package com.github.neshkeev.avaj.mtl;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class Reader<R extends @NotNull Object, A extends @NotNull Object> extends ReaderTKind<R, Id.@NotNull mu, A> {

    public Reader(@NotNull final Function<R, A> delegate) {
        super(r -> Id.IdMonad.INSTANCE.pure(delegate.apply(r)));
    }

    public static final class ReaderMonad<R extends @NotNull Object> extends ReaderTMonad<R, Id.@NotNull mu> {
        public ReaderMonad() {
            super(Id.IdMonad.INSTANCE);
        }
    }
}
