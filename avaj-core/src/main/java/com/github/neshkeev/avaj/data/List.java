package com.github.neshkeev.avaj.data;

import com.github.neshkeev.avaj.typeclasses.Monoid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.stream.Stream;

import static com.github.neshkeev.avaj.data.List.Cons.cons;
import static com.github.neshkeev.avaj.data.List.Nil.nil;

public abstract class List<T extends @NotNull Object> {
    private List() {}
    @Contract(value = "-> !null", pure = true)
    @NotNull
    public abstract T head();
    @Contract(value = "-> !null", pure = true)
    public abstract @NotNull List<T> tail();
    @Contract(pure = true)
    public abstract boolean isEmpty();

    @Contract(value = "_ -> !null", pure = true)
    @NotNull
    public abstract List<T> merge(@NotNull final List<T> right);

    @SafeVarargs
    @Contract(value = "_ -> !null", pure = true)
    @NotNull
    public static<T extends @NotNull Object> List<T> of(final T @NotNull ... el) {
        if (el.length == 0) return nil();
        return of(Stream.of(el).iterator());
    }

    public static<T extends @NotNull Object> List<T> of(@NotNull final Iterator<T> iterator) {
        if (!iterator.hasNext()) return nil();
        return cons(iterator.next(), of(iterator));
    }

    public static final class Nil<T extends @NotNull Object> extends List<T> {
        private Nil() { }

        @Override
        public T head() { throw new UnsupportedOperationException("head for Nil is not defined"); }

        @Override
        public @NotNull List<T> tail() { throw new UnsupportedOperationException("tail for Nil is not defined"); }

        @Contract(value = "-> true", pure = true)
        @Override
        public boolean isEmpty() {
            return true;
        }

        public static<T extends @NotNull Object> Nil<T> nil() { return new Nil<>(); }

        @Override
        @NotNull
        public List<T> merge(@NotNull final List<T> right) {
            return right;
        }

        @Override
        public String toString() {
            return "nil";
        }
    }
    public static final class Cons<T extends @NotNull Object> extends List<T> {
        private final T head;
        private final List<T> tail;

        private Cons(final T head, @NotNull final List<T> tail) {
            this.head = head;
            this.tail = tail;
        }
        public static<T extends @NotNull Object> Cons<T> cons(final T value, List<T> list) {
            return new Cons<>(value, list);
        }

        @Override
        public T head() {
            return head;
        }

        @Override
        public @NotNull List<T> tail() {
            return tail;
        }

        @Contract(value = "-> false", pure = true)
        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        @NotNull
        public List<T> merge(@NotNull final List<T> right) {
            return cons(head, tail.merge(right));
        }

        @Override
        public String toString() {
            return head + ", " + tail.toString();
        }
    }

    public static final class ListMonoid<T extends @NotNull Object> implements Monoid<@NotNull List<T>> {
        @Override
        @NotNull
        public List<T> empty() {
            return nil();
        }

        @Override
        @NotNull
        public List<T> concat(@NotNull final List<T> left, @NotNull final List<T> right) {
            return left.merge(right);
        }
    }
}
