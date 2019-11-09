package com.github.neshkeev.avaj.data;

import java.util.function.Function;

public interface Cont<R, A> extends Function<Function< A,  R>, R> {
}
