package com.github.neshkeev.avaj.typeclasses.cov;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Functions;
import com.github.neshkeev.avaj.K1;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface Functor<F extends Functor.mu> {

    @NotNull
    <A, B> Function<? super @NotNull App<? extends F, A>, ? extends @NotNull App<? extends F, B>> map(
            @NotNull final Function<? super @NotNull A, ? extends @NotNull B> fab
    );

    default <A, B> Function<? super App<? extends F, B>, ? extends App<? extends F, A>> constMap(@NotNull final A a) {
        final var alwaysA = Functions.<A, B>constFunction(a);

        return map(alwaysA);
    }

    interface mu extends K1 { }
}
