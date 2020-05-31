package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

// * -> (* -> *) -> * -> *
// r -> m a
public interface ReaderT<
        R extends @NotNull Object,
        M extends @NotNull Object & Monad.mu,
        A extends @NotNull Object
    > extends Function<
        R,
        @NotNull App<M, A>> { }
