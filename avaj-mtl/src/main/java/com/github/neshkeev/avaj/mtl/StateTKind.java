package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class StateTKind<S extends @NotNull Object, M extends @NotNull Object & Monad.mu, A extends @NotNull Object>
        implements App<StateTKind.@NotNull mu<S, M>, A> {

    public static void main(String[] args) {
        final StateTMonad<@NotNull Integer, Id.@NotNull mu> sm = new StateTMonad<>(Id.IdMonad.INSTANCE);
        System.out.println(Id.narrow(StateTKind.narrow(sm.flatMap(sm.put(5), s -> sm.pure("X"))).getDelegate().apply(1)).getValue());

        System.out.println(StateTKind.narrow(sm.flatMap(sm.get(),
                s -> sm.flatMap(sm.put(s + 1),
                ss -> sm.pure(s)
        ))).getDelegate().apply(1));
    }

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
        @NotNull
        public <A extends @NotNull Object, B extends @NotNull Object>
        App<StateTKind.@NotNull mu<S, M>, B> flatMap(
                @NotNull final App<StateTKind.@NotNull mu<S, M>, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<StateTKind.@NotNull mu<S, M>, B>> aToMb
        ) {
            final StateT<S, M, A> sToMA = StateTKind.narrow(ma).getDelegate();
            final StateT<S, M, B> res = s -> internalMonad.flatMap(sToMA.apply(s),
                    ss -> {
                        final S newState = ss.getState();
                        final A newVal = ss.getValue();
                        return aToMb
                                .andThen(StateTKind::narrow)
                                .andThen(StateTKind::getDelegate)
                                .apply(newVal)
                                .apply(newState)
                                ;
                    });
            return new StateTKind<>(res);
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

        @Contract(value = "_, _ -> !null", pure = true)
        public<A extends @NotNull Object> App<M, A> evalStateT(@NotNull final StateTKind<S, M, @NotNull A> m, @NotNull final S state) {
            return internalMonad.<StateT.Result<S, A>, A>map(StateT.Result::getValue)
                    .compose(m.getDelegate())
                    .apply(state);
        }
    }
}
