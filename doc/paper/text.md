TODO
- why HKT useful
- make better explanation of auxiliary class mu.
- explain why *Kind is separated from *Monad

# Lightweight higher-kinded polymorphism in java. Coroutines

> Any sufficiently advanced technology is indistinguishable from magic

Arthur C. Clarke

## Abstract

This paper demonstrates the higher-kinded polymorphism in java that makes it possible to define the notions of functors and monads in the way they are implemented in Haskell. The paper contains an implementation of the `Continuation` monad that exposes the `callCC` method as a part of its standard `API`. The semantics of `callCC` is the same as the semantics of `call/cc` in Lisp-like languages. The `Coroutine` monad is just a special case of the `Continuation` monad. Everything is implemented in pure java without any manipulation with the bytecode or `FFI`/`JNI`.

## Higher-Kinded polymorphism

### Motivation
Higher-kinded polymorphism (or higher-kinded types, HKT) is an abstraction over type constructors in Haskell and some other functional languages like `scala` and it's the cornerstone in implementing type classes like functors, monads. `Java` doesn't let a programmer to generalize over type constructors: it's illegal to write the following code:
```java
interface Applicative<F> {
    <A> F<A> pure(A value); // <<-- Error: Type 'F' does not have type parameters.
}
```
`java` doesn't know what the type parameter `F` is and fairly complains that `F` does not have type parameters.

In contrary `Haskell` makes it legal to write this code like this:
```haskell
class Applicative f
    pure :: a -> f a
```
Here `Haskell` due to its reach type system solves the type equations and figures that `f` must be a type parameter that accepts a type parameter. In other words `f` is a type constructor. In type theory it says that the kind of the type parameter `f` is `* -> *` (binary) whereas the kind of `a` is `*` (unary). The same piece of code can be implemented in `scala` like:
```scala
trait Applicative[F[_]] {
    def pure[A](x: A): F[A]
}
```
Here the programmer has to explicitly tell the compiler  that the `F` type parameter accepts one type parameter by writing `F[_]` (e.g. `F` is a type constructor).
Kinds of more arity are possible. For example, a key-value map has the kind `* -> * -> *` (ternary) because it needs the types of both the key and the value.

### Why HKT

HKT let the programmer to vary the type they use. For example, for the production environment the programmer can supply the real type and for testing a mock instance.

### Lightweight higher-kinded polymorphism
Jeremy Yallop and Leo White published the article called **"Lightweight higher-kinded polymorphism"** that shows how to bring the notion of HKT into languages where no HKT exists. They demonstrated the power of the proposed approach for OCaml but let's focus on java.

#### The App interface
The article introduces the following type:
```java
interface App<F, A> {}
```
This interface can be viewed in terms of Haskell like `F A` or in terms of scala like `F[A]`. This type denotes the idea that the type parameter `A` is applied to the type parameter `F`. Here both `F` and `A` are polymorphic. Using the `App` interface the code from above becomes legal in java if it is written like this:
```java
interface Applicative<F> {
    <A> App<F, A> pure(A value);
}
```
How does one convert `App<F, A>` and `F<A>` back and forth? `F<A>` needs to become a subtype of `App<F, A>` but the declaration is a bit tricky. Let's start with the simplest possible container: `Identity`. Semantically `Identity<T>` is equal to `T`:
```java
class Identity<T> implements App<Identity.mu, T> {
    T value;
    public Identity(T value) {
        this.value = value;
    }
    public static final class mu {}
}
```
Here the class `Identity.mu` is passed into `App` instead of passing `Identity` because `Identity` is generalized over `T` and `Identity.mu` is not generalized. Java inheritance rules allow to pass an instance of `Identity<T>` everywhere where `App<Identity.mu, T>` is expected. This defines the conversion from `F<A>` to `App<F, A>`.

The conversion from `App<F, A>` to `F<A>` is done by performing a downcast:
```java
class Identity<T> extends App<Identity.mu, T> {
    // the field and constructor omitted
    
    public static final class mu {}
    
    static<T> Identity<T> narrow(App<Identity.mu, T> value) {
        return (Identity<T>) value;
    }
}
```
The function `narrow` performs the downcast, and it is safe because `Identity.mu` is not defined and accessed from anywhere else but the `Identity` class.
Every polymorphic container has to define its own class `mu`.

By far the polymorphic container `Identity` can be used like this:
```java
App<Identity.mu, Integer> appIdInt = new Identity<>(42);
Identity<Integer> idContainer = Identity.narrow(appIdInt);
assert idContainer.value == 42;
```
It's enough to demonstrate the conversion back and forth between `F<A>` and `App<F, A>`

#### Real world implementations

**"Lightweight higher-kinded polymorphism"** already found its implementation in the JVM world in the projects **Arrow** and **DataFixerUpper**.

- **Arrow** is a general purpose functional-programming library that brings the notions of functors, monads and other category theory structures to kotlin.
- **DataFixerUpper** is a library that is used in minecraft to define profunctor-based category theory optics for data migration among versions of API.

## Ad hoc polymorphism

Interfaces in java play the same role as type classes do in Haskell or traits in scala. A java interface exposes a set of methods that can depend on each other, and their combinations can define some laws. Such a set of method is usually called an API and the behavior of the API depends on the implementors of the interface. The production implementation can perform *real* operations, and the implementation for unit tests can work with mocks. It's also called *ad hoc* polymorphism. The category theory structures like functors or monads are defined as type classes in Haskell. It happens that a monad requires to have a functor's structure inside, so let's focus on functor first.

### Functor
A functor in Haskell is a type class which exposes one function: `fmap :: (a -> b) -> (f a -> f b)`
```haskell
class Functor f where
    fmap :: (a -> b) -> (f a -> f b)
```
where `f` is a type constructor of the kind `* -> *`.

The `functor` interface in java looks like this:
```java
interface Functor<F> {
    <A, B> Function<App<F, A>, App<F, B>> map(Function<A, B> fun);
}
```
Here the name of the Haskell's function `fmap` was changed to the more appropriate name `map`. Conceptually, a functor is a generalized container, and there is a way to drag a regular function through the container applying it to each element in the container.

### Monad
A monad in Haskell is a type class which exposes two functions:
```haskell
class Functor m => Monad m where
    return :: a -> m a
    (>>=) :: m a -> (a -> m b) -> m b
```
here the `return` function trivially packs a regular value into a container. The `>>=` function (pronounced 'bind') allows chaining monadic computations. Sometimes this function is also called `flatMap`.
Here is the `monad` interface in java:
```java
interface Monad<M> extends Functor<M> {
    <A> App<M, A> pure(A a);
    <A, B> App<M, B> flatMap(App<M, A> ma, Function<A, App<M, B>> aToMb);
}
```
Here the name of the Haskell's function `return` was changed to `pure` since return is a `keyword` in java. The name of the function `>>=` was changed to `flatMap` as it's more familiar to java engineers.

### Examples

#### Identity Monad
Let's take a look at the most simplistic monad of all: `Identity`.

```java
class Identity<T> implements App<Identity.mu, T> {
    T value;

    public Identity(T value) {
        this.value = value;
    }

    public static final class mu { }

    static <T> Identity<T> narrow(App<Identity.mu, T> value) {
        return (Identity<T>) value;
    }
}

enum IdentityMonad implements Monad<Identity.mu> {
    INSTANCE;

    @Override
    public <A> App<Identity.mu, A> pure(A a) {
        return new Identity<>(a);
    }

    @Override
    public <A, B> Function<App<Identity.mu, A>, App<Identity.mu, B>> map(Function<A, B> fun) {
        return (App<Identity.mu, A> fa) -> {
            A a = Identity.narrow(fa).value;
            B b = fun.apply(a);
            return new Identity<>(b);
        };
    }

    @Override
    public <A, B> App<Identity.mu, B> flatMap(
            App<Identity.mu, A> ma,
            Function<A, App<Identity.mu, B>> aToMb) {
        A a = Identity.narrow(ma).value;
        return aToMb.apply(a);
    }
}
```
Here `IdentityMonad` is separated from the `Identity` itself, because (why?). The following example demonstrates how to use the `IdentityMonad`:
```java
public class Main {
    public static void main(String[] args) {
        IdentityMonad m = IdentityMonad.INSTANCE;
        App<Identity.mu, String> app = m.flatMap(m.pure(5),
            currValue -> m.flatMap(m.pure("+".repeat(currValue)),
            currString -> m.pure("The string '" + currString + "' was obtained by repeating '+' " + currValue + " times")
        ));
        Identity<String> result = Identity.narrow(app).value;
        System.out.println(result.value);
    }
}
```
Execution:
```bash
$ javac Main.java && java Main
The string '+++++' was obtained by repeating '+' 5 times
```
Here is how to read the code above:

1. The monadic computation above starts by lifting `5` to the `Identity` container by `m.pure(5)`;
2. `IdentityMonad` calls `flatMap` on two arguments `m.pure(5)` and the first lambda that starts with `currValue ->...`. Here `currValue` contains `5`, the current value inside of the container;
3. The first lambda calls `flatMap` on two arguments `m.pure("$".repeat(currValue))` and the second lambda that starts with `currString -> ...`. Here `currString` contains a sequence of `"$"` repeated `currValue` times.
4. The second lambda has access to both `currValue` and `currString`, it uses them to build the result string. The resulted string is lifted into the `Identity` container using `m.pure(...)`;
5. The `app` variable is of `App<Identity.mu, String>` type and needed to be converted to `Identity<String>` because the `App` interface does not expose any API to work with;
6. The conversion of `app` to `Identity<String>` is performed by `Identity::narrow` and stored into the `result` variable;
7. `Identity` has the `value` field which contains the result string of the monadic computation. Printing `value` outputs the string that was built in the 4-th step.

Unfortunately, java doesn't have any language construction that resembles `do-notation` from Haskell or `for comprehension` in scala, so there have to be nested `flatMap` calls to achieve the same effect.

Here is the equivalent code in Haskell for references that relies on do-notation.
```haskell
import Control.Monad.Identity

main = print $ runIdentity $ do
  currValue <- return 5
  currString <- return $ concat (replicate currValue "+")
  return $ "The string '" ++ currString ++ "' was obtained by repeating '+' " ++ (show currValue) ++ " times"
```
Execution
```bash
$ ghc Main.hs && ./Main
[1 of 1] Compiling Main             ( Main.hs, Main.o )
Linking Main ...
"The string '+++++' was obtained by repeating '+' 5 times"
```