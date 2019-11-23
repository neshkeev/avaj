package com.github.neshkeev.avaj.typeclasses.cov;

import com.github.neshkeev.avaj.App;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface Applicative<F extends Applicative.mu> extends Functor<F> {

    @NotNull
    <A> App<? extends F, A> pure(@NotNull final A a);

    @NotNull
    <A, B> Function<? super @NotNull App<? extends F, A>, ? extends @NotNull App<? extends F, B>> ap(
            @NotNull final App<? extends F, Function<? super @NotNull A, ? extends @NotNull B>> hfab
    );

    interface mu extends Functor.mu { }
}
