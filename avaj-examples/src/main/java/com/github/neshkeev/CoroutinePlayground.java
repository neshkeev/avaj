package com.github.neshkeev;

import com.github.neshkeev.CoroutineTKind.CoroutineTMonad;
import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.data.List;
import com.github.neshkeev.avaj.data.StringMonoid;
import com.github.neshkeev.avaj.mtl.*;
import com.github.neshkeev.avaj.mtl.WriterKind.WriterMonad;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.github.neshkeev.avaj.Unit.UNIT;
import static com.github.neshkeev.avaj.data.List.Nil.nil;

public class CoroutinePlayground {
    public static void main(String[] args) {
//        simpleCoroutine();
        final WriterMonad<@NotNull String> w = new WriterMonad<>(StringMonoid.INSTANCE);
        final CoroutineTMonad<@NotNull Unit, WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>> m = new CoroutineTMonad<>(w);
        final App<WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>, @NotNull Unit> res = m.runCoroutine(
                m.flatMap(
                        m.fork(m.replicateM_(2, p("H"))), _l -> m.flatMap(
                        m.fork(m.replicateM_(5, p("B"))), __ ->
                        m.replicateM_(3, p("C"))
        )));
        System.out.println(WriterKind.narrow(WriterTKind.narrow(res)).getWriter().getLog());
    }

    public static CoroutineTKind<
            @NotNull Unit,
            WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>,
            @NotNull Unit
            > p(@NotNull final String word) {
        final WriterMonad<@NotNull String> w = new WriterMonad<>(StringMonoid.INSTANCE);
        final CoroutineTMonad<@NotNull Unit, WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>> m = new CoroutineTMonad<>(w);
        return m.flatMap(
                m.lift(w.tell(word)),
                __ -> m.yield()
        );
    }
    private static void simpleCoroutine() {
        final WriterTKind.WriterTMonad<@NotNull String, Id.@NotNull mu> w = new WriterTKind.WriterTMonad<>(StringMonoid.INSTANCE, Id.IdMonad.INSTANCE);
        final CoroutineTMonad<@NotNull Unit, WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>> m = new CoroutineTMonad<>(w);

        final App<
                WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>,
                @NotNull Unit> run = m.runCoroutine(
                m.flatMap(
                        m.fork(m.replicateM_(2, printOneString("H"))), _l -> m.flatMap(
                        m.fork(m.replicateM_(5, printOneString("B"))), __ ->
                        m.replicateM_(3, printOneString("C"))
        )));
        System.out.println(Id.narrow(WriterTKind.narrow(run).getDelegate()).getValue().getLog());
    }

    public static CoroutineTKind<
            @NotNull Unit,
            WriterTKind.@NotNull mu<@NotNull List<@NotNull String>, Id.@NotNull mu>,
            @NotNull Unit
            > printOneList(@NotNull final String word) {
        final WriterTKind.WriterTMonad<@NotNull List<@NotNull String>, Id.@NotNull mu> w = new WriterTKind.WriterTMonad<>(new List.ListMonoid<>(), Id.IdMonad.INSTANCE);
        final CoroutineTMonad<@NotNull Unit, WriterTKind.@NotNull mu<@NotNull List<@NotNull String>, Id.@NotNull mu>> m = new CoroutineTMonad<>(w);
        return m.flatMap(
                m.lift(w.tell(List.of(word))),
                __ -> m.yield()
        );
    }

    public static CoroutineTKind<
            @NotNull Unit,
            WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>,
            @NotNull Unit
        > printOneString(@NotNull final String word) {
        final WriterTKind.WriterTMonad<@NotNull String, Id.@NotNull mu> w = new WriterTKind.WriterTMonad<>(StringMonoid.INSTANCE, Id.IdMonad.INSTANCE);
        final CoroutineTMonad<@NotNull Unit, WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>> m = new CoroutineTMonad<>(w);
        return m.flatMap(
                m.lift(w.tell(word)),
                __ -> m.yield()
        );
    }
}

// (a -> m r) -> m r
// (a -> StateT<List<CoroutineT<R, M, A>>, M, A>) -> StateT<List<CoroutineT<R, M, A>>, M, A>
// s -> m (a, s)
// StateT<List<CoroutineT<R, M, A>>, M, A> :: List<CoroutineT<R, M, A>> -> M<Result<List<CoroutineT<R, M, A>>, A>>
@FunctionalInterface
interface CoroutineT<
        R extends @NotNull Object,
        M extends @NotNull Object & Monad.mu,
        A extends @NotNull Object
    > extends ContT<
        R,
        StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>,
        A
    > {}

final class CoroutineTKind <
        R extends @NotNull Object,
        M extends @NotNull Object & Monad.mu,
        A extends @NotNull Object
    > extends ContTKind<
        R,
        StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>,
        A
    > {

    public CoroutineTKind(@NotNull CoroutineT<R, M, A> delegate) {
        super(delegate);
    }

    @NotNull
    public static <
            R extends @NotNull Object,
            M extends @NotNull Object & Monad.mu,
            A extends @NotNull Object
        > CoroutineTKind<R, M, A> narrow(
                @NotNull final ContTKind<
                        R,
                        StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>,
                    A> kind
    ) {
        return new CoroutineTKind<R, M, A>(kind.getDelegate()::apply);
    }

    public static final class mu<R extends @NotNull Object, M extends @NotNull Object & Monad.mu> implements ContTKind.mu<R, M> { }

    public static final class CoroutineTMonad<
            R extends @NotNull Object,
            M extends @NotNull Object & Monad.mu
            > implements
            MonadCont<ContTKind.@NotNull mu<R,
                    StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>>,
            MonadTrans<ContTKind.@NotNull mu<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>, M> {

        @NotNull
        private final StateTKind.StateTMonad<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M> stateTMonad;
        @NotNull
        private final ContTMonad<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>> contTMonad;

        public CoroutineTMonad(final @NotNull Monad<M> internalMonad) {
            this.stateTMonad = new StateTKind.StateTMonad<>(internalMonad);
            this.contTMonad = new ContTMonad<>(stateTMonad);
        }

        @Override
        public @NotNull <A extends @NotNull Object> CoroutineTKind<R, M, A> pure(@NotNull final A value) {
            return narrow(contTMonad.pure(value));
        }

        @Override
        @Contract(value = "_, _ -> !null", pure = true)
        public @NotNull <A extends @NotNull Object, B extends @NotNull Object> CoroutineTKind<R, M, B> flatMap(
                @NotNull final App<ContTKind.@NotNull mu<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>, A> ma,
                @NotNull final Function<
                        ? super A,
                        ? extends @NotNull App<ContTKind.@NotNull mu<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>, B>
                    > aToMb
        ) {
            return narrow(contTMonad.flatMap(ma, aToMb));
        }

        @Override
        public @NotNull <A extends @NotNull Object, B extends @NotNull Object> CoroutineTKind<R, M, A> callCC(
                @NotNull final Function<
                        ? super @NotNull Function<
                                ? super A,
                                ? extends @NotNull App<ContTKind.@NotNull mu<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>, B>>,
                        ? extends @NotNull App<ContTKind.@NotNull mu<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>, A>
                        > aToMbToMa
        ) {
            return narrow(contTMonad.callCC(aToMbToMa));
        }

        // getCCs :: Monad m => CoroutineT r m [CoroutineT r m ()]
        // getCCs = CoroutineT $ lift get
        @NotNull
        @Contract(value = "-> !null", pure = true)
        public CoroutineTKind<R, M, @NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>> getCCs() {
            return narrow(contTMonad.lift(stateTMonad.get()));
        }

        // putCCs :: Monad m => [CoroutineT r m ()] -> CoroutineT r m ()
        // putCCs = CoroutineT . lift . put
        @NotNull
        @Contract(value = "_ -> !null", pure = true)
        public CoroutineTKind<R, M, @NotNull Unit> putCCs(
                @NotNull final List<@NotNull CoroutineT<R, M, @NotNull Unit>> existingCors
        ) {
            return narrow(contTMonad.lift(stateTMonad.put(existingCors)));
        }

        // dequeue :: Monad m => CoroutineT r m ()
        @NotNull
        @Contract(value = "-> !null", pure = true)
        public CoroutineTKind<R, M, @NotNull Unit> dequeue() {
            return this.flatMap(getCCs(),
                    currCCs -> {
                        if (currCCs.isEmpty()) return pure(UNIT);
                        final CoroutineT<R, M, @NotNull Unit> head = currCCs.head();
                        final List<@NotNull CoroutineT<R, M, @NotNull Unit>> tail = currCCs.tail();
                        return flatMap(putCCs(tail), __ -> new CoroutineTKind<>(head));
                    });
        }

        // queue :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @Contract(value = "_ -> !null", pure = true)
        @NotNull
        public CoroutineTKind<R, M, @NotNull Unit> queue(
                @NotNull final App<ContTKind.@NotNull mu<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>, @NotNull Unit> from
        ) {
            return flatMap(getCCs(),
                    currCCs -> putCCs(currCCs.merge(List.of(ContTKind.narrow(from).getDelegate()::apply)))
            );
        }

        // yield :: Monad m => CoroutineT r m ()
        @Contract(value = "-> !null", pure = true)
        @NotNull
        public CoroutineTKind<R, M, @NotNull Unit> yield() {
            return this.<Unit, Unit>callCC(
                    k -> flatMap(
                            queue(k.apply(UNIT)),
                            __ -> dequeue()
                    ));
        }

        //fork :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @NotNull
        @Contract(value = "_ -> !null", pure = true)
        public CoroutineTKind<R, M, @NotNull Unit> fork(
                @NotNull final App<ContTKind.@NotNull mu<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>, @NotNull Unit> from
        ) {
            return this.<Unit, Unit>callCC(k -> flatMap(
                    queue(k.apply(UNIT)), _u -> this.flatMap(
                            from,
                            _l -> dequeue()
                    )));
        }

        //exhaust :: Monad m => CoroutineT r m ()
        @NotNull
        @Contract(value = "-> !null", pure = true)
        public CoroutineTKind<R, M, @NotNull Unit> exhaust() {
            return flatMap(getCCs(),
                    ccs -> {
                        if (ccs.isEmpty()) return pure(UNIT);
                        return flatMap(this.yield(), __ -> exhaust());
                    });
        }

        @Contract(value = "_ -> !null", pure = true)
        @NotNull
        public App<M, R> runCoroutine(@NotNull final CoroutineTKind<R, M, R> cor) {
            // addExhaust :: Monad m => CoroutineT r m a -> CoroutineT r m a
            final Function<? super @NotNull CoroutineTKind<R, M, R>, ? extends @NotNull CoroutineTKind<R, M, R>> addExhaust =
                    x -> flatMap(x, l -> flatMap(exhaust(), __ -> pure(l)));

            // corToState :: Monad m => ContT r (StateT [CoroutineT r m ()] m) r -> StateT [CoroutineT r m ()] m r
            final Function<? super @NotNull CoroutineTKind<R, M, R>, ? extends @NotNull StateTKind<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M, R>> corToState =
                    x -> x.getDelegate()
                            .andThen(StateTKind::narrow)
                            .apply(stateTMonad::pure);

            // stateToInternal :: Monad m => StateT [CoroutineT r m ()] m r -> m r
            final Function<? super StateTKind<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M, R>, ? extends App<M, R>> stateToInternal=
                    x -> stateTMonad.evalStateT(x, nil());

            return addExhaust
                    .andThen(corToState)
                    .andThen(stateToInternal)
                    .apply(cor);
        }

        @Override
        @Contract(value = "_ -> !null", pure = true)
        @NotNull
        public <A extends @NotNull Object> CoroutineTKind<R, M, A> lift(@NotNull App<M, A> m) {
            return new CoroutineTKind<R, M, A>(contTMonad.lift(stateTMonad.lift(m)).getDelegate()::apply);
        }
    }
}
