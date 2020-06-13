package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.K1;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface Functor<F extends @NotNull Object & Functor.mu> {

    @Contract(value = "_ -> !null", pure = true)
    @NotNull
    <A extends @NotNull Object, B extends @NotNull Object>
    Function<? super @NotNull App<F, A>, ? extends @NotNull App<F, B>> map(
            @NotNull final Function<? super A, ? extends B> f
    );

    @Contract(value = "_ -> !null", pure = true)
    @NotNull
    default <A extends @NotNull Object, B extends @NotNull Object>
    Function<? super @NotNull App<F, B>, ? extends @NotNull App<F, A>> constMap(final A a) {
        final Function<? super A, ? extends Function<B, A>> cnst = v -> b -> v;
        final var alwaysA = cnst.apply(a);

        return map(alwaysA);
    }

    interface mu extends K1 { }
}
