package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;

import java.util.function.Function;

// * -> (* -> *) -> * -> *
public interface ContT<R, M extends Monad.mu, A> extends Function<Function<A, App<M, R>>, App<M, R>> { }
