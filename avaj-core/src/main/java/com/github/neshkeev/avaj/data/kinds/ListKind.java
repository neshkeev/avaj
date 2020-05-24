package com.github.neshkeev.avaj.data.kinds;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ListKind<T extends @NotNull Object> implements App<ListKind.@NotNull mu, T> {
    private final List<T> delegate;

    public ListKind(@NotNull final List<T> delegate) { this.delegate = delegate; }

    @NotNull
    public List<T> getDelegate() { return delegate; }

    @NotNull
    public static <T extends @NotNull Object> ListKind<T> narrow(@NotNull final App<ListKind.@NotNull mu, T> kind) {
        return (ListKind<T>) kind;
    }

    public static final class mu implements Monad.mu { }

    public static class ListMonad implements Monad<ListKind.@NotNull mu> {

        @Override
        public <A extends @NotNull Object> @NotNull App<ListKind.@NotNull mu, A> pure(A a) {
            return new ListKind<>(List.of(a));
        }

        @Override
        public <A extends @NotNull Object, B extends @NotNull Object>
        @NotNull App<ListKind.@NotNull mu, B> flatMap(
                @NotNull final App<ListKind.@NotNull mu, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<ListKind.@NotNull mu, B>> aToMb
        ) {
            final var mas = narrow(ma).getDelegate();
            final var bs = mas.stream()
                    .map(aToMb)
                    .map(ListKind::narrow)
                    .map(ListKind::getDelegate)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());

            return new ListKind<>(bs);
        }
    }
}
