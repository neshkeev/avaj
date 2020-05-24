package com.github.neshkeev.avaj.data.kinds;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.data.Cont;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ContKind<R extends @NotNull Object, A extends @NotNull Object> implements App<ContKind.@NotNull mu<R>, A> {
    private final Cont<R, A> delegate;

    public ContKind(final Cont<R, A> delegate) {
        this.delegate = delegate;
    }

    public final Cont<R, A> getDelegate() {
        return delegate;
    }

    @NotNull
    public static <R extends @NotNull Object, A extends @NotNull Object> ContKind<R, A> narrow(
            @NotNull final App<@NotNull mu<R>, A> kind
    ) {
        return (ContKind<R, A>) kind;
    }

    public static class mu<R extends @NotNull Object> implements Monad.mu { }

    public static final class Instance<R extends @NotNull Object> implements Monad<ContKind.@NotNull mu<R>> {
        @Override
        public <A extends @NotNull Object> @NotNull App<ContKind.@NotNull mu<R>, A> pure(final A a) {
            final Cont<R, A> cont = c -> c.apply(a);
            return new ContKind<>(cont);
        }

        @Override
        public <A extends @NotNull Object, B extends @NotNull Object>
        @NotNull App<ContKind.@NotNull mu<R>, B> flatMap(
                @NotNull final App<ContKind.@NotNull mu<R>, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<ContKind.@NotNull mu<R>, B>> aToMb
        ) {
            final Cont<R, B> result = brr -> {
                final Function<A, R> far = a -> {
                    final var kcbr = ContKind.narrow(aToMb.apply(a));
                    final var cbr = kcbr.getDelegate();
                    return cbr.apply(brr);
                };
                return ContKind.narrow(ma).getDelegate().apply(far);
            };
            return new ContKind<>(result);
        }

        // ((a -> r b) -> r a) -> r a
        public static <A extends @NotNull Object, B extends @NotNull Object, R extends @NotNull Object>
        @NotNull ContKind<R, A> callCC(
                @NotNull final Function<
                        ? super @NotNull Function<? super A, ? extends @NotNull ContKind<R, B>>,
                        ? extends @NotNull ContKind<R, A>
                        > aToRbToRa
        ) {
            return new ContKind<>(
                    ar -> {
                        final Function<? super A, ? extends @NotNull ContKind<R, B>> fabr = a -> new ContKind<>(br -> ar.apply(a));
                        return aToRbToRa.apply(fabr).getDelegate().apply(ar);
                    }
            );
        }

    }
}
