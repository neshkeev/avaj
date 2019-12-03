package com.github.neshkeev.avaj.typeclasses.cov;

import com.github.neshkeev.avaj.App;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Monad<M extends Monad.mu> extends Applicative<M> {

    @NotNull
    default <A, B, C> Function<? super @NotNull A, ? extends @NotNull App<? extends M, C>> andThen(
            @NotNull final Function<? super @NotNull A, ? extends @NotNull App<? extends M, B>> aToMb,
            @NotNull final Function<? super @NotNull B, ? extends @NotNull App<? extends M, C>> bToMc
    ) {
        return a -> flatMap(aToMb.apply(a), bToMc);
    }

    @NotNull
    <A, B> App<? extends M, B> flatMap(
            @NotNull final App<? extends M, @NotNull A> ma,
            @NotNull final Function<? super @NotNull A, ? extends @NotNull App<? extends M, B>> aToMb
    ) ; /*{
        return this.<B>join(map(aToMb).apply(ma));
    }*/

    @NotNull
    default <A> App<? extends M, A> join(@NotNull final App<? extends M, App<? extends M, A>> mma) {
        return flatMap(mma, Function.identity());
    }

    @NotNull
    default <A, B> App<? extends M, B> chain(
            @NotNull final App<? extends M, A> ma,
            @NotNull final App<? extends M, B> mb
    ) {
        return chain(ma, () -> mb);
    }

    @NotNull
    default <A, B> App<? extends M, B> chain(
            @NotNull final App<? extends M, A> ma,
            @NotNull final Supplier<@NotNull App<? extends M, B>> mb
    ) {
        return flatMap(ma, a -> mb.get());
    }

    @NotNull
    @Override
    default <A, B> Function<? super @NotNull App<? extends M, A>, ? extends @NotNull App<? extends M, B>> map(
            @NotNull final Function<? super @NotNull A, ? extends @NotNull B> fab
    ) {
        return maK -> this.flatMap(maK, a -> this.pure(fab.apply(a)));
    }

    @Override
    default <A, B> Function<? super @NotNull App<? extends M, A>, ? extends @NotNull App<? extends M, B>> ap(
            @NotNull final App<? extends M, Function<? super @NotNull A, ? extends @NotNull B>> hfab
    ) {
        return maK ->
                this.flatMap(maK,
                        a -> this.flatMap(hfab,
                        fn -> this.<B>pure(fn.apply(a))
                ));
    }

    default <A> App<? extends M, A> chainMany(@NotNull final App<? extends M, A> head, @NotNull final App<? extends M, A>... tail) {
        if (tail.length == 0) return head;

        return Arrays.stream(tail).reduce(head, this::chain);
    }

    interface mu extends Applicative.mu { }
}
