TODO
- why HKT useful
- make better explanation of auxiliary class mu.

# Lightweight higher-kinded polymorphism in java. Coroutines

> Any sufficiently advanced technology is indistinguishable from magic

Arthur C. Clarke

## Abstract

This paper demonstrates the higher-kinded polymorphism in java that makes it possible to define the notions of functors and monads in the way they are implemented in Haskell. The paper contains an implementation of the `Continuation` monad that exposes the `callCC` method as a part of its standard `API`. The semantics of `callCC` is the same as the semantics of `call/cc` in Lisp-like languages. The `Coroutine` monad is just a special case of the `Continuation` monad. Everything is implemented in pure java without any manipulation with the bytecode or `FFI`/`JNI`.

## Higher-Kinded polymorphism

### Definition
Higher-kinded polymorphism (or higher-kinded types, HKT) is an abstraction over type constructors in Haskell and some other functional languages like `scala` and it's a corner stone in implementing type classes like functors, monads. `Java` doesn't let a programmer to generalize over the type constructor: it's illegal to write the following code:
```java
interface Applicative<M> {
    <A> M<A> pure(A value); // <<-- Error: Type 'M' does not have type parameters.
}
```
`java` doesn't know what the type parameter `M` is and fairly complains that `M` does not have type parameters.

In contrary `Haskell` makes it legal to write the same code as this:
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
Kinds of more arity is possible. For example, a key-value map has the kind `* -> * -> *` (ternary) because it needs the types of both key and value.

### Why HKT

HKT let the programmer to vary the type they use. For example, for the production environment the programmer can supply the real type and for testing a mock instance.

### Lightweight higher-kinded polymorphism
Jeremy Yallop and Leo White published the article called "Lightweight higher-kinded polymorphism" that shows how to bring the notion of HKT into languages where no HKT exists. They demonstrated the power of the proposed approach for OCaml but let's focus on java.

#### The App interface
The article introduces the following type:
```java
interface App<F, A> {}
```
This interface can be viewed in terms of Haskell like `F A` or in terms of scala like `F[A]`. This type denotes the idea that the type parameter `A` is applied to the type parameter `F`. Here both `F` and `A` are polymorphic. Using the `App` interface the code from above becomes legal in java if it is written like this:
```java
interface Applicative<M> {
    <A> App<M, A> pure(A value);
}
```
How does one convert `App<F, A>` and `F<A>` back and forth? `F<A>` needs to become a subtype of `App<F, A>` but the declaration is a bit tricky. Let's start with the simplest possible container: `Identity`. Semantically `Identity<T>` is equal to `T`:
```java
class Identity<T> extends App<Identity.mu, T> {
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
App<Identity.mu, Integer> idContainer = new Identity<>(42);
int originalValue = Identity.narrow(idContainer).value;
assert originalValue == 42;
```
It's enough to demonstrate the conversion back and forth between `F<A>` and `App<F, A>`

#### Real world implementations

**"Lightweight higher-kinded polymorphism"** already found its implementation in the projects for **Arrow** and **DataFixerUpper**.

- **Arrow** is a general purpose functional-programming library that brings the notions of functors, monads and other category theory structures to kotlin.
- **DataFixerUpper** is a library that is used in minecraft to define category theory optics for data migration among versions of API.