package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.App;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface Monad<M extends @NotNull Object & Monad.mu> extends Applicative<M> {

    default <A extends @NotNull Object, B extends @NotNull Object, C extends @NotNull Object>
    @NotNull Function<? super A, ? extends @NotNull App<M, C>> andThen(
            @NotNull final Function<? super A, ? extends @NotNull App<M, B>> aToMb,
            @NotNull final Function<? super B, ? extends @NotNull App<M, C>> bToMc
    ) {
        return a -> flatMap(aToMb.apply(a), bToMc);
    }

    default <A extends @NotNull Object>
    @NotNull App<M, A> join(@NotNull final App<M, @NotNull App<M, A>> mma) {
        return flatMap(mma, Function.identity());
    }

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

    default <A extends @NotNull Object, B extends @NotNull Object>
    @NotNull App<M, B> followBy(@NotNull final App<M, A> ma, @NotNull final App<M, B> mb) {
        return flatMap(ma, a -> mb);
    }

    @Override
    default <A extends @NotNull Object, B extends @NotNull Object>
    @NotNull Function<? super @NotNull App<M, A>, ? extends @NotNull App<M, B>> map(
            final @NotNull Function<? super A, ? extends B> f
    ) {
        return maK -> this.flatMap(maK, a -> this.pure(f.apply(a)));
    }

    @Override
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

    interface mu extends Applicative.mu { }
}
