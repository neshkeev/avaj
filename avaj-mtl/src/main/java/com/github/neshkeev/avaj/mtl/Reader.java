package com.github.neshkeev.avaj.mtl;

import org.jetbrains.annotations.NotNull;

// r -> a
public interface Reader<R extends @NotNull Object, A extends @NotNull Object> extends ReaderT<R, Id.@NotNull mu, A> { }
