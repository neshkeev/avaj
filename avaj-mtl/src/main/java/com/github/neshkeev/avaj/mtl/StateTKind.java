package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class StateTKind<S extends @NotNull Object, M extends @NotNull Object & Monad.mu, A extends @NotNull Object> implements App<StateTKind.@NotNull mu<S, M>, A> {

    @NotNull
    private final StateT<S, M, A> delegate;

    public StateTKind(@NotNull final StateT<S, M, A> delegate) { this.delegate = delegate; }

    @NotNull
    public final StateT<S, M, A> getDelegate() { return delegate; }

    @NotNull
    public static<
            S extends @NotNull Object,
            M extends @NotNull Object & Monad.mu,
            A extends @NotNull Object
        > StateTKind<S, M, A> narrow(
                @NotNull final App<@NotNull mu<S, M>, A> kind
    ) {
        return (StateTKind<S, M, A>) kind;
    }

    public interface mu<S extends @NotNull Object, M extends @NotNull Object & Monad.mu> extends Monad.mu { }

    public static class StateTMonad<S extends @NotNull Object, M extends @NotNull Object & Monad.mu>
            implements Monad<@NotNull mu<S, M>>, MonadTrans<@NotNull mu<S, M>, M> {

        private final Monad<M> internalMonad;

        public StateTMonad(@NotNull final Monad<M> internalMonad) {
            this.internalMonad = internalMonad;
        }

        @NotNull
        @Override
        public <A extends @NotNull Object> App<StateTKind.@NotNull mu<S, M>, A> pure(final A a) {
            return new StateTKind<>(s -> internalMonad.pure(new StateT.Result<>(a, s)));
        }

        @Override
        public <A extends @NotNull Object, B extends @NotNull Object> @NotNull App<StateTKind.@NotNull mu<S, M>, B> flatMap(
                @NotNull final App<StateTKind.@NotNull mu<S, M>, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<StateTKind.@NotNull mu<S, M>, B>> aToMb
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

        @Override
        public @NotNull <A extends @NotNull Object> App<StateTKind.@NotNull mu<S, M>, A> lift(@NotNull final App<M, A> m) {
            return new StateTKind<>(
                    s -> internalMonad.flatMap(m,
                    x -> internalMonad.pure(new StateT.Result<>(x, s))
            ));
        }

        public StateTKind<S, M, S> get() {
            return new StateTKind<>(s -> internalMonad.pure(new StateT.Result<>(s, s)));
        }

        public StateTKind<S, M, @NotNull Unit> put(@NotNull final S state) {
            return new StateTKind<>(ignore -> internalMonad.pure(new StateT.Result<>(Unit.UNIT, state)));
        }
//        @NotNull
//        public static<S extends @NotNull Object, M extends Monad.mu> StateTMonad<S, M> narrow(Monad<? extends StateTKind.mu<S, M>> monad) {
//            return (StateTMonad<S, M>) monad;
//        }

    }
}
