package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface MonadWriter<
        W extends @NotNull Object,
        M extends @NotNull Object & Monad.mu
    > extends Monad<M> {

    @NotNull
    @Contract(value = "_ -> !null", pure = true)
    App<M, @NotNull Unit> tell(final W a);

}
