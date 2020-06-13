package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Functions;
import com.github.neshkeev.avaj.Unit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Applicative<F extends @NotNull Object & Applicative.mu> extends Functor<F> {

    @Contract(value = "_ -> !null", pure = true)
    <A extends @NotNull Object> @NotNull App<F, A> pure(final A a);

    @Contract(value = "_ -> !null", pure = true)
    <A extends @NotNull Object, B extends @NotNull Object>
    @NotNull Function<? super @NotNull App<F, A>, ? extends @NotNull App<F, B>> ap(
            @NotNull final App<F, @NotNull Function<? super A, ? extends B>> f
    );

    // liftA2 :: (a -> b -> c) -> f a -> f b -> f c
    @Contract(value = "_ -> !null", pure = true)
    default <A extends @NotNull Object, B extends @NotNull Object, C extends @NotNull Object>
    @NotNull Function<
            ? super @NotNull App<F, A>,
            ? extends @NotNull Function<
                    ? super @NotNull App<F, B>,
                    ? extends @NotNull App<F, C>
                    >
            > liftA2(
            @NotNull final BiFunction<A, B, C> fun
    ) {
        final Function<? super A, ? extends @NotNull Function<? super B, ? extends C>> curriedFun = Functions.curry(fun);
        final Function<? super @NotNull App<F, A>, ? extends @NotNull App<F, @NotNull Function<? super B, ? extends C>>> faToFbc = map(curriedFun);
        return fa -> this.ap(faToFbc.apply(fa));
    }

    // (<*) :: f a -> f b -> f a
    @Contract(value = "_, _ -> !null", pure = true)
    default <A extends @NotNull Object, B extends @NotNull Object>
    @NotNull App<F, A> discardRight(
            @NotNull final App<F, A> fa,
            @NotNull final Supplier<@NotNull App<F, B>> fb
    ) {
        return liftA2(Functions.<A, B>constFunction()).apply(fa).apply(fb.get());
    }

    @Contract(value = "_ -> !null", pure = true)
    default <A extends @NotNull Object, B extends @NotNull Object>
    @NotNull Function<? super @NotNull Supplier<App<F, B>>, ? extends @NotNull App<F, A>> discardRight(
            @NotNull final App<F, A> fa
    ) {
        return Functions.<App<F, A>, Supplier<App<F, B>>, App<F, A>>curry(this::discardRight).apply(fa);
    }

    @Contract(value = "_, _ -> !null", pure = true)
    default @NotNull App<F, @NotNull Unit> when(boolean b, final @NotNull Supplier<@NotNull App<F, @NotNull Unit>> s) {
        return b ? s.get() : pure(Unit.UNIT);
    }

    interface mu extends Functor.mu { }
}
