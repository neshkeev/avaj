package com.github.neshkeev;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.mtl.ContT;
import com.github.neshkeev.avaj.mtl.ContTKind;
import com.github.neshkeev.avaj.mtl.StateT;
import com.github.neshkeev.avaj.mtl.StateTKind;
import com.github.neshkeev.avaj.typeclasses.cov.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Coroutines {

    public static void main(String[] args) {
        final CoroutineTKind.CoroutineTMonad<Integer, Id.mu> cor = new CoroutineTKind.CoroutineTMonad<Integer, Id.mu>(Id.IdMonad.INSTANCE);
        final App<? extends ContTKind.mu<Integer, StateTKind.mu<List<CoroutineT<Integer, Id.mu, Unit>>, Id.mu>>, Integer> pure = cor.pure(5);
        CoroutineTKind.narrowCoroutineT(pure);
    }
}

interface CoroutineT<R, M extends Monad.mu, A> extends ContT<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, A> {}

class CoroutineTKind<R, M extends Monad.mu, A>
        extends ContTKind<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, A>
{
    public CoroutineTKind(@NotNull final CoroutineT<R, M, A> delegate) { super(delegate); }

    @NotNull
    public static<R, M extends Monad.mu, A> CoroutineTKind<R, M, A> narrowCoroutineT(
            @NotNull final App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, A> kind
    ) {
        final var narrow = ContTKind.narrow(kind);
        return (CoroutineTKind<R, M, A>) narrow;
    }

    public interface mu<R, M extends Monad.mu> extends StateTKind.mu<List<CoroutineT<R, M, Unit>>, M> {}

    public static final class CoroutineTMonad<R, M extends Monad.mu> extends ContTKind.ContTMonad<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>> {
        private final StateTKind.StateTMonad<List<CoroutineT<R, M, Unit>>, M> stateTMonad;
        private final Monad<M> internalMonad;

        public CoroutineTMonad(@NotNull final Monad<M> internalMonad) {
            super(new StateTKind.StateTMonad<>(internalMonad));
            this.internalMonad = internalMonad;

            this.stateTMonad = StateTKind.StateTMonad.narrow(getMonad());
        }

        @Override
        public <A> App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, A> ctor(@NotNull ContT<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, A> cont) {
            return new CoroutineTKind<R, M, A>(cont::apply);
        }

        // getCCs :: Monad m => CoroutineT r m [CoroutineT r m ()]
        @NotNull
        public CoroutineTKind<R, M, List<CoroutineT<R, M, Unit>>> getCCs() {
            final var delegate = CoroutineTKind.narrowCoroutineT(lift(stateTMonad.get())).getDelegate();

            return new CoroutineTKind<R, M, List<CoroutineT<R, M, Unit>>>(delegate::apply);
        }

        // putCCs :: Monad m => [CoroutineT r m ()] -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> putCCs(
                @NotNull final List<CoroutineT<R, M, Unit>> cors
        ) {
            final var coroutine = CoroutineTKind.narrowCoroutineT(lift(stateTMonad.put(cors))).getDelegate();
            return new CoroutineTKind<R, M, Unit>(coroutine::apply);
        }

        // dequeue :: Monad m => CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> dequeue() {
            final var kind = flatMap(getCCs(),
                    cors -> {
                        final List<CoroutineT<R, M, Unit>> rest;

                        if (cors.isEmpty()) {
                            return this.pure(Unit.UNIT);
                        } else if (cors.size() == 1) {
                            rest = new ArrayList<>(0);
                        } else {
                            rest = cors.stream().skip(1).collect(Collectors.toList());
                        }

                        final var first = new CoroutineTKind<>(cors.get(0));

                        return chain(putCCs(rest), first);
                    }
            );
            return new CoroutineTKind<R, M, Unit>(ContTKind.narrow(kind).getDelegate()::apply);
        }

        // enqueue :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> enqueue(@NotNull final CoroutineTKind<R, M, Unit> next) {
            final CoroutineT<R, M, Unit> cor = next.getDelegate()::apply;
            final var kind = flatMap(getCCs(),
                    cors -> {
                        cors.add(cor);
                        return putCCs(cors);
                    });
            return new CoroutineTKind<R, M, Unit>(ContTKind.narrow(kind).getDelegate()::apply);
        }

        // yieldCo :: Monad m => CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> yieldCo() {
            final var result = ContTMonad.<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, Unit, Unit>callCC(this::yieldInCallCC);
            return CoroutineTKind.narrowCoroutineT(result);
        }

        @NotNull
        private ContTKind<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, Unit> yieldInCallCC(
                @NotNull final Function<
                        ? super @NotNull Unit,
                        ? extends @NotNull ContTKind<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, Unit>
                        > k
        ) {
            final var delegate = k.apply(Unit.UNIT).getDelegate();

            final var chain = chain(enqueue(new CoroutineTKind<R, M, Unit>(delegate::apply)), dequeue());
            return CoroutineTKind.narrow(chain);
        }

        // fork :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> forkCo(@NotNull final CoroutineTKind<R, M, Unit> cor) {
            final var result = ContTMonad.<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, Unit, Unit>callCC(
                    k -> {
                        final var delegate = k.apply(Unit.UNIT).getDelegate();

                        final CoroutineTKind<R, M, Unit> narrow = CoroutineTKind.narrowCoroutineT(
                                chain(
                                        chain(enqueue(new CoroutineTKind<R, M, Unit>(delegate::apply)), cor),
                                        dequeue()
                                )
                        );
                        return narrow;
                    }
            );
            return CoroutineTKind.narrowCoroutineT(result);
        }


        // runCoroutineT :: Monad m => CoroutineT r m r -> m r
        public App<? extends M, R> runCoroutine(@NotNull final CoroutineTKind<R, M, R> cor) {
            final Function<CoroutineTKind<R, M, R>, StateTKind<List<CoroutineT<R, M, Unit>>, M, R> > second =
                    ctk -> StateTKind.narrow(ctk.getDelegate().apply(stateTMonad::pure));

            final Function<StateT.Result<List<CoroutineT<R, M, Unit>>, R>, R> valueProjection = StateT.Result::getValue;

            final Function<StateTKind<List<CoroutineT<R, M, Unit>>, M, R>, App<? extends M, R> > third =
                    st -> internalMonad
                            .map(valueProjection)
                            .apply(st.getDelegate().apply(new ArrayList<>()));
            return third.compose(second).apply(cor);
        }
    }
}

final class Id<A> implements App<Id.mu, A> {
    private final A value;

    public Id(@NotNull final A value) {
        this.value = value;
    }

    @NotNull
    public final A getValue() {
        return value;
    }

    @NotNull
    public static <A> Id<A> narrow(@NotNull final App<? extends mu, A> kind) {
        return (Id<A>) kind;
    }

    public static final class mu implements Monad.mu { }

    @Override
    public final String toString() {
        return value.toString();
    }

    public enum IdMonad implements Monad<mu> {
        INSTANCE;

        @NotNull
        @Override
        public <A> App<Id.mu, A> pure(@NotNull final A a) {
            return new Id<>(a);
        }

        @NotNull
        @Override
        public <A, B> App<? extends Id.mu, B> flatMap(
                final @NotNull App<? extends Id.mu, @NotNull A> ma,
                final @NotNull Function<? super @NotNull A, ? extends @NotNull App<? extends Id.mu, B>> aToMb
        ) {
            final var value = narrow(ma).getValue();
            return aToMb.apply(value);
        }
    }
}
