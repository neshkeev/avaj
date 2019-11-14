package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

// * -> (* -> *) -> * -> *
// s -> m (a, s)
public interface StateT<S, M extends Monad.mu, A> extends Function<S, App<M, StateT.Result<@NotNull S, @NotNull A>>> {

    public final class Result<S, A> {
        private final A value;
        private final S state;

        public Result(@NotNull final A value, @NotNull final S state) {
            this.value = value;
            this.state = state;
        }

        @NotNull
        public final A getValue() { return value; }

        @NotNull
        public final S getState() { return state; }

        @Override
        public String toString() {
            return Objects.toString(value);
        }
    }
}
