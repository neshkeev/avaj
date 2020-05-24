package com.github.neshkeev;

//import com.github.neshkeev.avaj.App;
//import com.github.neshkeev.avaj.Unit;
//import com.github.neshkeev.avaj.mtl.ContT;
//import com.github.neshkeev.avaj.mtl.ContTKind;
//import com.github.neshkeev.avaj.mtl.StateT;
//import com.github.neshkeev.avaj.mtl.StateTKind;
//import com.github.neshkeev.avaj.typeclasses.Monad;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import static com.github.neshkeev.avaj.Functions.alter;
//
//public class Coroutines {
//
//    public static void main(String[] args) {
//
//        final var w = WriterK.WriterMonad.INSTANCE;
//        final var m = new CoroutineTKind.CoroutineTMonad<Unit, WriterK.mu>(w);
//
//        final var wHello = w.tell("Hello");
////        final CoroutineTKind<Unit, WriterK.mu, Unit> chain = m.chainMany(
////                m.lift1(wHello),
////                m.yieldCo(),
////                m.lift1(w.tell(", ")),
////                m.yieldCo()
////        );
//        final var myHello = m.flatMap(m.lift1(wHello),
//                x -> m.flatMap(
//                m.yieldCo(),
//                y -> m.flatMap(m.lift1(w.tell(", ")), z ->
//                m.yieldCo()
//        )));
//        final var fHello = m.forkCo(myHello);
//
//        final var wWorld = w.tell("World");
//        final var myWorld = m.flatMap(m.lift1(wWorld), x -> m.yieldCo());
//
//        final var wri = m.runCoroutine(m.chain(m.forkCo(fHello), CoroutineTKind.narrow(myWorld)));
//        System.out.println(WriterK.narrow(wri).getDelegate());
//    }
//}
//
//interface CoroutineT<R, M extends Monad.mu, A> extends ContT<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, A> {}
//
//class CoroutineTKind<R, M extends Monad.mu, A>
//        extends ContTKind<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, A>
//{
//    public CoroutineTKind(@NotNull final CoroutineT<R, M, A> delegate) { super(delegate); }
//
//    @Override
//    public CoroutineT<R, M, A> getDelegate() {
//        final var delegate = super.getDelegate();
//        return delegate::apply;
//    }
//
//    @NotNull
//    public static<R, M extends Monad.mu, A> CoroutineTKind<R, M, A> narrowCoroutineT(
//            @NotNull final App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, A> kind
//    ) {
//        final var narrow = ContTKind.narrow(kind);
//        return (CoroutineTKind<R, M, A>) narrow;
//    }
//
//    public interface mu<R, M extends Monad.mu> extends StateTKind.mu<List<CoroutineT<R, M, Unit>>, M> {}
//
//    public static final class CoroutineTMonad<R, M extends Monad.mu> extends ContTKind.ContTMonad<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>> {
//        private final StateTKind.StateTMonad<List<CoroutineT<R, M, Unit>>, M> stateTMonad;
//        private final Monad<M> internalMonad;
//
//        public CoroutineTMonad(@NotNull final Monad<M> internalMonad) {
//            super(new StateTKind.StateTMonad<>(internalMonad));
//            this.internalMonad = internalMonad;
//
//            this.stateTMonad = StateTKind.StateTMonad.narrow(getMonad());
//        }
//
////        @Override
//        public <A> App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, A> ctor(@NotNull ContT<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, A> cont) {
//            return new CoroutineTKind<R, M, A>(cont::apply);
//        }
//
//        @Override
//        public @NotNull <A, B> CoroutineTKind<R, M, B> flatMap(
//                @NotNull App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, @NotNull A> ma,
//                @NotNull Function<? super @NotNull A, ? extends @NotNull App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, B>> aToMb
//        ) {
//            final var result = super.flatMap(ma, aToMb);
//            return narrowCoroutineT(result);
//        }
//
//        @Override
//        public @NotNull <A, B> CoroutineTKind<R, M, B> chain(
//                @NotNull final App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, A> ma,
//                @NotNull final App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, B> mb
//        ) {
//            return narrowCoroutineT(super.chain(ma, mb));
//        }
//
////        <A> CoroutineTKind<R, M, A> chainMany(
////                @NotNull final App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, A> ma,
////                @NotNull final App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, A>... mas
////        ) {
////            if (mas.length == 0) return narrowCoroutineT(ma);
////            final Function<? super App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, A>, ? extends Function<? super App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, A>, ? extends App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, A>>> curChain = Functions.curry(this::chain);
//////            final var apply = curChain.apply(ma);
//////            for(var a : mas) {
//////
//////            }
////            Arrays.stream(mas).collect(
////                    Collector.of(
////                            () -> curChain.apply(ma),
////                            (acc, next) -> {
////                                acc.apply(curChain.apply(next));
////                                return;
////                                },
////                            null
////                    )
////            )
////            return null;
////        }
//
//        @Override
//        public @NotNull <A> CoroutineTKind<R, M, A> lift(
//                @NotNull final App<? extends StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>, A> m
//        ) {
//            return narrowCoroutineT(super.lift(m));
//        }
//
//        public @NotNull <A> CoroutineTKind<R, M, A> lift1(
//                @NotNull final App<M, A> m
//        ) {
//            final var lift = super.lift(new StateTKind<>(
//                    s -> internalMonad.flatMap(m,
//                    x -> internalMonad.pure(new StateT.Result<>(x, s))
//                    )
//            ));
//            return narrowCoroutineT(lift);
//        }
//
//        // getCCs :: Monad m => CoroutineT r m [CoroutineT r m ()]
//        @NotNull
//        public CoroutineTKind<R, M, List<CoroutineT<R, M, Unit>>> getCCs() {
//            final var delegate = lift(stateTMonad.get()).getDelegate();
//            return new CoroutineTKind<>(delegate);
//        }
//
//        // putCCs :: Monad m => [CoroutineT r m ()] -> CoroutineT r m ()
//        @NotNull
//        public CoroutineTKind<R, M, Unit> putCCs(
//                @NotNull final List<CoroutineT<R, M, Unit>> cors
//        ) {
//            final var coroutine = lift(stateTMonad.put(cors)).getDelegate();
//            return new CoroutineTKind<>(coroutine);
//        }
//
//        // dequeue :: Monad m => CoroutineT r m ()
//        @NotNull
//        public CoroutineTKind<R, M, Unit> dequeue() {
//            return flatMap(getCCs(), this::dequeFirst);
//        }
//
//        @NotNull
//        private App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, Unit> dequeFirst(
//                @NotNull final List<CoroutineT<R, M, Unit>> cors
//        ) {
//            final List<CoroutineT<R, M, Unit>> rest;
//
//            if (cors.isEmpty()) {
//                return this.pure(Unit.UNIT);
//            } else if (cors.size() == 1) {
//                rest = new ArrayList<>(0);
//            } else {
//                rest = cors.stream().skip(1).collect(Collectors.toList());
//            }
//
//            final var first = new CoroutineTKind<>(cors.get(0));
//
//            return chain(putCCs(rest), first);
//        }
//
//        // enqueue :: Monad m => CoroutineT r m () -> CoroutineT r m ()
//        @NotNull
//        public CoroutineTKind<R, M, Unit> enqueue(@NotNull final CoroutineTKind<R, M, Unit> next) {
//            final var cor = next.getDelegate();
//            final var kind = flatMap(
//                    getCCs(),
//                    cors -> putCCs(alter(cors, e -> e.add(cor)))
//            );
//            return new CoroutineTKind<>(kind.getDelegate());
//        }
//
//        // yieldCo :: Monad m => CoroutineT r m ()
//        @NotNull
//        public CoroutineTKind<R, M, Unit> yieldCo() {
//            final var result = this.<Unit, Unit>callCC(this::yieldInCallCC);
//            return CoroutineTKind.narrowCoroutineT(result);
//        }
//
//        @Nullable
//        private App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, Unit> yieldInCallCC(
//                @NotNull final Function<
//                        ? super @NotNull Unit,
//                        ? extends @NotNull App<? extends ContTKind.mu<R, StateTKind.mu<List<CoroutineT<R, M, Unit>>, M>>, Unit>
//                        > kont
//        ) {
//            final var delegate = narrowCoroutineT(kont.apply(Unit.UNIT)).getDelegate();
//
//            final var next = new CoroutineTKind<>(delegate);
//            return chain(enqueue(next), dequeue());
//        }
//
//        // fork :: Monad m => CoroutineT r m () -> CoroutineT r m ()
//        @NotNull
//        public CoroutineTKind<R, M, Unit> forkCo(@NotNull final CoroutineTKind<R, M, Unit> cor) {
//            final var result = this.<Unit, Unit>callCC(
//                    k -> {
//                        final var delegate = narrowCoroutineT(k.apply(Unit.UNIT)).getDelegate();
//
//                        final var enqueue = enqueue(new CoroutineTKind<>(delegate));
//
//                        return chain(chain(enqueue, cor), dequeue());
//                    }
//            );
//            return CoroutineTKind.narrowCoroutineT(result);
//        }
//
//
//        // runCoroutineT :: Monad m => CoroutineT r m r -> m r
//        public App<? extends M, R> runCoroutine(@NotNull final CoroutineTKind<R, M, R> cor) {
//            final Function<CoroutineTKind<R, M, R>, StateTKind<List<CoroutineT<R, M, Unit>>, M, R> > second =
//                    ctk -> StateTKind.narrow(ctk.getDelegate().apply(stateTMonad::pure));
//
//            final Function<StateT.Result<List<CoroutineT<R, M, Unit>>, R>, R> valueProjection = StateT.Result::getValue;
//
//            final Function<StateTKind<List<CoroutineT<R, M, Unit>>, M, R>, App<? extends M, R> > third =
//                    st -> internalMonad
//                            .map(valueProjection)
//                            .apply(st.getDelegate().apply(new ArrayList<>()));
//
//            return third.compose(second).apply(cor);
//        }
//    }
//}
//
//final class Id<A> implements App<Id.mu, A> {
//    private final A value;
//
//    public Id(@NotNull final A value) {
//        this.value = value;
//    }
//
//    @NotNull
//    public final A getValue() {
//        return value;
//    }
//
//    @NotNull
//    public static <A> Id<A> narrow(@NotNull final App<? extends mu, A> kind) {
//        return (Id<A>) kind;
//    }
//
//    public static final class mu implements Monad.mu { }
//
//    @Override
//    public final String toString() {
//        return value.toString();
//    }
//
//    public enum IdMonad implements Monad<mu> {
//        INSTANCE;
//
//        @NotNull
//        @Override
//        public <A> App<Id.mu, A> pure(@NotNull final A a) {
//            return new Id<>(a);
//        }
//
//        @NotNull
//        @Override
//        public <A, B> App<? extends Id.mu, B> flatMap(
//                final @NotNull App<? extends Id.mu, @NotNull A> ma,
//                final @NotNull Function<? super @NotNull A, ? extends @NotNull App<? extends Id.mu, B>> aToMb
//        ) {
//            final var value = narrow(ma).getValue();
//            return aToMb.apply(value);
//        }
//    }
//}
//
//class Writer<A> {
//    private final String log;
//    private final A value;
//
//    Writer(@NotNull final String log, @NotNull final A value) {
//        this.log = log;
//        this.value = value;
//    }
//
//    public String getLog() { return log; }
//    public A getValue() { return value; }
//
//    @Override
//    public String toString() {
//        return "(" + log + ", " + value + ")";
//    }
//}
//
//class WriterK<A> implements App<WriterK.mu, A> {
//    private final Writer<A> delegate;
//
//    WriterK(@NotNull final Writer<A> delegate) { this.delegate = delegate; }
//
//    public Writer<A> getDelegate() { return delegate; }
//    public static<A> WriterK<A> narrow(@NotNull final App<? extends WriterK.mu, A> kind) { return (WriterK<A>) kind; }
//
//    public enum mu implements Monad.mu {}
//
//    public enum WriterMonad implements Monad<WriterK.mu> {
//        INSTANCE;
//
//        @Override
//        public @NotNull <A> App<WriterK.mu, A> pure(@NotNull final A a) { return new WriterK<>(new Writer<>("", a)); }
//
//        @Override
//        public @NotNull <A, B> App<WriterK.mu, B> flatMap(
//                final @NotNull App<? extends WriterK.mu, @NotNull A> ma,
//                final @NotNull Function<? super @NotNull A, ? extends @NotNull App<? extends WriterK.mu, B>> aToMb
//        ) {
//            final var wa = narrow(ma).getDelegate();
//            final var wb = narrow(aToMb.apply(wa.getValue())).getDelegate();
//
//            return new WriterK<>(new Writer<>(wa.getLog() + wb.getLog(), wb.getValue()));
//        }
//
//        public App<WriterK.mu, Unit> tell(@NotNull final String log) {
//            return new WriterK<>(new Writer<>(log, Unit.UNIT));
//        }
//    }
//}
