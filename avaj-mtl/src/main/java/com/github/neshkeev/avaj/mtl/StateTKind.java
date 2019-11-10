package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
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

    public static class StateTMonad<S, M extends Monad.mu> implements Monad<mu<S, M>> {

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
    }
}
