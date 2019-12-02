package com.github.neshkeev.cors;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.mtl.ContT;
import com.github.neshkeev.avaj.mtl.ContTKind;
import com.github.neshkeev.avaj.mtl.StateT;
import com.github.neshkeev.avaj.mtl.StateTKind;
import com.github.neshkeev.avaj.mtl.StateTKind.StateTMonad;
import com.github.neshkeev.avaj.typeclasses.cov.MonadTrans;
import com.github.neshkeev.avaj.typeclasses.cov.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.neshkeev.avaj.Functions.alter;

public class Cors {
    public static void main(String[] args) {
        final var w = WriterK.WriterMonad.INSTANCE;
        final var m = new CoroutineTKind.CoroutineTMonad<Unit, WriterK.mu>(w);

        final var wHello = w.tell("Hello");
        final var myHello = m.flatMap(m.lift(wHello),
                x -> m.flatMap(
                m.yieldCo(),
                y -> m.flatMap(m.lift(w.tell(", ")), z ->
                m.yieldCo()
        )));
        final var fHello = m.forkCo(myHello);

        final var wWorld = w.tell("World");
        final var myWorld = m.flatMap(m.lift(wWorld), x -> m.yieldCo());

        final var wri = m.runCoroutine(

        m.chain(
                m.forkCo(fHello),
                myWorld
        ));
        System.out.println(WriterK.narrow(wri).getDelegate());
    }
}

interface CoroutineT<R, M extends Monad.mu, A> extends ContT<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, A> {}

class CoroutineTKind<R, M extends Monad.mu, A> extends ContTKind<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, A> {
    public CoroutineTKind(@NotNull CoroutineT<R, M, A> delegate) { super(delegate); }

    @Override
    public CoroutineT<R, M, A> getDelegate() { return super.getDelegate()::apply; }

    @NotNull
    public static<R, M extends Monad.mu, A> CoroutineTKind<R, M, A> narrowCoroutineT(
            @NotNull final App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, A> kind
    ) {
        final var narrow = narrow(kind);
        return new CoroutineTKind<R, M, A>(narrow.getDelegate()::apply);
    }

    public static final class CoroutineTMonad<R, M extends Monad.mu> implements Monad<ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>>, MonadTrans<M> {

        private final Monad<M> internalMonad;
        private final StateTMonad<List<CoroutineT<R, M, Unit>>, M> stateTMonad;
        private final ContTKind.ContTMonad<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>> contMonad;

        public CoroutineTMonad(@NotNull final Monad<M> internalMonad) {
            this.internalMonad = internalMonad;
            this.stateTMonad = new StateTMonad<>(internalMonad);
            this.contMonad = new ContTKind.ContTMonad<>(stateTMonad);
        }

        @Override
        public @NotNull <A> CoroutineTKind<R, M, A> pure(@NotNull A a) {
            return narrowCoroutineT(contMonad.pure(a));
        }

        @Override
        public @NotNull <A, B> CoroutineTKind<R, M, B> flatMap(
                final @NotNull App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, @NotNull A> ma,
                final @NotNull Function<? super @NotNull A, ? extends @NotNull App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, B>> aToMb
        ) {
            return narrowCoroutineT(contMonad.flatMap(ma, aToMb));
        }

        // getCCs :: Monad m => CoroutineT r m [CoroutineT r m ()]
        @NotNull
        public CoroutineTKind<R, M, List<CoroutineT<R, M, Unit>>> getCCs() {
            final var cont = ContTKind.
                    <
                            R,
                            StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>,
                            List<CoroutineT<R, M, Unit>>
                    > narrow(contMonad.lift(stateTMonad.get()));
            return CoroutineTKind.narrowCoroutineT(cont);
        }

        // putCCs :: Monad m => [CoroutineT r m ()] -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> putCCs(
                @NotNull final List<CoroutineT<R, M, Unit>> cors
        ) {
            final var cont = ContTKind.
                    <
                            R,
                            StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>,
                            Unit
                    > narrow(contMonad.lift(stateTMonad.put(cors)));
            return CoroutineTKind.narrowCoroutineT(cont);
        }

        // dequeue :: Monad m => CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> dequeue() {
            return flatMap(getCCs(), this::dequeFirst);
        }

        @NotNull
        private App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, Unit> dequeFirst(
                @NotNull final List<CoroutineT<R, M, Unit>> cors
        ) {
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

        // enqueue :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> enqueue(@NotNull final CoroutineTKind<R, M, Unit> next) {
            final var cor = next.getDelegate();
            final var kind = flatMap(
                    getCCs(),
                    cors -> putCCs(alter(cors, e -> e.add(cor)))
            );
            return new CoroutineTKind<>(kind.getDelegate());
        }

        // yieldCo :: Monad m => CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> yieldCo() {
            final var result = contMonad.<Unit, Unit>callCC(this::yieldInCallCC);
            return CoroutineTKind.narrowCoroutineT(result);
        }

        @NotNull
        private App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, Unit> yieldInCallCC(
                @NotNull final Function<
                        ? super @NotNull Unit,
                        ? extends @NotNull App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, Unit>
                        > kont
        ) {
            final var delegate = narrowCoroutineT(kont.apply(Unit.UNIT)).getDelegate();

            final var next = new CoroutineTKind<>(delegate);
            return chain(enqueue(next), dequeue());
        }

        // fork :: Monad m => CoroutineT r m () -> CoroutineT r m ()
        @NotNull
        public CoroutineTKind<R, M, Unit> forkCo(@NotNull final CoroutineTKind<R, M, Unit> cor) {
            final var result = contMonad.<Unit, Unit>callCC(
                    k -> {
                        final var delegate = narrowCoroutineT(k.apply(Unit.UNIT)).getDelegate();

                        final var enqueue = enqueue(new CoroutineTKind<>(delegate));

                        return chain(chain(enqueue, cor), dequeue());
                    }
            );
            return CoroutineTKind.narrowCoroutineT(result);
        }

        // runCoroutineT :: Monad m => CoroutineT r m r -> m r
        public App<? extends M, R> runCoroutine(
                @NotNull final App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, R> cor
        ) {
            final Function<CoroutineTKind<R, M, R>, StateTKind<List<CoroutineT<R, M, Unit>>, M, R> > second =
                    ctk -> StateTKind.narrow(ctk.getDelegate().apply(stateTMonad::pure));

            final Function<StateT.Result<List<CoroutineT<R, M, Unit>>, R>, R> valueProjection = StateT.Result::getValue;

            final Function<StateTKind<List<CoroutineT<R, M, Unit>>, M, R>, App<? extends M, R> > third =
                    st -> internalMonad
                            .map(valueProjection)
                            .apply(st.getDelegate().apply(new ArrayList<>()));

            return third.compose(second).apply(CoroutineTKind.narrowCoroutineT(cor));
        }

        @Override
        public @NotNull <A> CoroutineTKind<R, M, A> lift(@NotNull final App<? extends M, A> m) {
            final var lift = contMonad.
                    lift(stateTMonad.lift(m));
            return CoroutineTKind.narrowCoroutineT(lift);
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
    public static<A> WriterK<A> narrow(@NotNull final App<? extends WriterK.mu, A> kind) { return (WriterK<A>) kind; }

    public enum mu implements Monad.mu {}

    public enum WriterMonad implements Monad<WriterK.mu> {
        INSTANCE;

        @Override
        public @NotNull <A> App<WriterK.mu, A> pure(@NotNull final A a) { return new WriterK<>(new Writer<>("", a)); }

        @Override
        public @NotNull <A, B> App<WriterK.mu, B> flatMap(
                final @NotNull App<? extends WriterK.mu, @NotNull A> ma,
                final @NotNull Function<? super @NotNull A, ? extends @NotNull App<? extends WriterK.mu, B>> aToMb
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
