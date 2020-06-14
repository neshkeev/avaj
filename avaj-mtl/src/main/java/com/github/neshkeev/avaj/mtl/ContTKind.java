package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ContTKind<R extends @NotNull Object, M extends @NotNull Object & Monad.mu, A extends @NotNull Object>
        implements App<ContTKind.@NotNull mu<R, M>, A> {

    @NotNull
    private final ContT<R, M, A> delegate;

    public ContTKind(@NotNull final ContT<R, M, A> delegate) { this.delegate = delegate; }

    @NotNull
    public ContT<R, M, A> getDelegate() { return delegate; }

    @NotNull
    public static<R extends @NotNull Object, M extends @NotNull Object & Monad.mu, A extends @NotNull Object> ContTKind<R, M, A> narrow(
            @NotNull final App<ContTKind.@NotNull mu<R, M>, A> kind
    ) {
        return (ContTKind<R, M, A>) kind;
    }

    public interface mu<R extends @NotNull Object, M extends @NotNull Object & Monad.mu> extends Monad.mu { }

    public static class ContTMonad<R extends @NotNull Object, M extends @NotNull Object & Monad.mu>
            implements MonadCont<@NotNull mu<R, M>>, MonadTrans<@NotNull mu<R, M>, M> {

        @NotNull private final Monad<M> monad;

        public ContTMonad(@NotNull final Monad<M> monad) {
            this.monad = monad;
        }

        @Override
        @NotNull
        @Contract(value = "_ -> !null", pure = true)
        public <A extends @NotNull Object, B extends @NotNull Object> ContTKind<R, M, A> callCC(
                @NotNull final Function<
                        ? super @NotNull Function<
                                ? super A,
                                ? extends @NotNull App<ContTKind.@NotNull mu<R, M>, B>>,
                        ? extends @NotNull App<ContTKind.@NotNull mu<R, M>, A>> aToMbToMa) {
            return new ContTKind<>(
                    ar -> {
                        final Function<? super @NotNull A, ? extends @NotNull App<ContTKind.@NotNull mu<R, M>, B>> fabr =
                                a -> new ContTKind<>(br -> ar.apply(a));

                        return narrow(aToMbToMa.apply(fabr)).getDelegate().apply(ar);
                    }
            );
        }

        @Override
        public @NotNull <A extends @NotNull Object> ContTKind<R, M, A> pure(final A a) {
            @NotNull final App<M, A> ma = monad.pure(a);
            return new ContTKind<>(c -> monad.flatMap(ma, c));
        }

        @Override
        public <A extends @NotNull Object, B extends @NotNull Object> @NotNull ContTKind<R, M, B> flatMap(
                @NotNull final App<ContTKind.@NotNull mu<R, M>, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<ContTKind.@NotNull mu<R, M>, B>> aToMb
        ) {
            final ContT<R, M, B> result = brr -> {
                final Function<? super A, ? extends App<M, R>> far = a -> {
                    final ContTKind<R, M, B> kcbr = narrow(aToMb.apply(a));
                    final ContT<R, M, B> cbr = kcbr.getDelegate();
                    return cbr.apply(brr);
                };
                return narrow(ma).getDelegate().apply(far);
            };
            return new ContTKind<>(result);
        }

        @Override
        public <A extends @NotNull Object> @NotNull ContTKind<R, M, A> lift(@NotNull final App<M, A> m) {
            return new ContTKind<>(famr -> monad.flatMap(m, famr));
        }
    }
}

