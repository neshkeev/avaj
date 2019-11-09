package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;

import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class Id<A> implements App<Id.mu, A> {
    private final A value;

    public Id(@NotNull final A value) {
        this.value = value;
    }

    @NotNull
    public final A getValue() {
        return value;
    }

    @NotNull
    public static <A> Id<A> narrow(@NotNull final App<Id.mu, A> kind) {
        return (Id<A>) kind;
    }

    public static final class mu implements Monad.mu { }

    @Override
    public final String toString() {
        return value.toString();
    }

    public enum Instance implements Monad<Id.mu> {
        INSTANCE;

        @NotNull
        @Override
        public <A> App<Id.mu, A> pure(@NotNull final A a) {
            return new Id<>(a);
        }

        @NotNull
        @Override
        public <A, B> App<Id.mu, B> flatMap(
                @NotNull final App<Id.mu, A> ma,
                @NotNull final Function<@NotNull A, ? extends @NotNull App<Id.mu, B>> aToMb
        ) {
            final var value = narrow(ma).getValue();
            return aToMb.apply(value);
        }
    }
}
