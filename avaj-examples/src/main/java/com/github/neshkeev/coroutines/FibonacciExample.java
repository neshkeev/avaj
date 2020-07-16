package com.github.neshkeev.coroutines;

import com.github.neshkeev.avaj.Unit;
import com.github.neshkeev.avaj.data.StringMonoid;
import com.github.neshkeev.avaj.mtl.Id;
import com.github.neshkeev.avaj.mtl.WriterKind;
import com.github.neshkeev.avaj.mtl.WriterTKind;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FibonacciExample {

    public static void main(String[] args) {
        final var m = getCoroutineMonad();
        final var coroutine = m.flatMap(
                m.fork(m.replicateM_(14, sep("|"))), __ ->
                fib(0, 1)
        );

        System.out.println(WriterKind.narrow(WriterTKind.narrow(m.runCoroutine(coroutine))).getWriter().getLog());
    }

    private static CoroutineTKind<@NotNull Unit, WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>, @NotNull Unit> fib(
            final int a, final int b
    ) {
        final var m = getCoroutineMonad();
        if (a > 200) return m.yield();
        return m.flatMap(
                printOne(a), _l -> m.flatMap(
                m.yield(), _r ->
                fib(b, a + b)
        ));
    }

    private static CoroutineTKind<@NotNull Unit, WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>, @NotNull Unit> sep(
            @NotNull final String separator
    ) {
        final var m = getCoroutineMonad();
        return m.flatMap(
                printOne(separator), __ ->
                m.yield()
        );
    }

    @NotNull
    private static CoroutineTKind.CoroutineTMonad<@NotNull Unit, WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>> getCoroutineMonad() {
        return new CoroutineTKind.CoroutineTMonad<>(new WriterKind.WriterMonad<>(StringMonoid.INSTANCE));
    }

    private static CoroutineTKind<@NotNull Unit, WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>, @NotNull Unit> printOne(
            @NotNull final Object c
    ) {
        final var w = new WriterKind.WriterMonad<>(StringMonoid.INSTANCE);
        final CoroutineTKind.CoroutineTMonad<@NotNull Unit, WriterTKind.@NotNull mu<@NotNull String, Id.@NotNull mu>> m = new CoroutineTKind.CoroutineTMonad<>(w);

        return m.lift(w.tell(Objects.toString(c)));
    }
}
