package com.github.neshkeev.avaj.data.kinds;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.data.Cont;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ContKind<R, A> implements App<ContKind.mu<R>, A> {
    private final Cont<R, A> delegate;

    public ContKind(final Cont<R, A> delegate) {
        this.delegate = delegate;
    }

    public final Cont<R, A> getDelegate() {
        return delegate;
    }

    public static <R, A> ContKind<R, A> narrow(App<mu<R>, A> kind) {
        return (ContKind<R, A>) kind;
    }

    public static class mu<R> implements Monad.mu { }

    public static final class Instance<R> implements Monad<ContKind.mu<R>> {
        @NotNull
        @Override
        public <A> App<ContKind.mu<R>, A> pure(@NotNull final A a) {
            final Cont<R, A> cont = c -> c.apply(a);
            return new ContKind<>(cont);
        }

        @NotNull
        @Override
        public <A, B> App<ContKind.mu<R>, B> flatMap(
                @NotNull final App<ContKind.mu<R>, A> ma,
                @NotNull final Function<@NotNull A, ? extends @NotNull App<ContKind.mu<R>, B>> aToMb
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

        public static <A, B, R> ContKind<R, A> callCC(
                Function<Function<A, ContKind<R, B>>, ContKind<R, A>> aToRbToRa
        ) {
            return new ContKind<>(
                    ar -> {
                        final Function<A, ContKind<R, B>> fabr = a -> new ContKind<>(br -> ar.apply(a));
                        return aToRbToRa.apply(fabr).getDelegate().apply(ar);
                    }
            );
        }
    }
}
