package com.github.neshkeev.avaj.data.kinds;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public class OptionalKind<T extends @NotNull Object> implements App<OptionalKind.@NotNull mu, T> {

    private final Optional<T> delegate;

    public OptionalKind(final Optional<T> delegate) {
        this.delegate = delegate;
    }

    public final Optional<T> getDelegate() { return delegate; }

    @NotNull
    public static <T extends @NotNull Object> OptionalKind<T> narrow(@NotNull final App<@NotNull mu, T> kind) {
        return (OptionalKind<T>) kind;
    }

    public static final class mu implements Monad.mu { }

    public enum Instance implements Monad<@NotNull mu> {
        INSTANCE;

        @Override
        public @NotNull <A extends @NotNull Object> App<OptionalKind.@NotNull mu, A> pure(A a) {
            return new OptionalKind<>(Optional.of(a));
        }

        @Override
        public <A extends @NotNull Object, B extends @NotNull Object> @NotNull App<OptionalKind.@NotNull mu, B> flatMap(
                @NotNull final App<OptionalKind.@NotNull mu, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<OptionalKind.@NotNull mu, B>> aToMb
        ) {
            final var toB = aToMb.andThen(OptionalKind::narrow).andThen(OptionalKind::getDelegate);
            final var optionalB = narrow(ma).getDelegate().flatMap(toB);
            return new OptionalKind<>(optionalB);
        }
    }
}
