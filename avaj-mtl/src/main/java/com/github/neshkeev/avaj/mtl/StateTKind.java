package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
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
    public static<S, M extends Monad.mu, A> StateTKind<S, M, A> narrow(@NotNull final App<mu<S, M>, A> kind) { return (StateTKind<S, M, A>) kind; }

    public interface mu<S, M extends Monad.mu> extends Monad.mu { }

    public static class StateTMonad<S, M extends Monad.mu> implements Monad<mu<S, M>>, MonadTrans<mu<S, M>, M> {

        private final Monad<M> internalMonad;

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
        public <A, B> App<StateTKind.mu<S, M>, B> flatMap(
                @NotNull final App<StateTKind.mu<S, M>, A> ma,
                @NotNull final Function<@NotNull A, ? extends @NotNull App<StateTKind.mu<S, M>, B>> aToMb
        ) {
            final StateT<S, M, B> smbStateT = s -> {
                final var ima = narrow(ma).getDelegate().apply(s);
                return internalMonad.flatMap(ima,
                        is -> narrow(aToMb.apply(is.getValue()))
                                .getDelegate()
                                .apply(s)
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
        public @NotNull <A> App<StateTKind.mu<S, M>, A> lift(@NotNull final App<M, A> m) {
            return null;
        }
    }
}
