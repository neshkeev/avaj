package com.github.neshkeev.avaj.typeclasses;

import com.github.neshkeev.avaj.App;
import org.jetbrains.annotations.NotNull;

public interface MonadTrans<T extends @NotNull Object & Monad.mu, INTERNAL extends @NotNull Object & Monad.mu> {

    <A extends @NotNull Object>
    @NotNull App<T, A> lift(@NotNull final App<INTERNAL, A> m);
}
