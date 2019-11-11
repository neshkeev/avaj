package com.github.neshkeev;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.data.kinds.ListKind;
import com.github.neshkeev.avaj.mtl.ContT;
import com.github.neshkeev.avaj.mtl.ContTKind;
import com.github.neshkeev.avaj.mtl.StateTKind;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContTPlayground {
    public static void main(String[] args) {

    }
}

// newtype CoroutineT r m a = CoroutineT {runCoroutineT' :: ContT r (StateT [CoroutineT r m ()] m) a}
//    deriving (Functor,Applicative,Monad,MonadCont,MonadIO)

interface CoroutineT<R, M extends Monad.mu, A> extends
        ContT<R, StateTKind.mu<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu>, A> {}

class CoroutineTKind<R, M extends Monad.mu, A>
        implements App<CoroutineTKind.mu<R, StateTKind.mu<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu>>, A> {

    private final CoroutineT<R, M, A> delegate;

    public CoroutineTKind(@NotNull final CoroutineT<R, M, A> delegate) { this.delegate = delegate; }

    public CoroutineT<R, M, A> getDelegate() { return delegate; }

    @NotNull
    public static<R, M extends Monad.mu, A> CoroutineTKind<R, M, A> narrow(
            @NotNull final App<CoroutineTKind.mu<R, StateTKind.mu<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu>>, A> kind
    ) {
        return (CoroutineTKind<R, M, A>) kind;
    }

    public interface mu<R, M extends Monad.mu> extends Monad.mu { }

    public static class CoroutineTMonad<R, M extends Monad.mu>
            implements Monad<mu<R, StateTKind.mu<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu>>> {

        private final ListKind.ListMonad listMonad;
        private final StateTKind.StateTMonad<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu> stateTMonad;
        private final ContTKind.ContTMonad<R, StateTKind.mu<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu>> contTMonad;

        public CoroutineTMonad() {
            listMonad = new ListKind.ListMonad();
            stateTMonad = new StateTKind.StateTMonad<>(listMonad);
            contTMonad = new ContTKind.ContTMonad<>(stateTMonad);
        }

        @Override
        public @NotNull <A> App<CoroutineTKind.mu<R, StateTKind.mu<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu>>, A> pure(
                @NotNull final A a
        ) {
            return new CoroutineTKind<>(r -> r.apply(a));
        }

        @Override
        public @NotNull <A, B> App<CoroutineTKind.mu<R, StateTKind.mu<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu>>, B> flatMap(
                @NotNull final App<CoroutineTKind.mu<R, StateTKind.mu<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu>>, A> ma,
                @NotNull final Function<@NotNull A, ? extends @NotNull App<CoroutineTKind.mu<R, StateTKind.mu<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu>>, B>> aToMb
        ) {
            final var corRma = narrow(ma).getDelegate();

            final var aToCorRmb = aToMb
                    .andThen(CoroutineTKind::narrow)
                    .andThen(CoroutineTKind::getDelegate);

            final CoroutineT<R, M, B> corRmb = r ->
                    corRma.apply(a -> aToCorRmb.apply(a)
                            .apply(r.andThen(StateTKind::narrow))
                    );

            return new CoroutineTKind<>(corRmb);
        }

        // getCCs :: Monad m => CoroutineT r m [CoroutineT r m ()]
        @NotNull
        public CoroutineTKind<R, M, ListKind<CoroutineT<R, M, Unit>>> getCCs() {

            final var coroutine = ContTKind.narrow(contTMonad.lift(stateTMonad.get())).getDelegate();

            return new CoroutineTKind<>(coroutine::apply);
        }

        // putCCs :: Monad m => [CoroutineT r m ()] -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> putCCs(
                @NotNull final ListKind<CoroutineT<R, M, Unit>> cors
        ) {
            final var listMonad = new ListKind.ListMonad();
            final var stateTMonad = new StateTKind.StateTMonad<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu>(listMonad);
            final var contTMonad = new ContTKind.ContTMonad<R, StateTKind.mu<ListKind<CoroutineT<R, M, Unit>>, ListKind.mu>>(stateTMonad);

            final var lift = ContTKind.narrow(contTMonad.lift(stateTMonad.put(cors))).getDelegate();
            return new CoroutineTKind<>(lift::apply);
        }

        // dequeue :: Monad m => CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> dequeue() {
            final var kind = flatMap(getCCs(),
                    ccs -> {
                        final var cors = ccs.getDelegate();

                        final List<CoroutineT<R, M, Unit>> rest;

                        if (cors.isEmpty()) {
                            return this.pure(Unit.UNIT);
                        } else if (cors.size() == 1) {
                            rest = List.of();
                        } else {
                            rest = cors.stream().skip(1).collect(Collectors.toList());
                        }

                        final var first = new CoroutineTKind<>(cors.get(0));
                        return followBy(putCCs(new ListKind<>(rest)), first);
                    }
            );
            return CoroutineTKind.narrow(kind);
        }
        // queue :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> enqueue(@NotNull final CoroutineTKind<R, M, Unit> next) {
            final var cor = next.getDelegate();
            final var kind = flatMap(getCCs(),
                    ccs -> {
                        final var cors = ccs.getDelegate();
                        cors.add(cor);
                        return putCCs(new ListKind<>(cors));
                    });
            return CoroutineTKind.narrow(kind);
        }

        // next :: Monad m => CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> next() {
            return CoroutineTMonad.<R, M, Unit, Unit>callCC(
                    k -> CoroutineTKind.narrow(
                            followBy(enqueue(k.apply(Unit.UNIT)), dequeue())
                    )
            );
        }

        // fork :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> fork(@NotNull final CoroutineTKind<R, M, Unit> cor) {
            return CoroutineTMonad.<R, M, Unit, Unit>callCC(
                    k -> CoroutineTKind.narrow(
                            followBy(
                                    followBy(enqueue(k.apply(Unit.UNIT)), cor),
                                    dequeue()
                            )
                    )
            );
        }

        @NotNull
        public static <R, M extends Monad.mu, A, B> CoroutineTKind<R, M, A> callCC(
                @NotNull final Function<
                        @NotNull Function<
                                ? super @NotNull A,
                                ? extends @NotNull CoroutineTKind<R, M, B>>,
                        ? extends @NotNull CoroutineTKind<R, M, A>> aToRbToRa
        ) {
            return new CoroutineTKind<>(
                    ar -> {
                        final Function<? super @NotNull A, ? extends @NotNull CoroutineTKind<R, M, B>> fabr =
                                a -> new CoroutineTKind<>(br -> ar.apply(a));

                        return aToRbToRa.apply(fabr).getDelegate().apply(ar);
                    }
            );
        }
    }
}

