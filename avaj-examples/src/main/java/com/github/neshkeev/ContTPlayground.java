package com.github.neshkeev;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.mtl.ContT;
import com.github.neshkeev.avaj.mtl.ContTKind;
import com.github.neshkeev.avaj.mtl.StateT;
import com.github.neshkeev.avaj.mtl.StateTKind;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContTPlayground {
    public static void main(String[] args) {
        final var w = WriterK.WriterMonad.INSTANCE;
        final var m = new CoroutineTKind.CoroutineTMonad<Unit, WriterK.mu>(w);

        final var wHello = w.tell("Hello");
        final var myHello = m.flatMap(m.lift(wHello),
                x -> m.flatMap(m.yieldCo(),
                y -> m.flatMap(m.lift(w.tell(", ")),
                z -> m.yieldCo()
        )));
        final var fHello = m.forkCo(CoroutineTKind.narrow(myHello));

        final var wWorld = w.tell("World");
        final var myWorld = m.flatMap(m.lift(wWorld), x -> m.yieldCo());

        final var wri = m.runCoroutine(CoroutineTKind.narrow(m.followBy(m.forkCo(fHello), CoroutineTKind.narrow(myWorld))));
        System.out.println(WriterK.narrow(wri).getDelegate());
    }
}

// newtype CoroutineT r m a = CoroutineT {runCoroutineT' :: ContT r (StateT [CoroutineT r m ()] m) a}
//    deriving (Functor,Applicative,Monad,MonadCont,MonadIO)

interface CoroutineT<R, M extends Monad.mu, A> extends
        ContT<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, A> {}

class CoroutineTKind<R, M extends Monad.mu, A>
        implements App<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, A> {

    private final CoroutineT<R, M, A> delegate;

    public CoroutineTKind(@NotNull final CoroutineT<R, M, A> delegate) { this.delegate = delegate; }

    @NotNull
    public CoroutineT<R, M, A> getDelegate() { return delegate; }

    @NotNull
    public static<R, M extends Monad.mu, A> CoroutineTKind<R, M, A> narrow(
            @NotNull final App<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, A> kind
    ) {
        return (CoroutineTKind<R, M, A>) kind;
    }

    public interface mu<R, M extends Monad.mu> extends ContTKind.mu<R, M> { }

    public static final class CoroutineTMonad<R, M extends Monad.mu>
        implements Monad<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>>,
            MonadTrans<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, M>
    {

        private final ContTKind.ContTMonad<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>> contMonad;
        private final StateTKind.StateTMonad<List<CoroutineT<R, M, Unit>>, M> stateTMonad;
        private final Monad<M> internalMonad;

        public CoroutineTMonad(@NotNull final Monad<M> internalMonad) {
            this.internalMonad = internalMonad;
            this.stateTMonad = new StateTKind.StateTMonad<>(internalMonad);
            this.contMonad = new ContTKind.ContTMonad<>(stateTMonad);
        }

        @Override
        public @NotNull <A> App<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, A> pure(
                @NotNull final A a
        ) {
            final var ma = stateTMonad.pure(a);
            return new CoroutineTKind<>(c -> stateTMonad.flatMap(ma, c));
        }

        @Override
        public @NotNull <A, B> App<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, B> flatMap(
                @NotNull final App<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, A> ma,
                @NotNull final Function<@NotNull A, ? extends @NotNull App<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, B>> aToMb
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
        public CoroutineTKind<R, M, List<CoroutineT<R, M, Unit>>> getCCs() {

            final var delegate = ContTKind.narrow(contMonad.lift(stateTMonad.get())).getDelegate();

            return new CoroutineTKind<>(delegate::apply);
        }

        // putCCs :: Monad m => [CoroutineT r m ()] -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> putCCs(
                @NotNull final List<CoroutineT<R, M, Unit>> cors
        ) {
            final var coroutine = ContTKind.narrow(contMonad.lift(stateTMonad.put(cors))).getDelegate();
            return new CoroutineTKind<>(coroutine::apply);
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

//                        System.out.println("Dequeue size is " + rest.size());

                        return followBy(putCCs(rest), first);
                    }
            );
            return CoroutineTKind.narrow(kind);
        }

        // queue :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> enqueue(@NotNull final CoroutineTKind<R, M, Unit> next) {
            final var cor = next.getDelegate();
            final var kind = flatMap(getCCs(),
                    cors -> {
                        cors.add(cor);
//                        System.out.println("Enqueue size is " + cors.size());
                        return putCCs(cors);
                    });
            return CoroutineTKind.narrow(kind);
        }

        // next :: Monad m => CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> yieldCo() {
            return CoroutineTMonad.<R, M, Unit, Unit>callCC(
                    k -> CoroutineTKind.narrow(
                            followBy(enqueue(k.apply(Unit.UNIT)), dequeue())
                    )
            );
        }

        // fork :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> forkCo(@NotNull final CoroutineTKind<R, M, Unit> cor) {
            return CoroutineTMonad.<R, M, Unit, Unit>callCC(
                    k -> CoroutineTKind.narrow(
                            followBy(
                                    followBy(enqueue(k.apply(Unit.UNIT)), cor),
                                    dequeue()
                            )
                    )
            );
        }

        // exhaust :: Monad m => CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> exhaust() {
            System.out.println("Start exhaust");
            final var kind = flatMap(getCCs(),
                    ccs -> {
                        if (ccs.size() > 0) return followBy(yieldCo(), exhaust());
                        else return pure(Unit.UNIT);
                    });
            return CoroutineTKind.narrow(kind);
        }

        // runCoroutineT :: Monad m => CoroutineT r m r -> m r
        public App<M, R> runCoroutine(@NotNull final CoroutineTKind<R, M, R> cor) {
            System.out.println("run coroutine");
//            final CoroutineTKind<R, M, R> myCor = CoroutineTKind.narrow(discardRight(cor, exhaust()));
//            final CoroutineTKind<R, M, R> myCor = CoroutineTKind.narrow(discardRight(cor, this::exhaust));
            final CoroutineTKind<R, M, R> myCor = cor;
//            final Function<
//                    ? super @NotNull Supplier<App<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, Unit>>,
//                    ? extends @NotNull App<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, R>> function = this.discardRight(cor);
//
//            final Supplier<App<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, Unit>> t1 = this::exhaust;
//            final CoroutineTKind<R, M, R> myCor = CoroutineTKind.narrow(function.apply(t1));

            final Function<CoroutineTKind<R, M, R>, StateTKind<List<CoroutineT<R, M, Unit>>, M, R> > second =
                    ctk -> StateTKind.narrow(ctk.getDelegate().apply(stateTMonad::pure));

            final Function<StateT.Result<List<CoroutineT<R, M, Unit>>, R>, R> valueProjection = StateT.Result::getValue;

            final Function<StateTKind<List<CoroutineT<R, M, Unit>>, M, R>, App<M, R> > third =
                    st -> internalMonad
                            .map(valueProjection)
                            .apply(st.getDelegate().apply(new ArrayList<>()));

            return third.compose(second).apply(myCor);
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

        @Override
        public @NotNull <A> App<CoroutineTKind.mu<R, ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, A> lift(
                @NotNull final App<M, A> m
        ) {
            final var lift = contMonad.lift(new StateTKind<>(
                    s -> internalMonad.flatMap(m,
                            x -> internalMonad.pure(new StateT.Result<>(x, s))
                    )
            ));
            return new CoroutineTKind<>(ContTKind.narrow(lift).getDelegate()::apply);
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

class WriterK<A> implements App<WriterK.mu, A> {
    private final Writer<A> delegate;

    WriterK(@NotNull final Writer<A> delegate) { this.delegate = delegate; }

    public Writer<A> getDelegate() { return delegate; }
    public static<A> WriterK<A> narrow(@NotNull final App<WriterK.mu, A> kind) { return (WriterK<A>) kind; }

    public enum mu implements Monad.mu {}

    public enum WriterMonad implements Monad<WriterK.mu> {
        INSTANCE;

        @Override
        public @NotNull <A> App<WriterK.mu, A> pure(@NotNull final A a) { return new WriterK<>(new Writer<>("", a)); }

        @Override
        public @NotNull <A, B> App<WriterK.mu, B> flatMap(
                @NotNull final App<WriterK.mu, A> ma,
                @NotNull final Function<@NotNull A, ? extends @NotNull App<WriterK.mu, B>> aToMb
        ) {
            final var wa = narrow(ma).getDelegate();
            final var wb = narrow(aToMb.apply(wa.getValue())).getDelegate();

            return new WriterK<>(new Writer<>(wa.getLog() + wb.getLog(), wb.getValue()));
        }

        public App<WriterK.mu, Unit> tell(@NotNull final String log) {
            return new WriterK<>(new Writer<>(log, Unit.UNIT));
        }
    }
}
