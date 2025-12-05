package implicitScopeLoop

trait Dummy[T]


trait A[T] extends B
trait B extends Dummy[A[Int]]
object B {
  implicit def theB: B = new B {}
  implicit def theA: A[Int] = new A[Int] {}
}

object Test {
  def getB(using b: B) = b
  def getA[T](using a: A[T]) = a

  getB
  getA
}