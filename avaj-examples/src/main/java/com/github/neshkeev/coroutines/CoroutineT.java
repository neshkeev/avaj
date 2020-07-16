package com.github.neshkeev.coroutines;

import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.data.List;
import com.github.neshkeev.avaj.mtl.ContT;
import com.github.neshkeev.avaj.mtl.StateTKind;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

// (a -> m r) -> m r
// (a -> StateT<List<CoroutineT<R, M, A>>, M, A>) -> StateT<List<CoroutineT<R, M, A>>, M, A>
// s -> m (a, s)
// StateT<List<CoroutineT<R, M, A>>, M, A> :: List<CoroutineT<R, M, A>> -> M<Result<List<CoroutineT<R, M, A>>, A>>
@FunctionalInterface
interface CoroutineT<
        R extends @NotNull Object,
        M extends @NotNull Object & Monad.mu,
        A extends @NotNull Object
    > extends ContT<
        R,
        StateTKind.@NotNull mu<@NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>, M>,
        A
    > {
}