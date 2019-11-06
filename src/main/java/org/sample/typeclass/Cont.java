package org.sample.typeclass;

import java.util.function.Function;

public interface Cont<R, A> extends Function<Function< A,  R>, R> {
}

class ContKind<R, A> implements Kind<ContKind.mu<R>, A> {
    private final Cont<R, A> delegate;
    ContKind(final Cont<R, A> delegate) {
        this.delegate = delegate;
    }

    public final Cont<R, A> getDelegate() {
        return delegate;
    }
    public static <R, A> ContKind<R, A> narrow(Kind< mu<R>,  A> kind) {
        return (ContKind<R, A>) kind;
    }

    public static class mu<R> { }

    public static final class Instance<R> implements Monad<ContKind.mu<R>> {

        @Override
        public <A> Kind< mu<R>,  A> pure(A a) {
            final Cont<R, A> cont = c -> c.apply(a);
            return new ContKind<>(cont);
        }

        @Override
        public <A, B> Kind<mu<R>,  B> flatMap(
                final Kind< mu<R>,  A> ma,
                final Function< A,  Kind< mu<R>,  B>> aToMb
        ) {
            final Cont<R, B> result = brr -> {
                final Function<A, R> far = a -> {
                    final var kcbr = ContKind.narrow(aToMb.apply(a));
                    final var cbr = kcbr.getDelegate();
                    return cbr.apply(brr);
                };
                return ContKind.narrow(ma).getDelegate().apply(far);
            };
            return new ContKind<>(result);
        }

        public static <A, B, R> ContKind<R, A> callCC(
                Function<Function<A, ContKind<R, B>>, ContKind<R, A>> aToRbToRa
        ) {
            return new ContKind<>(
                    ar -> {
                        final Function<A, ContKind<R, B>> fabr = a -> new ContKind<>(br -> ar.apply(a));
                        return aToRbToRa.apply(fabr).getDelegate().apply(ar);
                    }
            );
        }
    }

}
