package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.K1;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface Functor<F extends Functor.mu> {
    @NotNull
    <A, B> Function<? super @NotNull App<F, A>, ? extends @NotNull App<F, B>> map(
            @NotNull final Function<? super @NotNull A, ? extends @NotNull B> f
    );

    default <A, B> Function<App<F, B>, App<F, A>> constMap(@NotNull final A a) {
        final Function<A, Function<B, A>> cnst = v -> b -> v;
        final var alwaysA = cnst.apply(a);
        final var map = map(alwaysA);

        return map::apply;
    }

    interface mu extends K1 { }
}
