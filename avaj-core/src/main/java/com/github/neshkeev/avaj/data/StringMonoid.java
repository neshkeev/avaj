package com.github.neshkeev.avaj.data;

import com.github.neshkeev.avaj.typeclasses.Monoid;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum StringMonoid implements Monoid<@NotNull String> {
    INSTANCE;

    @Contract(pure = true)
    @Override
    @NotNull
    public String empty() {
        return "";
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String concat(@NotNull final String left, @NotNull final String right) {
        return left + right;
    }
}
