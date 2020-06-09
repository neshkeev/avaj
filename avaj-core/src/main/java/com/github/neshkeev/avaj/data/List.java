package com.github.neshkeev.avaj.data;

import org.jetbrains.annotations.NotNull;

public abstract class List<T extends @NotNull Object> {
    private List() {}
    public abstract T head();
    public abstract @NotNull List<T> tail();

    @NotNull
    public abstract List<T> merge(@NotNull final List<T> right);

    public static final class Nil<T extends @NotNull Object> extends List<T> {
        private Nil() { }

        @Override
        public T head() { throw new UnsupportedOperationException("head for Nil is not defined"); }

        @Override
        public @NotNull List<T> tail() { throw new UnsupportedOperationException("tail for Nil is not defined"); }

        public static<T extends @NotNull Object> Nil<T> nil() { return new Nil<>(); }

        @Override
        @NotNull
        public List<T> merge(@NotNull List<T> right) {
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

        @Override
        @NotNull
        public List<T> merge(@NotNull List<T> right) {
            return cons(head, tail.merge(right));
        }

        @Override
        public String toString() {
            return head + ", " + tail.toString();
        }
    }
}
