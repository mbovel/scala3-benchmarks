package implicitCache

class A
object A {
  implicit def theA: A = new A
}
class Foo[T]
object Foo {
  implicit def theFoo: Foo[A] = new Foo[A]
}

object Test {
  def getFooA(using foo: Foo[A]) = foo
  def getA(using a: A) = a

  getFooA
  getA
}
