package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.App;
import org.jetbrains.annotations.NotNull;

public interface MonadTrans<T extends Monad.mu, INTERNAL extends Monad.mu> {

    @NotNull
    <A> App<? extends Monad.mu, A> lift(@NotNull final App<INTERNAL, A> m);
}
