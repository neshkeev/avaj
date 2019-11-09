package com.github.neshkeev;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

import java.nio.file.WatchEvent;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class Parser {
    public static void main(String[] args) {
        final var pkMonad = ParserKind.Instance.INSTANCE;

        final var kind = pkMonad.flatMap(anyChr(),
                c1 -> pkMonad.flatMap(anyChr(),
                c2 -> pkMonad.pure(c1 + "" + c2))
        );

        final ParserKind<? extends String> narrow = ParserKind.narrow(kind);
        Stream.of("", null, "12", "1", "123")
                .map(narrow.getDelegate())
                .forEach(System.out::println);
                ;

        final var kind1 = pkMonad.flatMap(digit(),
                c1 -> pkMonad.flatMap(digit(),
                c2 -> pkMonad.pure(c1 * c2))
        );

        Stream.of("", null, "62", "1", "723", "ab", "uu", "uub", "xcbdje")
                .map(ParserKind.narrow(kind1).getDelegate())
                .forEach(System.out::println);

    }
    public static ParserKind<Character> anyChr() {
        final Function<? super String, ? extends Optional<? extends Result<? extends Character>>> prs =
                s -> {
                    if (s == null || s.isBlank()) return Optional.empty();
                    else return Optional.of(new Result<>(s.charAt(0), s.substring(1)));
                };
        return new ParserKind<>(prs);
    }

    public static ParserKind<Integer> digit() {
        final var parserMonad = ParserKind.Instance.INSTANCE;

        final var kind = parserMonad.flatMap(anyChr(),
                c -> {
                    if (Character.isDigit(c)) return parserMonad.pure(Character.digit(c, 10));
                    else return new ParserKind<>(s -> Optional.empty());
                });

        return ParserKind.narrow(kind);
    }
}


class Result<T> {
    private final T res;
    private final String rest;

    Result(T res, String rest) {
        this.res = res;
        this.rest = rest;
    }

    public T getRes() {
        return res;
    }

    public String getRest() {
        return rest;
    }

    @Override
    public String toString() {
        return res.toString();
    }
}

class ParserKind<T> implements App<ParserKind.mu, T> {
    private final Function<? super String, ? extends Optional<? extends Result<? extends T>>> delegate;

    ParserKind(final Function<? super String, ? extends Optional<? extends Result<? extends T>>> delegate) {
        this.delegate = delegate;
    }

    public static <T> ParserKind<T> narrow(App<ParserKind.mu, T> kind) {
        return (ParserKind<T>) kind;
    }

    public Function<? super String, ? extends Optional<? extends Result<? extends T>>> getDelegate() {
        return delegate;
    }

    public static final class mu implements Monad.mu { }

    public enum Instance implements Monad<mu> {
        INSTANCE;

        @NotNull
        @Override
        public <A> App<ParserKind.mu, A> pure(@NotNull final A a) {
            final Function<? super String, ? extends Optional<? extends Result<? extends A>>> parser =
                    s -> Optional.of(new Result<>(a, s));

            return new ParserKind<>(parser);
        }

        @NotNull
        @Override
        public <A, B> App<ParserKind.mu, B> flatMap(
                @NotNull final App<ParserKind.mu, A> ma,
                @NotNull final Function<@NotNull A, ? extends @NotNull App<ParserKind.mu, B>> aToMb
        ) {
            final Function<? super String, ? extends Optional<? extends Result<? extends A>>> fst = ParserKind.narrow(ma).getDelegate();

            return new ParserKind<B>(
                    s -> {
                        final Optional<? extends Result<? extends A>> fstR = fst.apply(s);

                        return fstR.flatMap(
                                result -> {
                                    final A res = result.getRes();
                                    final String rest = result.getRest();

                                    final Optional<? extends Result<? extends B>> result1 =
                                            narrow(aToMb.apply(res)).getDelegate().apply(rest);
                                    return result1;
                                }
                        );
                    }
            );
        }
    }
}
