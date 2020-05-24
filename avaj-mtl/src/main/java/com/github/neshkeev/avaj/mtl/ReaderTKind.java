package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ReaderTKind<R extends @NotNull Object, M extends @NotNull Object & Monad.mu, A extends @NotNull Object>
        implements App<ReaderTKind.@NotNull mu<R, M>, A> {

    private final ReaderT<R, M, A> delegate;

    public ReaderTKind(@NotNull final ReaderT<R, M, A> delegate) { this.delegate = delegate; }

    @NotNull
    public ReaderT<R, M, A> getDelegate() { return delegate; }

    @NotNull
    public static<
            R extends @NotNull Object,
            M extends @NotNull Object & Monad.mu,
            A extends @NotNull Object
        > ReaderTKind<R, M, A> narrow(
                @NotNull final App<ReaderTKind.@NotNull mu<R, M>, A> kind
    ) {
        return (ReaderTKind<R, M, A>) kind;
    }

    public static final class mu<R extends @NotNull Object, M extends @NotNull Object & Monad.mu> implements Monad.mu {}

    public static class ReaderTMonad<R extends @NotNull Object, M extends @NotNull Object & Monad.mu>
            implements Monad<@NotNull mu<R, M>>, MonadTrans<@NotNull mu<R, M>, M> {

        private final Monad<M> monad;

        public ReaderTMonad(Monad<M> monad) {
            this.monad = monad;
        }

        @NotNull
        @Override
        public <A extends @NotNull Object> App<ReaderTKind.@NotNull mu<R, M>, A> pure(@NotNull final A a) {
            final App<M, A> pure = monad.pure(a);
            final ReaderT<R, M, A> reader = r -> pure;
            return new ReaderTKind<>(reader);
        }

        @Override
        public <A extends @NotNull Object, B extends @NotNull Object> @NotNull App<ReaderTKind.@NotNull mu<R, M>, B> flatMap(
                @NotNull final App<ReaderTKind.@NotNull mu<R, M>, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<ReaderTKind.@NotNull mu<R, M>, B>> aToMb
        ) {
            final ReaderT<R, M, A> rma = narrow(ma).getDelegate();
            return new ReaderTKind<>(r -> {
                final App<M, A> maApp = rma.apply(r);
                return monad.flatMap(maApp,
                        a -> {
                            final App<ReaderTKind.@NotNull mu<R, M>, B> krmb = aToMb.apply(a);
                            final ReaderT<R, M, B> rmb = narrow(krmb).getDelegate();
                            return rmb.apply(r);
                        }
                );
            });
        }

        @Override
        public @NotNull <A extends @NotNull Object> App<ReaderTKind.@NotNull mu<R, M>, A> lift(@NotNull final App<M, A> m) {
            return new ReaderTKind<>(r -> m);
        }

        @NotNull
        public App<ReaderTKind.@NotNull mu<R, M>, R> ask() {
            return new ReaderTKind<>(monad::<R>pure);
        }

        @NotNull
        public<A extends @NotNull Object> App<ReaderTKind.@NotNull mu<R, M>, A> asks(
                @NotNull final Function<? super R, ? extends A> fn
        ) {
            return new ReaderTKind<>(r -> monad.pure(fn.apply(r)));
        }

        @NotNull
        public<A extends @NotNull Object> App<ReaderTKind.@NotNull mu<R, M>, A> local(
                @NotNull final Function<? super R, ? extends R> fn,
                @NotNull final App<ReaderTKind.@NotNull mu<R, M>, A> from
        ) {
            final ReaderT<R, M, A> delegate = ReaderTKind.narrow(from).getDelegate().compose(fn)::apply;
            return new ReaderTKind<>(delegate);
        }
    }
}
