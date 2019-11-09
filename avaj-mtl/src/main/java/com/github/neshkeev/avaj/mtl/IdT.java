package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class IdT<M extends Monad.mu, A> implements App<IdT.mu<M>, A> {
    private final App<M, A> value;

    public IdT(@NotNull final App<M, A> value) {
        this.value = value;
    }

    @NotNull
    public final App<M, A> getValue() {
        return value;
    }

    @NotNull
    public static<M extends Monad.mu, A> IdT<M, A> narrow(@NotNull final App<IdT.mu<M>, A> kind) {
        return (IdT<M, A>) kind;
    }

    public static final class mu<M> implements Monad.mu { }

    public static final class IdTMonad<M extends Monad.mu> implements Monad<mu<M>>, MonadTrans<mu<M>> {
        private final Monad<M> internalMonad;

        public IdTMonad(@NotNull final Monad<M> internalMonad) {
            this.internalMonad = internalMonad;
        }

        @NotNull
        @Override
        public <A> App<IdT.mu<M>, A> pure(@NotNull final A a) {
            return new IdT<>(internalMonad.pure(a));
        }

        @NotNull
        @Override
        public <Mon extends mu, A> App<? extends mu, A> lift(@NotNull final App<Mon, A> m) {
            return new IdT<>(m);
        }

        @NotNull
        @Override
        public <A, B> App<IdT.mu<M>, B> flatMap(
                @NotNull final App<IdT.mu<M>, A> ma,
                @NotNull final Function<@NotNull A, ? extends @NotNull App<IdT.mu<M>, B>> aToMb
        ) {
            final App<M, A> value = IdT.narrow(ma).getValue();
            final App<M, B> mbApp = internalMonad.flatMap(
                    value,
                    aToMb.andThen(e -> IdT.narrow(e)).andThen(IdT::getValue)
            );
            return new IdT<>(mbApp);
        }
    }
}
