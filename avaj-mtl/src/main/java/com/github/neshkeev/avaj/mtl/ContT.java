package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.cov.Monad;

import java.util.function.Function;

// * -> (* -> *) -> * -> *
// a -> m r
@FunctionalInterface
public interface ContT<R, M extends Monad.mu, A> extends Function<Function<? super A, ? extends App<? extends M, R>>, App<? extends M, R>> { }
