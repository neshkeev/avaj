package com.github.neshkeev.avaj.typeclasses;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface Monoid<A extends @NotNull Object> extends Semigroup<A> {

    @Contract(pure = true) A empty();

    interface mu extends Semigroup.mu {}
}
