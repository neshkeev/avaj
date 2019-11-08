package org.sample.cont.typeclass;

import java.util.function.Function;

interface K1 {};
interface App<F extends K1, A> extends K1 {}

interface Functor<F extends Functor.mu> {
    <A, B> Function<App<F, A>, App<F, B>> map(Function<A, B> f);

    interface mu extends K1 { }
}

interface Applicative<F extends Applicative.mu> extends Functor<F> {
    <A> App<F, A> pure(A a);
    <A, B> Function<App<F, A>, App<F, B>> ap(App<F, Function<A, B>> f);

    interface mu extends Functor.mu { }
}

interface Monad<M extends Monad.mu> extends Applicative<M> {
    <A, B> App<M, B> flatMap(App<M, A> ma, Function<A, App<M, B>> aToMb);

    @Override
    default <A, B> Function<App<M, A>, App<M, B>> map(Function<A, B> f) {
        return maK -> this.flatMap(maK, a -> this.pure(f.apply(a)));
    }

    @Override
    default <A, B> Function<App<M, A>, App<M, B>> ap(App<M, Function<A, B>> hfn) {
        return maK ->
                this.flatMap(maK,
                        a -> this.flatMap(hfn,
                                fn -> this.pure(fn.apply(a))
                        ));
    }

    interface mu extends Applicative.mu { }
}

interface ReaderT<R, M extends Monad.mu, A> extends Function<R, App<M, A>> { }

class ReaderTKind<R, M extends Monad.mu, A> implements App<ReaderTKind.mu<R, M>, A> {
    private final ReaderT<R, M, A> delegate;

    ReaderTKind(ReaderT<R, M, A> delegate) { this.delegate = delegate; }

    public ReaderT<R, M, A> getDelegate() { return delegate; }

    public static<R, M extends Monad.mu, A> ReaderTKind<R, M, A> narrow(App<ReaderTKind.mu<R, M>, A> kind) { return (ReaderTKind<R, M, A>) kind; }
    public static final class mu<R, M> implements Monad.mu {}

    public static final class ReaderMonad<R, M extends Monad.mu> implements Monad<mu<R, M>> {

        private final Monad<M> monad;

        public ReaderMonad(Monad<M> monad) {
            this.monad = monad;
        }


        @Override
        public <A> App<ReaderTKind.mu<R, M>, A> pure(A a) {
            final App<M, A> pure = monad.pure(a);
            ReaderT<R, M, A> reader = r -> pure;
            return new ReaderTKind<>(reader);
        }

        @Override
        public <A, B> App<ReaderTKind.mu<R, M>, B> flatMap(
                final App<ReaderTKind.mu<R, M>, A> ma,
                final Function<A, App<ReaderTKind.mu<R, M>, B>> aToMb
        ) {
            final ReaderT<R, M, A> rma = narrow(ma).getDelegate();
            return new ReaderTKind<R, M, B>(r -> {
                final App<M, A> maApp = rma.apply(r);
                final App<M, B> mbApp = monad.flatMap(maApp,
                        a -> {
                            final App<ReaderTKind.mu<R, M>, B> krmb = aToMb.apply(a);
                            final ReaderT<R, M, B> rmb = narrow(krmb).getDelegate();
                            return rmb.apply(r);
                        }
                );
                return mbApp;
            });
        }
    }
}

class Id<A> implements App<Id.mu, A> {
    private final A value;

    Id(A value) { this.value = value; }

    public A getValue() { return value; }
    public static <A> Id<A> narrow(App<Id.mu, A> kind) { return (Id<A>) kind; }

    public static final class mu implements Monad.mu { }

    @Override
    public String toString() {
        return value.toString();
    }

    public enum Instance implements Monad<mu> {
        INSTANCE;

        @Override
        public <A> App<Id.mu, A> pure(A a) {
            return new Id<>(a);
        }

        @Override
        public <A, B> App<Id.mu, B> flatMap(App<Id.mu, A> ma, Function<A, App<Id.mu, B>> aToMb) {
            final var value = narrow(ma).getValue();
            return aToMb.apply(value);
        }
    }
}
