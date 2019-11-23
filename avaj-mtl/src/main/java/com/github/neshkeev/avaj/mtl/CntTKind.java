package com.github.neshkeev.avaj.mtl;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.typeclasses.cov.Monad;
import org.jetbrains.annotations.NotNull;

public class CntTKind<R, M extends Monad.mu, A> implements App<CntTKind.mu<R, M>, A>{

    private final ContT<R, M, A> delegate;

    public CntTKind(@NotNull final ContT<R, M, A> delegate) { this.delegate = delegate; }

    public ContT<R, M, A> getDelegate() { return delegate; }

    @NotNull
    public static <R, M extends Monad.mu, A> CntTKind<R, M, A> narrow( @NotNull final App<? extends CntTKind.mu<R, M>, A> kind ) { return (CntTKind<R, M, A>) kind; }

    public interface mu<R, M extends Monad.mu> extends Monad.mu { }
}
