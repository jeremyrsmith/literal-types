# literal-types
This is a compiler plugin which allows specifying singleton literal types.

## About
This project was mainly done as an exercise in creating a compiler plugin.  I used
[kind-projector](https://github.com/non/kind-projector) as the skeleton, so credit goes to that project.  I'm really not
sure if this is a useful or good idea, but in case you want to play around with it, I published it to bintray:

```scala
resolvers += Resolver.bintrayRepo("jeremyrsmith", "maven")
addCompilerPlugin("io.github.jeremyrsmith" %% "literal-types" % "0.1.0")
```

Scala has a concept of singleton types; that is, the type that is inhabited by only a single value:

```scala
val foo = "foo string"

// this function will only accept foo as its argument
def fn(arg: foo.type) = ???
```

So we can look at the type of `foo` in different ways, from least to most specific:

1. `Any`
2. `AnyRef`
3. `String`
4. `foo.type`

But there is also a notion of *constant types*, that is, a type whose value is statically known.  This is available
for certain types - at least `String` and `Int`, but possibly others (and notably *not* `Symbol`).  So the type of 
`foo` above has another level to it:

1. `Any`
2. `AnyRef`
3. `String`
4. `String("foo string")`
5. `foo.type`

These types can't be directly expressed in the Scala language - not because the compiler doesn't know about them, but
because the parser just doesn't allow for them to be expressed.

What this compiler plugin does is allow them to be expressed, in a fashion.  The caveat is that you'll sacrifice a few
classes of type identifiers.

## Expressing Literal Types
The plugin works by rewriting certain type identifiers into constant types.  Since the types you're expressing aren't
legal scala identifiers, they must be encased in backticks, like:

```scala
type Foo = `"foo"`
```

In this case, `` `"foo"` `` is replaced with the constant type `String("foo")`.  Of course, this means that you won't
be able to use that literal name as an identifier for a type.  But you wouldn't do that anyway, would you?

The literal types that can be expressed this way are:

1. String - `` `"some string"` ``
2. Int - `` `23` ``
3. Symbol - `` `'foo` `` - `Symbol` is a special case, because it's not actually a constant type.  Therefore, it needs a
different strategy for creating that type.  Since the only case you would want to use a `Symbol` singleton literal type
is with [shapeless](https://github.com/milessabin/shapeless), when you specify a symbol type as shown, it's assumed that
shapeless is available in the project, and it will be rewritten to i.e. `` Witness.`'foo`.T ``, which is shapeless' own
syntax for specifying literal types.

The plugin will operate on any such identifier *in a type position*. That is, you can use such identifiers as names of
variables (which again, you shouldn't be doing) but not as names of types.

Again, I'm not sure how useful this idea is, because (as mentioned just above) you can already specify these types using
the slightly more arduous `` Witness.`[literal]`.T `` syntax of shapeless.