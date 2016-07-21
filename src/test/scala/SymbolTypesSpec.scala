import org.scalatest.FreeSpec
import shapeless.labelled.FieldType
import shapeless.syntax.singleton._
import shapeless._
import shapeless.record.Record

class SymbolTypesSpec extends FreeSpec {



  "symbol literals" - {
    "as parameter type annotation" in {
      def inFunction(t: `'foo`) = t
      assert(inFunction('foo.narrow) == 'foo)
      assertDoesNotCompile("inFunction('bar)")
    }

    "as type alias" in {
      type A = `'foo`
    }

    "as part of hlist type" in {
      type ->>[K, V] = FieldType[K, V]
      type A = (`'foo` ->> Int) :: (`'bar` ->> String) :: HNil
      implicitly[A <:< Record.`'foo -> Int, 'bar -> String`.T]
    }

    "don't interfere with valdef" in {
      val `'foo` = 10
    }

    "valdef annotation" in {
      val `'foo`: `'foo` = 'foo.narrow
    }
  }

  "string literals" - {
    "as parameter type annotation" in {
      def inFunction(t: `"foo"`) = t
      assert(inFunction("foo") == "foo")
      assertDoesNotCompile("""inFunction("bar")""")
    }

    "as type alias" in {
      type A = `"foo"`
    }

    "as part of hlist type" in {
      type ->>[K, V] = FieldType[K, V]
      type A = (`"foo"` ->> Int) :: (`"bar"` ->> String) :: HNil
      implicitly[A <:< Record.`"foo" -> Int, "bar" -> String`.T]
    }

    "don't interfere with valdef" in {
      val `"foo"` = 10
    }

    "valdef annotation" in {
      val `"foo"`: `"foo"` = "foo".narrow
    }
  }

  "integer literals" - {
    "as parameter type annotation" in {
      def inFunction(t: `23`) = t
      assert(inFunction(23) == 23)
      assertDoesNotCompile("""inFunction(22)""")
    }

    "as type alias" in {
      type A = `22`
    }

    "as part of hlist type" in {
      type ->>[K, V] = FieldType[K, V]
      type A = (`22` ->> Int) :: (`23` ->> String) :: HNil
      implicitly[A <:< Record.`22 -> Int, 23 -> String`.T]
    }

    "don't interfere with valdef" in {
      val `23` = 10
    }

    "valdef annotation" in {
      val `23`: `23` = 23.narrow
    }
  }
}
