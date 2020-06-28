package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import com.github.neshkeev.avaj.typeclasses.Monoid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class WriterTKind<
        W extends @NotNull Object,
        M extends @NotNull Object & Monad.mu,
        A extends @NotNull Object
    > implements App<WriterTKind.@NotNull mu<W, M>, A> {

    @NotNull private final App<M, @NotNull Writer<W, A>> delegate;

    public WriterTKind(@NotNull final App<M, @NotNull Writer<W, A>> delegate) { this.delegate = delegate; }

    public static final class Writer<W extends @NotNull Object, A extends @NotNull Object> {
        final W log;
        final A value;

        public Writer(@NotNull final W log, final A value) {
            this.log = log;
            this.value = value;
        }

        @NotNull
        public W getLog() { return log; }

        public final A getValue() { return value; }

        @Override
        public String toString() { return String.format("(%s,%s)", log, value); }
    }

    @NotNull public App<M, @NotNull Writer<W, A>> getDelegate() { return delegate; }

    public static final class mu<
            W extends @NotNull Object,
            M extends @NotNull Object & Monad.mu
            > implements Monad.mu {}

    @NotNull
    public static <
            W extends @NotNull Object,
            M extends @NotNull Object & Monad.mu,
            A extends @NotNull Object
        > WriterTKind<W, M, A> narrow(@NotNull final App<WriterTKind.@NotNull mu<W, M>, A> kind) {
        return (WriterTKind<W, M, A>) kind;
    }

    public static final class WriterTMonad<W extends @NotNull Object, M extends @NotNull Object & Monad.mu>
            implements MonadWriter<W, @NotNull mu<W, M>>, MonadTrans<@NotNull mu<W, M>, M>
    {
        @NotNull private final Monoid<W> monoid;
        @NotNull private final Monad<M> internalMonad;

        public WriterTMonad(@NotNull final Monoid<W> monoid, @NotNull final Monad<M> internalMonad) {
            this.monoid = monoid;
            this.internalMonad = internalMonad;
        }

        @Override
        public @NotNull App<WriterTKind.@NotNull mu<W, M>, @NotNull Unit> tell(final W a) {
            return new WriterTKind<>(internalMonad.pure(new Writer<>(a, Unit.UNIT)));
        }

        @Override
        public @NotNull <A extends @NotNull Object> WriterTKind<W, M, A> pure(final A a) {
            return new WriterTKind<>(internalMonad.pure(new Writer<>(monoid.empty(), a)));
        }

        @Override
        @NotNull
        public <A extends @NotNull Object, B extends @NotNull Object> WriterTKind<W, M, B> flatMap(
                @NotNull final App<WriterTKind.@NotNull mu<W, M>, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<WriterTKind.@NotNull mu<W, M>, B>> aToMb
        ) {
            final App<M, @NotNull Writer<W, A>> mwa = narrow(ma).getDelegate();
            return new WriterTKind<>(internalMonad.flatMap(mwa, wa -> {
                final App<M, @NotNull Writer<W, B>> mwb = narrow(aToMb.apply(wa.getValue())).getDelegate();
                return internalMonad.flatMap(mwb, wb -> internalMonad.pure(
                        new Writer<>(monoid.concat(wa.getLog(), wb.getLog()), wb.getValue())
                ));
            }));
        }

        @Override
        public @NotNull <A extends @NotNull Object> WriterTKind<W, M, A> lift(@NotNull final App<M, A> m) {
            return new WriterTKind<>(
                    internalMonad.flatMap(m,
                            x -> internalMonad.pure(
                            new Writer<>(monoid.empty(), x)
            )));
        }
    }
}
