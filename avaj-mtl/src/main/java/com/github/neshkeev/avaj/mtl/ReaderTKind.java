package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ReaderTKind<R, M extends Monad.mu, A> implements App<ReaderTKind.mu<R, M>, A> {

    private final ReaderT<R, M, A> delegate;

    public ReaderTKind(@NotNull final ReaderT<R, M, A> delegate) { this.delegate = delegate; }

    @NotNull
    public ReaderT<R, M, A> getDelegate() { return delegate; }

    @NotNull
    public static<R, M extends Monad.mu, A> ReaderTKind<R, M, A> narrow(@NotNull final App<ReaderTKind.mu<R, M>, A> kind) {
        return (ReaderTKind<R, M, A>) kind;
    }

    public static final class mu<R, M> implements Monad.mu {}

    public static class ReaderTMonad<R, M extends Monad.mu> implements Monad<mu<R, M>>, MonadTrans<mu<R, M>, M> {

        private final Monad<M> monad;

        public ReaderTMonad(Monad<M> monad) {
            this.monad = monad;
        }

        @NotNull
        @Override
        public <A> App<ReaderTKind.mu<R, M>, A> pure(@NotNull final A a) {
            final App<M, A> pure = monad.pure(a);
            final ReaderT<R, M, A> reader = r -> pure;
            return new ReaderTKind<>(reader);
        }

        @NotNull
        @Override
        public <A, B> App<ReaderTKind.mu<R, M>, B> flatMap(
                @NotNull final App<ReaderTKind.mu<R, M>, A> ma,
                @NotNull final Function<@NotNull A, ? extends @NotNull App<ReaderTKind.mu<R, M>, B>> aToMb
        ) {
            final ReaderT<R, M, A> rma = narrow(ma).getDelegate();
            return new ReaderTKind<R, M, B>(r -> {
                final App<M, A> maApp = rma.apply(r);
                final App<M, B> mbApp = monad.flatMap(maApp,
                        a -> {
                            final App<ReaderTKind.mu<R, M>, B> krmb = aToMb.apply(a);
                            final ReaderT<R, M, B> rmb = narrow(krmb).getDelegate();
                            return rmb.apply(r);
                        }
                );
                return mbApp;
            });
        }

        @NotNull
        @Override
        public <A> App<ReaderTKind.mu<R, M>, A> lift(@NotNull final App<M, A> m) {
            return new ReaderTKind<>(r -> m);
        }

        @NotNull
        public App<ReaderTKind.mu<R, M>, R> ask() {
            return new ReaderTKind<>(r -> monad.pure(r));
        }

        @NotNull
        public<A> App<ReaderTKind.mu<R, M>, A> asks(final Function<? super @NotNull R, ? extends @NotNull A> fn) {
            return new ReaderTKind<>(r -> monad.pure(fn.apply(r)));
        }

        @NotNull
        public<A> App<ReaderTKind.mu<R, M>, A> local(
                @NotNull final Function<? super @NotNull R, ? extends @NotNull R> fn,
                @NotNull final App<ReaderTKind.mu<R, M>, A> from
        ) {
            final ReaderT<R, M, A> delegate = ReaderTKind.narrow(from).getDelegate().compose(fn)::apply;
            return new ReaderTKind<>(delegate);
        }
    }
}
