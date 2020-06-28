package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.typeclasses.Monoid;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class WriterKind<W extends @NotNull Object, A extends @NotNull Object>
        extends WriterTKind<W, Id.@NotNull mu, A> {

    public WriterKind(@NotNull Writer<W, A> delegate) {
        super(Id.IdMonad.INSTANCE.pure(delegate));
    }

    @NotNull
    public static <W extends @NotNull Object, A extends @NotNull Object> WriterKind<W, A> narrow(
            WriterTKind<W, Id.@NotNull mu, @NotNull A> kind
    ) {
        return new WriterKind<>(Id.narrow(kind.getDelegate()).getValue());
    }

    public @NotNull Writer<W, A> getWriter() {
        return Id.narrow(getDelegate()).getValue();
    }

    public static final class WriterMonad<W extends @NotNull Object>
            implements MonadWriter<W, @NotNull mu<W, Id.@NotNull mu>> {
        private final WriterTMonad<W, Id.@NotNull mu> monad;

        public WriterMonad(@NotNull final Monoid<W> monoid) {
            this.monad = new WriterTMonad<>(monoid, Id.IdMonad.INSTANCE);
        }

        @Override
        @NotNull
        public App<WriterTKind.@NotNull mu<W, Id.@NotNull mu>, @NotNull Unit> tell(final W a) {
            return narrow(WriterTKind.narrow(monad.tell(a)));
        }

        @Override
        @NotNull
        public <A extends @NotNull Object> WriterKind<W, A> pure(final A a) {
            return narrow(WriterTKind.narrow(monad.pure(a)));
        }

        @Override
        public @NotNull
        <A extends @NotNull Object, B extends @NotNull Object> WriterKind<W, B> flatMap(
                @NotNull final App<WriterTKind.@NotNull mu<W, Id.@NotNull mu>, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<WriterTKind.@NotNull mu<W, Id.@NotNull mu>, B>> aToMb
        ) {
            return narrow(WriterTKind.narrow(monad.flatMap(ma, aToMb)));
        }
    }
}
