TODO:
- explain why downcast is safe
- should there be a monad explanation?
- figure out if I need a curried Functor#map


Structure:

- Abstract
- HKT
  - motivation
  - type constructor vs data constructor
  - examples
- Lightweight HKT
  - Lightweight higher-kinded polymorphism - Jeremy Yallop and Leo White
  - App interface, injections/projections
  - kind class, marker interfaces
  - Examples: Monad, Monoid.
- Monad transformers
  - lifting
- Continuation
  - callCC
- Common Monads
  - State
  - Writer
- Unit/Singleton
- Coroutine
  - Definition
    - Internal monad
  - Interface MonadCoroutine
  - Kind
  - Monad
  - runner
  - example (Fibonacci)

# Abstract
Coroutines can be viewed as a special case of the continuation monad.
This paper implements coroutines in terms of pure java 8 without direct
bytecode manipulations nor JNI/FFI calls.

# Higher-Kinded polymorphism
Higher-kinded polymorphism (or higher-kinded types, HKT) is a means to
generalize over type constructors.

## Motivation
Consider the following overloaded function `map`:

```java
class MapperUtil {
  static <A, B> Id<B> map(Function<A, B> fun, Id<A> id) {
    B b = fun.apply(id.getValue());
    return new Id<>(b);
  }
  
  static <A, B> List<B> map(Function<A, B> fun, List<A> as) {
    if (as instanceof Nil) return new Nil<>();
    B b = fun.apply(as.head());
    return new Cons<>(b, map(fun, as.tail()));
  }

  static <A, B> Maybe<B> map(Function<A, B> fun, Maybe<A> m) {
    if (m instanceof Nothing) return new Nothing<>();
    B b = fun.apply(m.get());
    return new Just<>(b);
  }
  static <A, B> Tree<B> map(Function<A, B> fun, Tree<A> m) { ... }
}

class Id<A> {
  Id(A value) { ... }
}
abstract class List<A> {
  static class Nil<A> extends List<A> {
    Nil() { ... }
  }

  static class Cons<A> extends List<A> {
    Cons(A a, List<A> rest) { ... }
  }
}

abstract class Maybe {
  static class Nothing<A> extends Maybe<A> {
    Nothing() { ... }
  }

  static class Just<A> extends Maybe<A> {
    Just(A element) { ... }
  }
}

abstract class Tree<A> { ... }
```
All the versions of the overloaded function `map` serves the same purpose:
they convert values which are wrapped in a container from one type to
another one without altering the structure of the container.
All the signatures of the overloaded function look the almost same, they
all accept a function from `A` to `B`, but they differ on the second
argument: the first one accepts `Id<A>`, the second one accepts `List<A>`,
the third one accepts `Maybe<A>` and the forth one accepts `Tree<A>`.
Notice that the type parameter `A` is the same in all four functions.
The difference is what comes before `A`. This part is called a
**type constructor**.

Software engineering is all about generalization: the better a program
is generalized the more concise it becomes. How does one generalize this
function? The interface-based ad hoc polymorphism usually comes to the rescue.

Even though some programming languages like Haskell or Scala offers a means of
generalization over type constructors, it's known to be illegal to write in
java constructions like:
```java
interface Functor<F> {
  <A, B> F<B> map(Function<A, B> fun, F<A> cont); // <<-- Error: Type 'F' does not have type parameters.
}
```

## Type constructors
### Kinds
A type constructor is a feature of a programming language that allows one
to build new types from old ones. Java implements this idea using generics.
The number of type parameters defines the arity of their type constructor. The
arity splits the set of all type constructors into equivalence classes.
Such equivalence classes are called *kinds*. A type constructor without any
type parameters (for example, `Integer`) is denoted as `*`, a type constructor
with a single type parameter (for example, `List`) is denoted as `* -> *`,
a type constructor with two type parameters (for example, `Map`) is denoted
as `* -> * -> *`, and so on.

### Relation to data constructors
Type constructors differ from data constructors (*regular constructors*)
in the context they are applicable to: whereas data constructors are called
during a program's execution, they build new values and define what the values
are like, type constructors are meant to create new types during a
program's compilation.

## Examples

Type classes are the corner stone in Haskell to define abstractions like
Functors, Monad, Monoid, etc. Type classes allows implementing the ad hoc
polymorphism in Haskell. They are heavily rely on HKT. For example, here
is the definition of Functor in Haskell:
```haskell
class Functor f where
  fmap :: (a -> b) -> f a -> f b
```
Due to Haskell's reach type system the compiler can deduce the arity of
the `f` type constructor which is 1 and its kind is `* -> *`.

Scala defines this construction via the mechanism of traits and the explicit
specification the type constructor's arity:
```scala
trait Functor[F[_]] {
  def map[A, B](fun: A => B, cont: F[A]): F[B]
}
```
Here the programmer has to explicitly inform the compiler that the `F` type
constructor accepts just one type parameter by writing `F[_]`, e.g. its arity
is 1, which means that its kind is `* -> *`.

# Lightweight HKT
There is a way to bring the notion of HKT to languages which lack HKT.
For this to be possible a number of new abstractions needs to be introduced.

## The App interface
The `App` interface encodes the notion of `F<A>`, where both `F` and `A`
are polymorphic:
```java
interface App<F, A> {}
```
The `Functor` interface from above can be transformed into a legal
java construction using the `App` interface:
```java
interface Functor<F> {
  <A, B> App<F, B> map(Function<A, B> fun, App<F, A> value);
}
```

## Injections and projections
Injections and projection functions are meant to perform legal
conversions from `App<F, A>` to `F<A>` and vice versa.

### Injection
`F<A>` needs to become a subtype of `App<F, A>` but the declaration is a bit
tricky. Let's start with the simplest possible container: `Id`. Semantically
`Id<T>` is the same as `T`:

```java
class Id<T> implements App<Id.mu, T> {
  T value;
  public Id(T value) {
    this.value = value;
  }
  public static final class mu {}
}
```
Here the class `Id.mu` is passed into `App` instead of passing `Id` because
`Id` is generalized over `T` and `Id.mu` is not generalized. Java inheritance
rules allow passing an instance of `Id<T>` everywhere where `App<Id.mu, T>` is
expected. This way defines the conversion from `F<A>` to `App<F, A>`.

### Projection

The conversion from `App<F, A>` to `F<A>` is done by performing a downcast:
```java
class Id<T> extends App<Id.mu, T> {
    // the field and constructor omitted
    
    public static final class mu {}
    
    static<T> Id<T> narrow(App<Id.mu, T> value) {
        return (Id<T>) value;
    }
}
```
So far the polymorphic container `Id` can be used like this:
```java
App<Id.mu, Integer> appIdInt = new Id<>(42);
Id<Integer> idContainer = Id.narrow(appIdInt);
assert idContainer.value == 42;
```

### Kind class and marker class

The class that implements `App<F, A>` is usually called a `Kind` class and its
name contains the `Kind` suffix.

The kind class also defines a marker class which is usually called `mu`. It helps
to perform downcast safely(TODO: make a better explanation). 
Every polymorphic container has to define its own class `mu`.

## Examples

### Monad

A `Monad` interface inherits `Functor`. It should define two methods:

- `pure` - a trivial way to put a value into the monadic container
- `flatMap` - a monadic bind, same as `(>>=)` in Haskell

The `Functor#map` function can be implemented in terms of these two functions.

Here is the full definition of `Monad`:
```java
interface Monad<M> extends Functor<M> {
  <A> App<M, A> pure(A a);

  <A, B> App<M, B> flatMap(App<M, A> ma, Function<A, App<M, B>> aToMb);

  @Override
  default <A, B> App<M, B> map(Function<A, B> fun, App<M, A> ma) {
      return flatMap(ma, a -> pure(fun.apply(a)));
  }
}
```

### Monad example

Let's take a look at the most simplistic monad of all: `Id`.
```java
class Id<T> implements App<Id.mu, T> {
  T value;

  public Id(T value) {
    this.value = value;
  }

  public static final class mu { }

  static <T> Id<T> narrow(App<Id.mu, T> value) {
    return (Id<T>) value;
  }
}

enum IdMonad implements Monad<Id.mu> {
  INSTANCE;

  @Override
  public <A> App<Id.mu, A> pure(A a) {
    return new Id<>(a);
  }

  @Override
  public <A, B> App<Id.mu, B> flatMap(App<Id.mu, A> ma, Function<A, App<Id.mu, B>> aToMb) {
    A a = Id.narrow(ma).value;
    return aToMb.apply(a);
  }
}
```
The following example demonstrates how to use the `IdMonad`:
```java
public class Main {
  public static void main(String[] args) {
    IdMonad m = IdMonad.INSTANCE;
    App<Id.mu, String> app = m.flatMap(m.pure(5),
            currValue -> m.flatMap(m.pure("+".repeat(currValue)),
            currString -> m.pure("The string '" + currString + "' was obtained by repeating '+' " + currValue + " times")
    ));
    Id<String> result = Id.narrow(app).value;
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

1. The monadic computation above starts by lifting `5` to the `Id` container by `m.pure(5)`;
2. `IdMonad` calls `flatMap` on two arguments `m.pure(5)` and the first lambda that starts with `currValue -> ...`. Here `currValue` contains `5`: the current value inside of the container;
3. The first lambda calls `flatMap` on two arguments `m.pure("+".repeat(currValue))` and the second lambda that starts with `currString -> ...`. Here `currString` contains a sequence of `"+"` repeated `currValue` times.
4. The second lambda has access to both `currValue` and `currString`, it uses both of them to build the resulting string. The resulting string is lifted into the `Id` container using `m.pure(...)`;
5. The `app` variable is of the `App<Id.mu, String>` type and needs to be converted to `Id<String>`, because the `App` interface does not expose any API to work with;
6. The conversion of `app` to `Id<String>` is performed with `Id::narrow` and stored into the `result` variable;
7. `Id` has the `value` field which contains the resulting string of the monadic computation. Printing `value` outputs the string that was built in the 4-th step.

Unfortunately, java doesn't have any language construction that resembles
*do-notation* from Haskell or *for comprehension* in Scala, so there have
to be nested `flatMap` calls to achieve the same effect.

Here is the equivalent code in Haskell that relies on *do-notation* for references.
```haskell
import Control.Monad.Id

main = print $ runId $ do
  currValue <- return 5
  currString <- return $ concat (replicate currValue "+")
  return $ "The string '" ++ currString ++ "' was obtained by repeating '+' " ++ (show currValue) ++ " times"
```
Execution:
```bash
$ ghc Main.hs && ./Main
[1 of 1] Compiling Main             ( Main.hs, Main.o )
Linking Main ...
"The string '+++++' was obtained by repeating '+' 5 times"
```

### Monoid
A `Monoid` interface defines two methods:

- `empty` - the neutral element of the monoid
- `concat` - a general way to combine the content of two monoids 

Here is the full definition of `Monoid`:
```java
interface Monoid<A> {
  A empty();
  A concat(A left, A right);
}
```
