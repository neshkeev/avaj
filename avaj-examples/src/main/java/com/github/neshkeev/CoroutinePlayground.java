package com.github.neshkeev;

import com.github.neshkeev.CoroutineTKind.CoroutineTMonad;
import com.github.neshkeev.WriterK.WriterMonad;
import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Functions;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.data.List;
import com.github.neshkeev.avaj.mtl.*;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

import static com.github.neshkeev.avaj.Functions.constFunction;
import static com.github.neshkeev.avaj.Unit.UNIT;
import static com.github.neshkeev.avaj.data.List.Nil.nil;

public class CoroutinePlayground {
    public static void main(String[] args) {
        final CoroutineTMonad<@NotNull Unit, WriterK.@NotNull mu> m = new CoroutineTMonad<>(WriterMonad.INSTANCE);
        final ContTKind<@NotNull Unit, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit>>, WriterK.@NotNull mu>, @NotNull Unit> fiveA = ContTKind.narrow(m.replicateM_(2, printOne(1, "a")));
        final CoroutineTKind<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit> one = new CoroutineTKind<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit>(fiveA.getDelegate()::apply);

        final ContTKind<@NotNull Unit, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit>>, WriterK.@NotNull mu>, @NotNull Unit> sixB = ContTKind.narrow(m.replicateM_(16, printOne(1, "b")));
        final CoroutineTKind<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit> two = new CoroutineTKind<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit>(sixB.getDelegate()::apply);

        final ContTKind<@NotNull Unit, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit>>, WriterK.@NotNull mu>, @NotNull Unit> fourC = ContTKind.narrow(m.replicateM_(4, printOne(1, "c")));
        final CoroutineTKind<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit> three = new CoroutineTKind<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit>(fourC.getDelegate()::apply);

//        final Supplier<@NotNull App<ContTKind.mu<@NotNull Unit, StateTKind.mu<@NotNull List<@NotNull CoroutineT<@NotNull Unit, WriterK.mu, @NotNull Unit>>, WriterK.mu>>, @NotNull Unit>> exhaust = m::exhaust;
//        System.out.println(WriterK.narrow(m.runCoroutine(
//                new CoroutineTKind<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit>(ContTKind.narrow(
//                        m.<Unit, Unit>discardRight((m.flatMap(
//                                m.fork(one), _c -> m.flatMap(
//                                        m.fork(two), _d ->
//                                                three
//                                )))).apply(exhaust)).getDelegate()::apply
//                ))).getDelegate().getLog());

        System.out.println(WriterK.narrow(
                m.runCoroutine(
                new CoroutineTKind<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit>(m.flatMap(
                        m.fork(one), _c -> m.flatMap(
                        m.fork(two), _d ->
                        three
                        )).getDelegate()::apply)
                )
        ).getDelegate().getLog());
    }

    public static CoroutineTKind<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit> printOne(int i, String word) {
        final WriterMonad w = WriterMonad.INSTANCE;
        final CoroutineTMonad<@NotNull Unit, WriterK.@NotNull mu> m = new CoroutineTMonad<>(w);
        return new CoroutineTKind<@NotNull Unit, WriterK.@NotNull mu, @NotNull Unit>(m.flatMap(m.lift(w.tell(word.repeat(i))), ccc -> m.yield()).getDelegate()::apply);
    }
}

//
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

    public static final class mu<R extends @NotNull Object, M extends @NotNull Object & Monad.mu> implements ContTKind.mu<R, M> { }

    public static final class CoroutineTMonad<
            R extends @NotNull Object,
            M extends @NotNull Object & Monad.mu
        > implements MonadCont<ContTKind.@NotNull mu<R,
        StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>>,
        MonadTrans<ContTKind.@NotNull mu<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>, M>
    {
        @NotNull private final Monad<M> internalMonad;

        @NotNull private final StateTKind.StateTMonad<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M> stateTMonad;
        @NotNull private final ContTMonad<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>> contTMonad;
        public CoroutineTMonad(final @NotNull Monad<M> internalMonad) {
            this.internalMonad = internalMonad;
            this.stateTMonad = new StateTKind.StateTMonad<>(internalMonad);
            this.contTMonad = new ContTMonad<>(stateTMonad);
        }

        @Override
        @Contract(value = "_ -> !null", pure = true)
        @NotNull
        public <A extends @NotNull Object, B extends @NotNull Object> ContTKind<
                R,
                StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>,
                A
            > callCC(@NotNull final Function<
                ? super @NotNull Function<
                        ? super A,
                        ? extends @NotNull App<
                                ContTKind.@NotNull mu<
                                        R,
                                        StateTKind.@NotNull mu<
                                                @NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>,
                                                M>>,
                                B>>,
                ? extends @NotNull App<ContTKind.@NotNull mu<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>, A>> aToMbToMa) {
            return contTMonad.callCC(aToMbToMa);
        }

        @Override
        @Contract(value = "_ -> !null", pure = true)
        public @NotNull <A extends @NotNull Object>
        ContTKind<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>, A> pure(@NotNull final A a) {
            return contTMonad.pure(a);
        }

        @Override
        @NotNull
        @Contract(value = "_, _ -> !null", pure = true)
        public <A extends @NotNull Object, B extends @NotNull Object>
        ContTKind<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>, B> flatMap(
                    @NotNull final App<ContTKind.@NotNull mu<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>, A> ma,
                    @NotNull final Function<? super A, ? extends @NotNull App<ContTKind.@NotNull mu<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>>, B>> aToMb
        ) {
            return contTMonad.flatMap(ma, aToMb);
        }

        // getCCs :: Monad m => CoroutineT r m [CoroutineT r m ()]
        // getCCs = CoroutineT $ lift get
        @NotNull
        @Contract(value = "-> !null", pure = true)
        public CoroutineTKind<R, M, @NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>> getCCs() {
            return new CoroutineTKind<R, M, @NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>>(
                    contTMonad.lift(stateTMonad.get()).getDelegate()::apply
            );
        }

        // putCCs :: Monad m => [CoroutineT r m ()] -> CoroutineT r m ()
        // putCCs = CoroutineT . lift . put
        @NotNull
        @Contract(value = "_ -> !null", pure = true)
        public CoroutineTKind<R, M, @NotNull Unit> putCCs(
                @NotNull final List<@NotNull CoroutineT<R, M, @NotNull Unit>> existingCors
        ) {
            return new CoroutineTKind<R, M, @NotNull Unit>(
                    contTMonad.lift(stateTMonad.put(existingCors)).getDelegate()::apply
            );
        }

        // dequeue :: Monad m => CoroutineT r m ()
        @NotNull
        @Contract(value = "-> !null", pure = true)
        public CoroutineTKind<R, M, @NotNull Unit> dequeue() {
            final ContTKind<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>, @NotNull Unit> res =
                    flatMap(getCCs(),
                            currCCs -> {
                                if (currCCs.isEmpty()) return pure(UNIT);
                                return flatMap(
                                        putCCs(currCCs.tail()),
                                        _c -> new ContTKind<>(currCCs.head())
                                );
                            });
            return new CoroutineTKind<R, M, @NotNull Unit>(res.getDelegate()::apply);
        }

        // queue :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @Contract(value = "_ -> !null", pure = true)
        @NotNull
        public CoroutineTKind<R, M, @NotNull Unit> queue(@NotNull final CoroutineTKind<R, M, @NotNull Unit> from) {
            final List<@NotNull CoroutineT<R, M, @NotNull Unit>> newCor = List.of(from.getDelegate()::apply);
            final ContTKind<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>, @NotNull Unit> res =
                    flatMap(
                            getCCs(),
                            ccs -> putCCs(ccs.merge(newCor))
                    );
            return new CoroutineTKind<R, M, @NotNull Unit>(res.getDelegate()::apply);
        }

        // yield :: Monad m => CoroutineT r m ()
        @Contract(value = "-> !null", pure = true)
        @NotNull
        public CoroutineTKind<R, M, @NotNull Unit> yield() {
            final ContTKind<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>, @NotNull Unit> res =
                    this.<Unit, Unit>callCC(
                            k -> {
                                final CoroutineT<R, M, @NotNull Unit> cor = ContTKind.narrow(k.apply(UNIT)).getDelegate()::apply;
                                return flatMap(
                                        queue(new CoroutineTKind<>(cor)),
                                        c -> dequeue()
                                );
                            }
                    );
            return new CoroutineTKind<R, M, @NotNull Unit>(res.getDelegate()::apply);
        }

        //fork :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @NotNull
        @Contract(value = "_ -> !null", pure = true)
        public CoroutineTKind<R, M, @NotNull Unit> fork(@NotNull final CoroutineTKind<R, M, @NotNull Unit> p) {
            final ContTKind<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>, @NotNull Unit> res =
                    this.<Unit, Unit>callCC(
                            k -> {
                                final CoroutineT<R, M, @NotNull Unit> cor = ContTKind.narrow(k.apply(UNIT)).getDelegate()::apply;
                                return flatMap(queue(new CoroutineTKind<>(cor)),
                                        _c -> flatMap(
                                        p,
                                        _e -> dequeue()
                                ));
                            }
            );
            return new CoroutineTKind<R, M, @NotNull Unit>(res.getDelegate()::apply);
        }

        //exhaust :: Monad m => CoroutineT r m ()
        @NotNull
        @Contract(value = "-> !null", pure = true)
        public CoroutineTKind<R, M, @NotNull Unit> exhaust() {
            final ContTKind<R, StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>, @NotNull Unit> res =
                flatMap(getCCs(),
                    ccs -> {
                        if (ccs.isEmpty()) return pure(UNIT);
                        else return flatMap(
                                this.yield(),
                                d -> exhaust()
                        );
                    });
            return new CoroutineTKind<R, M, @NotNull Unit>(res.getDelegate()::apply);
        }
        //runCoroutineT :: Monad m => CoroutineT r m r -> m r
        //runCoroutineT a = stateToInternal (corToState (runCoroutineT' (addExhaust a)))
        //  where
        //    stateToInternal :: Monad m => StateT [CoroutineT r m ()] m r -> m r
        //    stateToInternal = flip evalStateT []
        //    corToState :: Monad m => ContT r (StateT [CoroutineT r m ()] m) r -> StateT [CoroutineT r m ()] m r
        //    corToState = flip runContT return
        //    addExhaust :: Monad m => CoroutineT r m a -> CoroutineT r m a
        //    addExhaust x = liftA2 const x exhaust
        @Contract(value = "_ -> !null", pure = true)
        @NotNull
        public App<M, R> runCoroutine(@NotNull final CoroutineTKind<R, M, R> cor) {
            final Function<@NotNull CoroutineTKind<R, M, R>, @NotNull CoroutineTKind<R, M, R>> addExhaust =
                    x -> new CoroutineTKind<R, M, R>(ContTKind.narrow(liftA2(Functions.<R, Unit>constFunction()).apply(x).apply(exhaust())).getDelegate()::apply);
            final Function<@NotNull CoroutineTKind<R, M, R>, StateTKind<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M, R>> corToState =
                    x -> x.getDelegate().andThen(StateTKind::narrow).apply(stateTMonad::pure);
            final Function<@NotNull StateTKind<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M, R>, @NotNull App<M, R>> stateToInternal =
                    x -> stateTMonad.evalStateT(x, nil());

            return addExhaust.andThen(corToState).andThen(stateToInternal).apply(cor);
        }

        @Override
        @Contract(value = "_ -> !null", pure = true)
        @NotNull
        public <A extends @NotNull Object> CoroutineTKind<R, M, A> lift(@NotNull App<M, A> m) {
            return new CoroutineTKind<R, M, A>(contTMonad.lift(stateTMonad.lift(m)).getDelegate()::apply);
        }
    }
}

class Writer<A> {
    private final String log;
    private final A value;

    Writer(@NotNull final String log, @NotNull final A value) {
        this.log = log;
        this.value = value;
    }

    public String getLog() { return log; }
    public A getValue() { return value; }

    @Override
    public String toString() {
        return "(" + log + ", " + value + ")";
    }
}

class WriterK<A extends @NotNull Object> implements App<WriterK.@NotNull mu, A> {
    @NotNull private final Writer<A> delegate;

    WriterK(@NotNull final Writer<A> delegate) { this.delegate = delegate; }

    @NotNull
    public Writer<A> getDelegate() { return delegate; }
    public static<A extends @NotNull Object> WriterK<A> narrow(@NotNull final App<WriterK.@NotNull mu, A> kind) { return (WriterK<A>) kind; }

    public enum mu implements Monad.mu {}

    public enum WriterMonad implements Monad<WriterK.@NotNull mu> {
        INSTANCE;

        @Override
        @Contract(value = "_ -> !null", pure = true)
        public @NotNull <A extends @NotNull Object> App<WriterK.@NotNull mu, A> pure(A a) {
            return new WriterK<>(new Writer<>("", a));
        }

        @Override
        @Contract(value = "_, _ -> !null", pure = true)
        public @NotNull <A extends @NotNull Object, B extends @NotNull Object> App<WriterK.@NotNull mu, B> flatMap(
                @NotNull final App<WriterK.@NotNull mu, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<WriterK.@NotNull mu, B>> aToMb) {
            final Writer<A> wa = narrow(ma).getDelegate();
            final Writer<B> wb = narrow(aToMb.apply(wa.getValue())).getDelegate();

            return new WriterK<>(new Writer<>(wa.getLog() + wb.getLog(), wb.getValue()));
        }

        @Contract(value = "_ -> !null", pure = true)
        public @NotNull App<WriterK.@NotNull mu, @NotNull Unit> tell(@NotNull final String a) {
            return new WriterK<>(new Writer<>(a, UNIT));
        }
    }
}
