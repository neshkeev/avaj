package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.cov.Monad;
import com.github.neshkeev.avaj.typeclasses.cov.MonadTrans;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ContTKind<R, M extends Monad.mu, A> implements App<ContTKind.mu<R, M>, A> {

    private final ContT<R, M, A> delegate;

    public ContTKind(@NotNull final ContT<R, M, A> delegate) { this.delegate = delegate; }

    public ContT<R, M, A> getDelegate() { return delegate; }

    @NotNull
    public static<R, M extends Monad.mu, A> ContTKind<R, M, A> narrow(
            @NotNull final App<? extends ContTKind.mu<R, M>, A> kind
    ) {
        return (ContTKind<R, M, A>) kind;
    }

    public interface mu<R, M extends Monad.mu> extends Monad.mu { }

    public static class ContTMonad<R, M extends Monad.mu> implements Monad<mu<R, M>>, MonadTrans<M> {
        private final Monad<M> monad;

        public Monad<M> getMonad() {
            return monad;
        }

        public ContTMonad(Monad<M> monad) {
            this.monad = monad;
        }

        @NotNull
        public<A> App<? extends ContTKind.mu<R, M>, A> ctor(@NotNull final ContT<R, M, A> cont) {
            return new ContTKind<>(cont);
        }

        @Override
        public @NotNull <A> App<? extends ContTKind.mu<R, M>, A> pure(@NotNull A a) {
            final var ma = monad.pure(a);
            return ctor(c -> monad.flatMap(ma, c));
        }

        @Override
        public @NotNull <A, B> App<? extends ContTKind.mu<R, M>, B> flatMap(
                @NotNull final App<? extends ContTKind.mu<R, M>, @NotNull A> ma,
                @NotNull final Function<? super @NotNull A, ? extends @NotNull App<? extends ContTKind.mu<R, M>, B>> aToMb
        ) {
            final ContT<R, M, B> result = brr -> {
                final Function<? super A, App<? extends M, R>> far = a -> {
                    final var kcbr = ContTKind.narrow(aToMb.apply(a));
                    final var cbr = kcbr.getDelegate();
                    return cbr.apply(brr);
                };
                return ContTKind.narrow(ma).getDelegate().apply(far);
            };
            return new ContTKind<>(result);
        }

        @Override
        public @NotNull <A> App<ContTKind.mu<R, M>, A> lift(@NotNull App<? extends M, A> m) {
            return new ContTKind<R, M, A>(famr -> monad.flatMap(m, famr));
        }

        @NotNull
        public static <R, M extends Monad.mu, A, B> ContTKind<R, M, A> callCC(
                @NotNull final Function<
                        @NotNull Function<
                                ? super @NotNull A,
                                ? extends @NotNull ContTKind<R, M, B>>,
                        ? extends @NotNull ContTKind<R, M, A>> aToRbToRa
        ) {
            return new ContTKind<>(
                    ar -> {
                        final Function<? super @NotNull A, ? extends @NotNull ContTKind<R, M, B>> fabr =
                                a -> new ContTKind<>(br -> ar.apply(a));

                        return aToRbToRa.apply(fabr).getDelegate().apply(ar);
                    }
            );
        }

    }
}

