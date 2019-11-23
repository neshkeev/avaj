package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.typeclasses.cov.Monad;
import com.github.neshkeev.avaj.typeclasses.cov.MonadTrans;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class StateTKind<S, M extends Monad.mu, A> implements App<StateTKind.mu<S, M>, A> {
    private final StateT<S, M, A> delegate;

    public StateTKind(@NotNull final StateT<S, M, A> delegate) {
        this.delegate = delegate;
    }

    @NotNull
    public final StateT<S, M, A> getDelegate() { return delegate; }

    @NotNull
    public static<S, M extends Monad.mu, A> StateTKind<S, M, A> narrow(@NotNull final App<? extends mu<S, M>, A> kind) { return (StateTKind<S, M, A>) kind; }

    public interface mu<S, M extends Monad.mu> extends Monad.mu { }

    public static class StateTMonad<S, M extends Monad.mu> implements Monad<mu<S, M>>, MonadTrans<M> {

        private final Monad<M> internalMonad;

        @NotNull
        public static<S, M extends Monad.mu> StateTMonad<S, M> narrow(Monad<StateTKind.mu<S, M>> monad) {
            return (StateTMonad<S, M>) monad;
        }

        public StateTMonad(@NotNull final Monad<M> internalMonad) {
            this.internalMonad = internalMonad;
        }

        @NotNull
        @Override
        public <A> App<StateTKind.mu<S, M>, A> pure(@NotNull final A a) {
            return new StateTKind<>(s -> internalMonad.pure(new StateT.Result<>(a, s)));
        }

        @NotNull
        @Override
        public <A, B> App<? extends StateTKind.mu<S, M>, B> flatMap(
                final @NotNull App<? extends StateTKind.mu<S, M>, @NotNull A> ma,
                final @NotNull Function<? super @NotNull A, ? extends @NotNull App<? extends StateTKind.mu<S, M>, B>> aToMb
        ) {
            final StateT<S, M, B> smbStateT = s -> {
                final var ima = StateTKind.narrow(ma).getDelegate().apply(s);
                return internalMonad.flatMap(ima,
                        is -> StateTKind.narrow(aToMb.apply(is.getValue()))
                                .getDelegate()
                                .apply(is.getState())
                );
            };

            return new StateTKind<>(smbStateT);
        }

        public StateTKind<S, M, S> get() {
            return new StateTKind<>(s -> internalMonad.pure(new StateT.Result<>(s, s)));
        }

        public StateTKind<S, M, Unit> put(@NotNull final S state) {
            return new StateTKind<>(ignore -> internalMonad.pure(new StateT.Result<>(Unit.UNIT, state)));
        }

        @Override
        public @NotNull <A> App<? extends mu, A> lift(@NotNull App<? extends M, A> m) {
            return null;
        }
    }
}
