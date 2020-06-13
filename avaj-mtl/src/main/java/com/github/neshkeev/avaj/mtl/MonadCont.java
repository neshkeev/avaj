package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface MonadCont<M extends @NotNull Object & Monad.mu> extends Monad<M> {

    // ((a -> m b) -> m a) -> m a
    @Contract(value = "_ -> !null", pure = true)
    <A extends @NotNull Object, B extends @NotNull Object>
    @NotNull App<M, A> callCC(
            @NotNull final Function<
                    ? super @NotNull Function<
                            ? super A,
                            ? extends @NotNull App<M, B>
                            >,
                    ? extends @NotNull App<M, A>
                    > aToMbToMa
    );

}
