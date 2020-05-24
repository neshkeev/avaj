package com.github.neshkeev;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.Monad;
import org.jetbrains.annotations.NotNull;

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

        final ParserKind<? extends @NotNull String> narrow = ParserKind.narrow(kind);
        Stream.of("", null, "12", "1", "123")
                .map(narrow.getDelegate())
                .forEach(System.out::println);

        final var kind1 = pkMonad.flatMap(digit(),
                c1 -> pkMonad.flatMap(digit(),
                c2 -> pkMonad.pure(c1 * c2))
        );

        Stream.of("", null, "62", "1", "723", "ab", "uu", "uub", "xcbdje")
                .map(ParserKind.narrow(kind1).getDelegate())
                .forEach(System.out::println);

    }
    public static ParserKind<@NotNull Character> anyChr() {
        final Function<? super String, ? extends Optional<? extends Result<? extends @NotNull Character>>> prs =
                s -> {
                    if (s == null || s.isBlank()) return Optional.empty();
                    else return Optional.of(new Result<>(s.charAt(0), s.substring(1)));
                };
        return new ParserKind<>(prs);
    }

    public static ParserKind<@NotNull Integer> digit() {
        final var parserMonad = ParserKind.Instance.INSTANCE;

        final var kind = parserMonad.flatMap(anyChr(),
                c -> {
                    if (Character.isDigit(c)) return parserMonad.pure(Character.digit(c, 10));
                    else return new ParserKind<>(s -> Optional.empty());
                });

        return ParserKind.narrow(kind);
    }
}


class Result<T extends @NotNull Object> {
    private final @NotNull T res;
    private final @NotNull String rest;

    Result(final T res, @NotNull final String rest) {
        this.res = res;
        this.rest = rest;
    }

    @NotNull
    public T getRes() {
        return res;
    }

    @NotNull
    public String getRest() {
        return rest;
    }

    @Override
    public String toString() {
        return res.toString();
    }
}

class ParserKind<T extends @NotNull Object> implements App<ParserKind.@NotNull mu, T> {

    @NotNull
    private final Function<? super @NotNull String, ? extends Optional<? extends Result<? extends T>>> delegate;

    ParserKind(@NotNull final Function<? super @NotNull String, ? extends Optional<? extends Result<? extends T>>> delegate) {
        this.delegate = delegate;
    }

    public static <T extends @NotNull Object> ParserKind<T> narrow(App<ParserKind.@NotNull mu, T> kind) { return (ParserKind<T>) kind; }

    @NotNull
    public Function<? super String, ? extends Optional<? extends Result<? extends T>>> getDelegate() { return delegate; }

    public static final class mu implements Monad.mu { }

    public enum Instance implements Monad<@NotNull mu> {
        INSTANCE;

        @Override
        public <A extends @NotNull Object> @NotNull App<ParserKind.@NotNull mu, A> pure(final A a) {
            final Function<? super String, ? extends Optional<? extends Result<? extends A>>> parser =
                    s -> Optional.of(new Result<>(a, s));

            return new ParserKind<>(parser);
        }

        @Override
        public <A extends @NotNull Object, B extends @NotNull Object> @NotNull App<ParserKind.@NotNull mu, B> flatMap(
                @NotNull final App<ParserKind.@NotNull mu, A> ma,
                @NotNull final Function<? super A, ? extends @NotNull App<ParserKind.@NotNull mu, B>> aToMb
        ) {
            final Function<? super String, ? extends Optional<? extends Result<? extends A>>> fst = ParserKind.narrow(ma).getDelegate();

            return new ParserKind<>(
                    s -> {
                        final Optional<? extends Result<? extends A>> fstR = fst.apply(s);

                        return fstR.flatMap(
                                result -> {
                                    final A res = result.getRes();
                                    final String rest = result.getRest();

                                    return narrow(aToMb.apply(res)).getDelegate().apply(rest);
                                }
                        );
                    }
            );
        }
    }
}
