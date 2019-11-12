package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.App;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface Monad<M extends Monad.mu> extends Applicative<M> {

    @NotNull
    default <A, B, C> Function<@NotNull A, ? extends @NotNull App<M, C>> andThen(
            @NotNull final Function<@NotNull A, ? extends @NotNull App<M, B>> aToMb,
            @NotNull final Function<@NotNull B, ? extends @NotNull App<M, C>> bToMc
    ) {
        return a -> flatMap(aToMb.apply(a), bToMc);
    }

    default <A> App<M, A> join(@NotNull final App<M, ? extends App<M, A>> mma) {
        return flatMap(mma, Function.identity());
    }

    @NotNull
    default <A, B> App<M, B> flatMap(
            @NotNull final App<M, A> ma,
            @NotNull final Function<@NotNull A, ? extends @NotNull App<M, B>> aToMb
    ) {
        return join(map(aToMb).apply(ma));
    }

    @NotNull
    default <A, B> App<M, B> followBy(@NotNull final App<M, A> ma, @NotNull final App<M, B> mb) {
        return flatMap(ma, a -> mb);
    }

    @NotNull
    @Override
    default <A, B> Function<@NotNull App<M, A>, ? extends @NotNull App<M, B>> map(
            @NotNull final Function<? super @NotNull A, ? extends @NotNull B> f
    ) {
        return maK -> this.flatMap(maK, a -> this.pure(f.apply(a)));
    }

    @Override
    default <A, B> Function<? super @NotNull App<M, A>, ? extends @NotNull App<M, B>> ap(
        final @NotNull App<M, Function<? super @NotNull A, ? extends @NotNull B>> hfn
    ) {
        return maK ->
                this.flatMap(maK,
                        a -> this.flatMap(hfn,
                        fn -> this.pure(fn.apply(a))
                ));
    }

    interface mu extends Applicative.mu { }
}
