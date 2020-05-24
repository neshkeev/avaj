package com.github.neshkeev.avaj.data;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface Cont<R extends @NotNull Object, A extends @NotNull Object>
        extends Function<@NotNull Function<A, R>, @NotNull R> { }
