package com.github.neshkeev;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

public class Covariance {
    public static void main(String[] args) {
        final Reader<Integer, Integer> reader = Reader.reader(Math::abs);
        final ReaderKind<Integer, Integer> r = new ReaderKind<>(reader);
        final var rFunctor = new ReaderKind.ReaderApplicative<Integer>();
        final var mapped = ReaderTKind.narrow(rFunctor.map("e"::repeat).apply(r)).getDelegate();
        System.out.println(IdKind.narrow(mapped.apply(-5)).getValue());
        System.out.println(IdKind.narrow(mapped.apply(-15)).getValue());
        System.out.println(IdKind.narrow(mapped.apply(1)).getValue());
    }
}

interface K1 {}
interface Kind<F extends K1, A> {}

interface Functor<F extends Functor.mu> {
    @NotNull
    <A, B> Function<? super Kind<? extends F, ? extends A>, ? extends Kind<? extends F, ? extends B>> map(@NotNull final Function<? super A, ? extends B> fab);

    interface mu extends K1 { }
}

interface Applicative<F extends Applicative.mu> extends Functor<F> {
    @NotNull
    <A> Kind<? extends F, ? extends A> pure(@NotNull final A a);

    @NotNull
    <A, B> Function<? super Kind<? extends F, ? extends A>, ? extends Kind<? extends F, ? extends B>> ap(
            @NotNull final Kind<? extends F, ? extends Function<? super A, ? extends B>> fab
    );
    interface mu extends Functor.mu { }
}

interface ReaderT<R, F extends Functor.mu, A> extends Function<R, Kind<? extends F, ? extends A>> { }

class ReaderTKind<R, F extends Functor.mu, A> implements Kind<ReaderTKind.mu<R, F>, A> {
    private final ReaderT<R, F, A> delegate;

    public ReaderTKind(@NotNull final ReaderT<R, F, A> delegate) { this.delegate = delegate; }

    @NotNull public ReaderT<R, F, A> getDelegate() { return delegate; }
    @NotNull
    public static<R, F extends Functor.mu, A> ReaderTKind<? super R, F, ? extends A> narrow(
            @NotNull final Kind<? extends ReaderTKind.mu<R, F>, ? extends A> kind
    ) {
        return (ReaderTKind<? super R, F, ? extends A>) kind;
    }

    public interface mu<R, F extends Functor.mu> extends Functor.mu { }
    public static class ReaderTFunctor<R, F extends Functor.mu> implements Functor<mu<R, F>> {
        private final Functor<F> internalFunctor;

        public ReaderTFunctor(@NotNull final Functor<F> internalFunctor) { this.internalFunctor = internalFunctor; }

        @NotNull
        @Override
//        public <A, B> Function<? super Kind<? extends ReaderTKind.mu<R, F>, ? extends A>, ? extends Kind<? extends ReaderTKind.mu<R, F>, ? extends B>> map(
        public <A, B> Function<
                ? super Kind<? extends ReaderTKind.mu<R, F>, ? extends A>,
                ? extends Kind<? extends ReaderTKind.mu<R, F>, ? extends B>> map(
                @NotNull final Function<? super A, ? extends B> fab) {
            return fak -> {
                final ReaderT<? super R, F, ? extends A> fa = narrow(fak).getDelegate();
                return new ReaderTKind<>(
                        r -> {
                            final Kind<? extends F, ? extends A> apply = fa.apply(r);
                            return internalFunctor.map(fab).apply(apply);
                        }
                );
            };
        }
    }
}

interface Reader<R, A> extends ReaderT<R, IdKind.mu, A> {
    static <R, A> Reader<R, A> reader(@NotNull final Function<? super R, ? extends A> from) {
        return r -> IdKind.IdFunctor.INSTANCE.pure(from.apply(r));
    }
}

class ReaderKind<R, A> extends ReaderTKind<R, IdKind.mu, A> {

    public ReaderKind(@NotNull Reader<R, A> delegate) {
        super(delegate);
    }

    public static class ReaderApplicative<R> extends ReaderTKind.ReaderTFunctor<R, IdKind.mu> {

        public ReaderApplicative() {
            super(IdKind.IdFunctor.INSTANCE);
        }

        @Override
        public @NotNull <A, B> Function<
                ? super Kind<? extends ReaderTKind.mu<R, IdKind.mu>, ? extends A>,
                ? extends Kind<? extends ReaderTKind.mu<R, IdKind.mu>, ? extends B>
                > map(@NotNull final Function<? super A, ? extends B> fab
        ) {
            return fa -> {
                final Kind<? extends ReaderTKind.mu<R, IdKind.mu>, ? extends B> apply = super.map(fab).apply(fa);
                return apply;
            };
        }
    }

}

class IdKind<A> implements Kind<IdKind.mu, A> {
    private final A value;

    public IdKind(@NotNull final A value) { this.value = value; }

    public A getValue() { return value; }

    public static<A> IdKind<? extends A> narrow(@NotNull final Kind<? extends IdKind.mu, ? extends A> kind) { return (IdKind<? extends A>) kind; }

    public interface mu extends Applicative.mu {}

    public enum IdFunctor implements Applicative<mu> {
        INSTANCE;

        @Override
        public @NotNull <A, B> Function<? super Kind<? extends IdKind.mu, ? extends A>, ? extends Kind<? extends IdKind.mu, ? extends B>> map(
                @NotNull final Function<? super A, ? extends B> fab
        ) {
            return fa -> {
                final A value = narrow(fa).getValue();
                final B b = fab.apply(value);
                return new IdKind<>(b);
            };
        }

        @Override
        public <A> Kind<? extends IdKind.mu, ? extends A> pure(@NotNull A a) {
            return new IdKind<>(a);
        }

        @Override
        public <A, B> Function<? super Kind<? extends IdKind.mu, ? extends A>, ? extends Kind<? extends IdKind.mu, ? extends B>> ap(@NotNull Kind<? extends IdKind.mu, ? extends Function<? super A, ? extends B>> fab) {
            return null;
        }
    }
}

