package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;

import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class Id<A extends @NotNull Object> implements App<Id.@NotNull mu, A> {
    private final A value;

    public Id(@NotNull final A value) {
        this.value = value;
    }

    @NotNull
    public final A getValue() {
        return value;
    }

    @NotNull
    public static <A extends @NotNull Object> Id<A> narrow(@NotNull final App<Id.@NotNull mu, A> kind) {
        return (Id<A>) kind;
    }

    public static final class mu implements Monad.mu { }

    @Override
    public final String toString() {
        return value.toString();
    }

    public enum IdMonad implements Monad<@NotNull mu> {
        INSTANCE;

        @NotNull
        @Override
        public <A extends @NotNull Object> App<Id.@NotNull mu, A> pure(@NotNull final A a) {
            return new Id<>(a);
        }

        @Override
        public <A extends @NotNull Object, B extends @NotNull Object> @NotNull App<Id.@NotNull mu, B> flatMap(
                @NotNull final App<Id.@NotNull mu, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<Id.@NotNull mu, B>> aToMb
        ) {
            return aToMb.apply(narrow(ma).value);
        }
    }
}
