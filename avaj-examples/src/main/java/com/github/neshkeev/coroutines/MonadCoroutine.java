package com.github.neshkeev.coroutines;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.data.List;
import com.github.neshkeev.avaj.mtl.ContTKind;
import com.github.neshkeev.avaj.mtl.MonadCont;
import com.github.neshkeev.avaj.mtl.StateTKind;
import com.github.neshkeev.avaj.typeclasses.Monad;
import com.github.neshkeev.avaj.typeclasses.MonadTrans;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface MonadCoroutine<
        R extends @NotNull Object,
        M extends @NotNull Object & Monad.mu
    > extends MonadCont<
        ContTKind.@NotNull mu<
                R,
                StateTKind.@NotNull mu<
                        @NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>
                        , M
                >
        >
    >, MonadTrans<
        ContTKind.@NotNull mu<
                R,
                StateTKind.@NotNull mu<
                        @NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>
                        , M
                >
        >
        , M
    >
{

    // yield :: Monad m => CoroutineT r m ()
    @Contract(value = "-> !null", pure = true)
    @NotNull CoroutineTKind<R, M, @NotNull Unit> yield();

    //fork :: Monad m => CoroutineT r m () -> CoroutineT r m ()
    @NotNull
    @Contract(value = "_ -> !null", pure = true)
    CoroutineTKind<R, M, @NotNull Unit> fork(
            @NotNull final App<
                    ContTKind.@NotNull mu<
                            R,
                            StateTKind.@NotNull mu<
                                    @NotNull List<@NotNull CoroutineT<R, M, @NotNull Unit>>,
                                    M
                                    >
                            >
                    , @NotNull Unit
                    > from
    );

    // runCoroutine :: Monad m => CoroutineT r m r => m r
    @Contract(value = "_ -> !null", pure = true)
    @NotNull App<M, R> runCoroutine(@NotNull final CoroutineTKind<R, M, R> cor);
}
