package com.github.neshkeev.avaj.typeclasses.cov;

import com.github.neshkeev.avaj.App;
import org.jetbrains.annotations.NotNull;

public interface MonadTrans<INTERNAL extends Monad.mu> {

    @NotNull
    <A> App<? extends Monad.mu, A> lift(@NotNull final App<? extends INTERNAL, A> m);
}
