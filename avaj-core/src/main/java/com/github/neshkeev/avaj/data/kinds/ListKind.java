package com.github.neshkeev.avaj.data.kinds;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.data.List;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.Monoid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.github.neshkeev.avaj.data.List.Cons.cons;
import static com.github.neshkeev.avaj.data.List.Nil.nil;

public final class ListKind<T extends @NotNull Object> implements App<ListKind.@NotNull mu, T> {
    @NotNull private final List<T> delegate;

    public ListKind(@NotNull final List<T> delegate) {
        this.delegate = delegate;
    }

    @NotNull
    public ListKind<T> merge(@NotNull final ListKind<T> right) {
        return new ListKind<>(delegate.merge(right.getDelegate()));
    }

    @NotNull public List<T> getDelegate() { return delegate; }
    public static<T extends @NotNull Object> @NotNull ListKind<T> narrow(@NotNull final App<@NotNull mu, T> value) { return (ListKind<T>) value; }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public static class mu implements Monad.mu, Monoid.mu { }

    public enum  ListMonad implements Monad<@NotNull mu> {
        INSTANCE;

        @Override
        @NotNull
        public <A extends @NotNull Object> ListKind<A> pure(A a) {
            return new ListKind<>(cons(a, nil()));
        }

        @Override
        @NotNull
        public <A extends @NotNull Object, B extends @NotNull Object> ListKind<B> flatMap(
                @NotNull final App<ListKind.@NotNull mu, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<ListKind.@NotNull mu, B>> aToMb
        ) {
            final List<A> delegate = narrow(ma).getDelegate();
            if (delegate instanceof List.Nil) return new ListKind<>(nil());

            final A head = delegate.head();
            final List<A> tail = delegate.tail();
            final ListKind<B> newHead = aToMb.andThen(ListKind::narrow).apply(head);
            final ListKind<B> newTail = flatMap(new ListKind<>(tail), aToMb);

            return newHead.merge(newTail);
        }
    }
}
