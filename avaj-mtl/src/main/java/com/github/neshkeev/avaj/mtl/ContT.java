package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

// * -> (* -> *) -> * -> *
// (a -> m r) -> m r
@FunctionalInterface
public interface ContT<
        R extends @NotNull Object,
        M extends @NotNull Object & Monad.mu,
        A extends @NotNull Object
    > extends Function<
        @NotNull Function<
                ? super A,
                ? extends @NotNull App<M, R>
                >,
        @NotNull App<M, R>> { }
