package com.github.neshkeev.avaj.data.kinds;


import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public class OptionalKind<T> implements App<OptionalKind.mu, T> {

    private final Optional<T> delegate;

    public OptionalKind(final Optional<T> delegate) {
        this.delegate = delegate;
    }

    public final Optional<T> getDelegate() {
        return delegate;
    }
    public static <T> OptionalKind<T> narrow(App<mu, T> kind) {
        return (OptionalKind<T>) kind;
    }

    public static final class mu implements Monad.mu { }

    public enum Instance implements Monad<mu> {
        INSTANCE;

        @NotNull
        @Override
        public <A> App<OptionalKind.mu, A> pure(@NotNull final A a) {
            return new OptionalKind<>(Optional.of(a));
        }

        @NotNull
        @Override
        public <A, B> App<OptionalKind.mu, B> flatMap(
                @NotNull final App<OptionalKind.mu, A> ma,
                @NotNull final Function<@NotNull A, ? extends @NotNull App<OptionalKind.mu, B>> aToMb
        ) {
            final var toB = aToMb.andThen(OptionalKind::narrow).andThen(OptionalKind::getDelegate);

            final var optionalB = narrow(ma).getDelegate().flatMap(toB);

            return new OptionalKind<>(optionalB);
        }
    }
}
