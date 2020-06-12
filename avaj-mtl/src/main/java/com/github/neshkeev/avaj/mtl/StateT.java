package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

// * -> (* -> *) -> * -> *
// s -> m (a, s)
public interface StateT<S extends @NotNull Object, M extends @NotNull Object & Monad.mu, A extends @NotNull Object>
        extends Function<S, @NotNull App<M, StateT.@NotNull Result<S, A>>> {

    final class Result<S extends @NotNull Object, A extends @NotNull Object> {
        private final A value;
        private final S state;

        public Result(final A value, final S state) {
            this.value = value;
            this.state = state;
        }

        @NotNull
        public final A getValue() { return value; }

        @NotNull
        public final S getState() { return state; }

        @Override
        public String toString() {
            return String.format("(%s, %s)", state, value);
        }
    }
}
