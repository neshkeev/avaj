package org.sample.typeclass;

import java.util.function.Function;

public class ReaderPlayground {
    public static void main(String[] args) {
        final var identityMonad = Identity.Instance.INSTANCE;
        final Function<Double, Kind<Identity.mu, Double>> pure = identityMonad::pure;

        final var reader = ReaderT.reader(Math::cos, pure);

        final var muDoubleKind = identityMonad.flatMap(reader.apply(0.0), identityMonad::pure);

        System.out.println(Identity.narrow(muDoubleKind).getValue());
    }
}
