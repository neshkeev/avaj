package com.github.neshkeev.avaj.data;

import org.jetbrains.annotations.NotNull;

public abstract class Either<L extends @NotNull Object, R extends @NotNull Object> {

    // sealed
    private Either() {}

    enum SIDE {
        LEFT, RIGHT
    }
    public abstract L left();
    public abstract R right();

    SIDE part() { return isRight() ? SIDE.RIGHT : SIDE.LEFT; }

    public boolean isLeft() { return !isRight(); }
    public boolean isRight() { return !isLeft(); }

    public static final class Left<L extends @NotNull Object, R extends @NotNull Object> extends Either<L, R> {
        private final L value;

        public Left(final L value) { this.value = value; }

        @Override
        public L left() { return value; }

        @Override
        public R right() { throw new UnsupportedOperationException("right is not implemented"); }

        @Override
        public boolean isLeft() { return true; }
    }

    public static final class Right<L extends @NotNull Object, R extends @NotNull Object> extends Either<L, R> {
        private final R value;

        public Right(final R value) { this.value = value; }

        @Override
        public L left() { throw new UnsupportedOperationException("left is not implemented"); }

        @Override
        public R right() { return value; }

        @Override
        public boolean isLeft() { return false; }
    }
}
