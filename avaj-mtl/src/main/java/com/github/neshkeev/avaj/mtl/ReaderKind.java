package com.github.neshkeev.avaj.mtl;

import org.jetbrains.annotations.NotNull;

public final class ReaderKind<R extends @NotNull Object, A extends @NotNull Object> extends ReaderTKind<R, Id.@NotNull mu, A> {

    public ReaderKind(@NotNull final Reader<R, A> delegate) {
        super(delegate);
    }

    public static final class ReaderMonad<R extends @NotNull Object> extends ReaderTMonad<R, Id.@NotNull mu> {
        public ReaderMonad() {
            super(Id.IdMonad.INSTANCE);
        }
    }
}
