package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Functions;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Applicative<F extends Applicative.mu> extends Functor<F> {

    @NotNull
    <A> App<F, A> pure(@NotNull final A a);

    @NotNull
    <A, B> Function<? super @NotNull App<F, A>, ? extends @NotNull App<F, B>> ap(
            @NotNull final App<F, Function<? super @NotNull A, ? extends @NotNull B>> f
    );

    // liftA2 :: (a -> b -> c) -> f a -> f b -> f c
    @NotNull
    default <A, B, C> Function<? super @NotNull App<F, A>, Function<? super @NotNull App<F, B>, ? extends @NotNull App<F, C>>> liftA2(
            @NotNull final BiFunction<A, B, C> fun
    ) {
        final Function<? super @NotNull A, Function<? super @NotNull B, ? extends @NotNull C>> curriedFun = a -> b -> fun.apply(a, b);
        final Function<? super @NotNull App<F, A>, ? extends @NotNull App<F, Function<? super @NotNull B, ? extends @NotNull C>>> faToFbc = map(curriedFun);
        return fa -> this.ap(faToFbc.apply(fa));
    }

    // (<*) :: f a -> f b -> f a
    @NotNull
    default <A, B> App<F, A> discardRight(
            @NotNull final App<F, A> fa,
            @NotNull final Supplier<App<F, B>> fb
    ) {
        return liftA2(Functions.<A, B>constFunction()).apply(fa).apply(fb.get());
    }

    @NotNull
    default <A, B> Function<? super @NotNull Supplier<App<F, B>>, ? extends @NotNull App<F, A>> discardRight(
            @NotNull final App<F, A> fa
    ) {
        return Functions.<App<F, A>, Supplier<App<F, B>>, App<F, A>>curry(this::discardRight).apply(fa);
    }

    interface mu extends Functor.mu { }
}
