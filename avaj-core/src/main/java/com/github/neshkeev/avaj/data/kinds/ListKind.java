package com.github.neshkeev.avaj.data.kinds;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ListKind<T> implements App<ListKind.mu, T> {
    private final List<T> delegate;

    public ListKind(List<T> delegate) { this.delegate = delegate; }

    public List<T> getDelegate() { return delegate; }

    public static <T> ListKind<T> narrow(App<ListKind.mu, T> kind) { return (ListKind<T>) kind; }

    public static final class mu implements Monad.mu { }

    public static class ListMonad implements Monad<ListKind.mu> {

        @NotNull
        @Override
        public <A> App<ListKind.mu, A> pure(@NotNull final A a) {
            return new ListKind<>(List.of(a));
        }

        @NotNull
        @Override
        public <A, B> App<ListKind.mu, B> flatMap(
                @NotNull final App<ListKind.mu, A> ma,
                @NotNull Function<@NotNull A, ? extends @NotNull App<ListKind.mu, B>> aToMb
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
