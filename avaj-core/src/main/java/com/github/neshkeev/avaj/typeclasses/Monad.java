package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.github.neshkeev.avaj.Unit.UNIT;

public interface Monad<M extends @NotNull Object & Monad.mu> extends Applicative<M> {

    @Contract(value = "_, _ -> !null", pure = true)
    default <A extends @NotNull Object, B extends @NotNull Object, C extends @NotNull Object>
    @NotNull Function<? super A, ? extends @NotNull App<M, C>> andThen(
            @NotNull final Function<? super A, ? extends @NotNull App<M, B>> aToMb,
            @NotNull final Function<? super B, ? extends @NotNull App<M, C>> bToMc
    ) {
        return a -> flatMap(aToMb.apply(a), bToMc);
    }

    @Contract(value = "_ -> !null", pure = true)
    default <A extends @NotNull Object>
    @NotNull App<M, A> join(@NotNull final App<M, @NotNull App<M, A>> mma) {
        return flatMap(mma, Function.identity());
    }

    @Contract(value = "_, _ -> !null", pure = true)
    default <A extends @NotNull Object, B extends @NotNull Object>
    @NotNull App<M, B> flatMap(
            @NotNull final App<M, A> ma,
            @NotNull final Function<? super A, ? extends @NotNull App<M, B>> aToMb
    ) {
        @NotNull final Function<
                ? super @NotNull App<M, A>,
                ? extends @NotNull App<M, @NotNull App<M, B>>> map = map(aToMb);
        return join(map.apply(ma));
    }

    @Contract(value = "_, _ -> !null", pure = true)
    default <A extends @NotNull Object, B extends @NotNull Object>
    @NotNull App<M, B> followBy(@NotNull final App<M, A> ma, @NotNull final App<M, B> mb) {
        return flatMap(ma, a -> mb);
    }

    @Override
    @Contract(value = "_ -> !null", pure = true)
    default <A extends @NotNull Object, B extends @NotNull Object>
    @NotNull Function<? super @NotNull App<M, A>, ? extends @NotNull App<M, B>> map(
            final @NotNull Function<? super A, ? extends B> f
    ) {
        return maK -> this.flatMap(maK, a -> this.pure(f.apply(a)));
    }

    @Override
    @Contract(value = "_ -> !null", pure = true)
    default <A extends @NotNull Object, B extends @NotNull Object>
    @NotNull Function<? super @NotNull App<M, A>, ? extends @NotNull App<M, B>> ap(
        final @NotNull App<M, @NotNull Function<? super @NotNull A, ? extends @NotNull B>> hfn
    ) {
        return maK ->
                this.flatMap(maK,
                        a -> this.flatMap(hfn,
                        fn -> this.pure(fn.apply(a))
                ));
    }

    @Contract(value = "_, _ -> !null", pure = true)
    default <A extends @NotNull Object>
    @NotNull App<M, @NotNull Unit> replicateM_(final int cnt, @NotNull final App<M, A> m) {
        if (cnt <= 0) return pure(UNIT);
        return flatMap(m, __ -> replicateM_(cnt - 1, m));
    }

    interface mu extends Applicative.mu { }
}
