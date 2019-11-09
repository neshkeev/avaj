package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.App;
import org.jetbrains.annotations.NotNull;

public interface MonadTrans<T extends Monad.mu> {

    @NotNull
    <M extends Monad.mu, A> App<? extends Monad.mu, A> lift(@NotNull final App<M, A> m);
}
