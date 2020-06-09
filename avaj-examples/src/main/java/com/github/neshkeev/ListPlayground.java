package com.github.neshkeev;

import com.github.neshkeev.avaj.App;
import com.github.neshkeev.avaj.data.List;
import com.github.neshkeev.avaj.data.List.Cons;
import com.github.neshkeev.avaj.data.kinds.ListKind;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.github.neshkeev.avaj.data.List.Cons.cons;
import static com.github.neshkeev.avaj.data.List.Nil.nil;

public class ListPlayground {
    public static void main(String[] args) {
        final List<@NotNull Integer> cons = cons(4, cons(2, nil()));
        final Cons<@NotNull Function<Integer, Double>> funcs = cons(Math::cos, cons(Math::sin, cons(Function.<Integer>identity().andThen(Integer::doubleValue), nil())));

        final ListKind.ListMonad monad = ListKind.ListMonad.INSTANCE;
        final ListKind<@NotNull Integer> kind = new ListKind<>(cons);
        final ListKind<@NotNull Function<Integer, Double>> funK = new ListKind<>(funcs);
        final ListKind<@NotNull Double> res = monad.flatMap(kind,
            a -> monad.flatMap(funK,
            f -> monad.pure(f.apply(a))
        ));
        System.out.println(res);

        final List<@NotNull Double> apply = monad.ap(monad.pure((Function<Integer, Double>) Math::cos))
                .andThen(ListKind::narrow)
                .andThen(ListKind::getDelegate)
                .apply(kind)
                ;
        System.out.println(apply);

        final Function<? super @NotNull App<ListKind.@NotNull mu, @NotNull Integer>, List<@NotNull Integer>> mapper =
                monad.<Integer, Integer>map(Math::incrementExact)
                        .andThen(ListKind::narrow)
                        .andThen(ListKind::getDelegate)
                ;
        System.out.println(mapper.apply(kind));
    }
}
