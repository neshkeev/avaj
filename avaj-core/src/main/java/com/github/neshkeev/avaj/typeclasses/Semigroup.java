package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.K1;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Semigroup<A extends @NotNull Object> {

    // concat :: m -> m -> m
    @Contract(pure = true)
    A concat(final A left, final A right);

    interface mu extends K1 {}
}
