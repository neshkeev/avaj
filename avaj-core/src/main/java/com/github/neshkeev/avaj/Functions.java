package com.github.neshkeev.avaj;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Functions {
    private Functions() { }

    @NotNull
    public static <A, B, R> Function<? super A, ? extends Function<? super B, ? extends R>> curry(BiFunction<A, B, R> func) {
        return a -> b -> func.apply(a, b);
    }

    @NotNull
    public static <A, B> BiFunction<A, B, A> constFunction() {
        return (a, b) -> a;
    }

    @NotNull
    public static <A, B> Function<? super B, ? extends A> constFunction(@NotNull final A a) {
        return Functions.<A, B, A>curry(constFunction()).apply(a);
    }

    @NotNull
    public static <A, B, C> BiFunction<B, A, C> flip(@NotNull final BiFunction<A, B, C> from) {
        return (b, a) -> from.apply(a, b);
    }

    @NotNull
    public static<A> A alter(@NotNull final A entity, @NotNull final Consumer<A> effect) {
        effect.accept(entity);
        return entity;
    }

}
