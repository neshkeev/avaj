package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.App;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface Applicative<F extends Applicative.mu> extends Functor<F> {

    @NotNull
    <A> App<F, A> pure(@NotNull final A a);

    <A, B> Function<? super @NotNull App<F, A>, ? extends @NotNull App<F, B>> ap(
            @NotNull final App<F, Function<? super @NotNull A, @NotNull B>> f
    );

    interface mu extends Functor.mu { }
}
