package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public final class IdT<M extends @NotNull Object & Monad.mu, A extends @NotNull Object> implements App<IdT.@NotNull mu<M>, A> {
    @NotNull
    private final App<M, A> value;

    public IdT(@NotNull final App<M, A> value) {
        this.value = value;
    }

    @NotNull
    public final App<M, A> getValue() {
        return value;
    }

    @NotNull
    public static<M extends @NotNull Object & Monad.mu, A extends @NotNull Object> IdT<M, A> narrow(
            @NotNull final App<IdT.@NotNull mu<M>, A> kind
    ) {
        return (IdT<M, A>) kind;
    }

    public static final class mu<M extends @NotNull Object & Monad.mu> implements Monad.mu { }

    public static final class IdTMonad<M extends @NotNull Object & Monad.mu> implements Monad<@NotNull mu<M>>, MonadTrans<@NotNull mu<M>, M> {
        private final Monad<M> internalMonad;

        public IdTMonad(@NotNull final Monad<M> internalMonad) {
            this.internalMonad = internalMonad;
        }

        @NotNull
        @Override
        public <A extends @NotNull Object> App<IdT.@NotNull mu<M>, A> pure(@NotNull final A a) {
            return new IdT<>(internalMonad.pure(a));
        }

        @Override
        public @NotNull <A extends @NotNull Object> App<IdT.@NotNull mu<M>, A> lift(@NotNull final App<M, A> m) {
            return new IdT<>(m);
        }

        @Override
        public <A extends @NotNull Object, B extends @NotNull Object> @NotNull App<IdT.@NotNull mu<M>, B> flatMap(
                @NotNull final App<IdT.@NotNull mu<M>, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<IdT.@NotNull mu<M>, B>> aToMb
        ) {
            final App<M, A> value = IdT.narrow(ma).getValue();
            final App<M, B> mbApp = internalMonad.flatMap(
                    value,
                    aToMb.andThen(IdT::narrow).andThen(IdT::getValue)
            );
            return new IdT<>(mbApp);
        }
    }
}
