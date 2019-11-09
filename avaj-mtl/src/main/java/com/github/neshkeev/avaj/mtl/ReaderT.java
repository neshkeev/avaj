package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;

import java.util.function.Function;

public interface ReaderT<R, M extends Monad.mu, A> extends Function<R, App<M, A>> { }
