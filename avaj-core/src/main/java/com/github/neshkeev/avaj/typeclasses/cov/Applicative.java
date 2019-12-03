package com.github.neshkeev.avaj.typeclasses.cov;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Functions;
import com.github.neshkeev.avaj.Unit;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Applicative<F extends Applicative.mu> extends Functor<F> {

    @NotNull
    <A> App<? extends F, A> pure(@NotNull final A a);

    @NotNull
    <A, B> Function<? super @NotNull App<? extends F, A>, ? extends @NotNull App<? extends F, B>> ap(
            @NotNull final App<? extends F, Function<? super @NotNull A, ? extends @NotNull B>> hfab
    );

    // liftA2 :: (a -> b -> c) -> f a -> f b -> f c
    @NotNull
    default <A, B, C> Function<? super @NotNull App<? extends F, A>, Function<? super @NotNull App<? extends F, B>, ? extends @NotNull App<? extends F, C>>> liftA2(
            @NotNull final BiFunction<A, B, C> fun
    ) {
        final Function<? super @NotNull A, Function<? super @NotNull B, ? extends @NotNull C>> curriedFun = a -> b -> fun.apply(a, b);
        final Function<? super @NotNull App<? extends F, A>, ? extends @NotNull App<? extends F, Function<? super @NotNull B, ? extends @NotNull C>>> faToFbc = map(curriedFun);
        return fa -> this.ap(faToFbc.apply(fa));
    }

    // (<*) :: f a -> f b -> f a
    @NotNull
    default <A, B> App<? extends F, A> discardRight(
            @NotNull final App<? extends F, A> fa,
            @NotNull final Supplier<App<? extends F, B>> fb
    ) {
        return liftA2(Functions.<A, B>constFunction()).apply(fa).apply(fb.get());
    }

    // (*>) :: f a -> f b -> f b
    @NotNull
    default <A, B> App<? extends F, B> then(
            @NotNull final App<? extends F, A> fa,
            @NotNull final App<? extends F, B> fb
    ) {
        return ap(this.<Function<? super B, ? extends B>, A>constMap(Function.identity()).apply(fa)).apply(fb);
    }

    default <A> App<? extends F, Unit> replicate_(final long count, @NotNull final App<? extends F, A> fa) {
        if (count == 0) return pure(Unit.UNIT);
        final var curry = Functions.<App<? extends F, A>, App<? extends F, Unit>, App<? extends F, Unit>>curry(this::then);

        return curry.apply(fa).apply(replicate_(count - 1, fa));
    }

    interface mu extends Functor.mu { }
}
