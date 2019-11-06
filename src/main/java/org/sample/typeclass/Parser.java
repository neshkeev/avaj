package org.sample.typeclass;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class Parser {
    public static void main(String[] args) {
//        final var pkMonad = ParserKind.Instance.INSTANCE;
//
//        final var kind = pkMonad.flatMap(anyChr(),
//                c1 -> pkMonad.<Character, String>flatMap(anyChr(),
//                c2 -> pkMonad.pure(c1 + "" + c2))
//        );
//
//        final ParserKind<? extends String> narrow = ParserKind.narrow(kind);
//        Stream.of("", null, "12", "1", "123")
//                .map(narrow.getDelegate())
//                .forEach(System.out::println);
//                ;
//
//        final var kind1 = pkMonad.flatMap(digit(),
//                c1 -> pkMonad.<Integer, Integer>flatMap(digit(),
//                c2 -> pkMonad.pure(c1 * c2))
//        );
//
//        Stream.of("", null, "62", "1", "723", "ab", "uu", "uub", "xcbdje")
//                .map(ParserKind.narrow(kind1).getDelegate())
//                .forEach(System.out::println);

    }
//    public static ParserKind<Character> anyChr() {
//        final Function<? super String, ? extends Optional<? extends Result<? extends Character>>> prs =
//                s -> {
//                    if (s == null || s.isBlank()) return Optional.empty();
//                    else return Optional.of(new Result<>(s.charAt(0), s.substring(1)));
//                };
//        return new ParserKind<>(prs);
//    }
//
//    public static ParserKind<? extends Integer> digit() {
//        final var parserMonad = ParserKind.Instance.INSTANCE;
//
//        final Kind<? super ParserKind.mu, ? extends Integer> kind = parserMonad.flatMap(anyChr(),
//                c -> {
//                    if (Character.isDigit(c)) return parserMonad.pure(Character.digit(c, 10));
//                    else return new ParserKind<>(s -> Optional.empty());
//                });
//
//        return ParserKind.narrow(kind);
//    }
}
//
//class OptionalKind<T> implements Kind<OptionalKind.mu, T> {
//
//    private final Optional<T> delegate;
//
//    OptionalKind(final Optional<T> delegate) {
//        this.delegate = delegate;
//    }
//
//    public final Optional<T> getDelegate() {
//        return delegate;
//    }
//    public static <T> OptionalKind<? extends T> narrow(Kind<? super OptionalKind.mu, ? extends T> kind) {
//        return (OptionalKind<? extends T>) kind;
//    }
//
//    public static final class mu { }
//
//    public enum Instance implements Monad<mu> {
//        INSTANCE;
//
//        @Override
//        public <A> Kind<? super mu, ? extends A> pure(A a) {
//            return new OptionalKind<>(Optional.of(a));
//        }
//
//        @Override
//        public <A, B> Kind<? super mu, ? extends B> flatMap(
//                final Kind<? super mu, ? extends A> ma,
//                final Function<? super A, ? extends Kind<? super mu, ? extends B>> aToMb
//        ) {
//            final Function<? super Kind<? super mu, ? extends B>, ? extends OptionalKind<? extends B>> narrow = OptionalKind::narrow;
//
//            final Function<? super A, ? extends Optional<? extends B>> streamFunction =
//                    aToMb.andThen(narrow).andThen(OptionalKind::getDelegate);
//
//            final var optionalB = narrow(ma).getDelegate().flatMap(streamFunction);
//
//            return new OptionalKind<>(optionalB);
//        }
//    }
//}
//
//class Result<T> {
//    private final T res;
//    private final String rest;
//
//    Result(T res, String rest) {
//        this.res = res;
//        this.rest = rest;
//    }
//
//    public T getRes() {
//        return res;
//    }
//
//    public String getRest() {
//        return rest;
//    }
//
//    @Override
//    public String toString() {
//        return res.toString();
//    }
//}
//
//class ParserKind<T> implements Kind<ParserKind.mu, T> {
//    private final Function<? super String, ? extends Optional<? extends Result<? extends T>>> delegate;
//
//    ParserKind(final Function<? super String, ? extends Optional<? extends Result<? extends T>>> delegate) {
//        this.delegate = delegate;
//    }
//
//    public static <T> ParserKind<? extends T> narrow(Kind<? super ParserKind.mu, ? extends T> kind) {
//        return (ParserKind<? extends T>) kind;
//    }
//
//    public Function<? super String, ? extends Optional<? extends Result<? extends T>>> getDelegate() {
//        return delegate;
//    }
//
//    public static final class mu { }
//
//    public enum Instance implements Monad<ParserKind.mu> {
//        INSTANCE;
//
//        @Override
//        public <A> Kind<? super mu, ? extends A> pure(A a) {
//            final Function<? super String, ? extends Optional<? extends Result<? extends A>>> parser =
//                    s -> Optional.of(new Result<>(a, s));
//
//            return new ParserKind<>(parser);
//        }
//
//        @Override
//        public <A, B> Kind<? super mu, ? extends B> flatMap(
//                final Kind<? super mu, ? extends A> ma,
//                final Function<? super A, ? extends Kind<? super mu, ? extends B>> aToMb
//        ) {
//            final Function<? super String, ? extends Optional<? extends Result<? extends A>>> fst = ParserKind.narrow(ma).getDelegate();
//
//            return new ParserKind<B>(
//                    s -> {
//                        final Optional<? extends Result<? extends A>> fstR = fst.apply(s);
//
//                        return fstR.flatMap(
//                                result -> {
//                                    final A res = result.getRes();
//                                    final String rest = result.getRest();
//
//                                    final Optional<? extends Result<? extends B>> result1 =
//                                            narrow(aToMb.apply(res)).getDelegate().apply(rest);
//                                    return result1;
//                                }
//                        );
//                    }
//            );
//        }
//    }
//}
