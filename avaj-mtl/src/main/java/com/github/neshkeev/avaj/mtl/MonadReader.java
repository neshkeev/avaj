package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface MonadReader<
        R extends @NotNull Object,
        M extends @NotNull Object & Monad.mu
    > extends Monad<M> {
    // ask :: m r
    @NotNull
    @Contract(value = "-> !null", pure = true)
    App<M, R> ask();

    // asks :: (r -> a) -> m a
    @NotNull
    @Contract(value = "_ -> !null", pure = true)
    <A extends @NotNull Object> App<M, A> asks(@NotNull final Function<? super R, ? extends A> fn);

    // local :: (r -> r) -> m a -> m a
    @NotNull
    @Contract(value = "_, _ -> !null", pure = true)
    <A extends @NotNull Object> App<M, A> local(
            @NotNull final Function<? super R, ? extends R> fn,
            @NotNull final App<M, A> from
    );
}

