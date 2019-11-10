package com.github.neshkeev;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.data.kinds.ListKind;
import com.github.neshkeev.avaj.mtl.ContT;
import com.github.neshkeev.avaj.mtl.StateTKind;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ContTPlayground {
    public static void main(String[] args) {

    }
}

enum Unit { UNIT }

// newtype CoroutineT r m a = CoroutineT {runCoroutineT' :: ContT r (StateT [CoroutineT r m ()] m) a}
//    deriving (Functor,Applicative,Monad,MonadCont,MonadIO)

interface CoroutineT<R, M extends Monad.mu, A> extends
        ContT<R, StateTKind.mu<ListKind<CoroutineT<R, M, Unit>>, M>, A> {}

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
    }
}

