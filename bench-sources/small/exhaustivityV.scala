package exhaustivityV

import scala.annotation.nowarn

sealed trait O
object A extends O
object B extends O

object Test {

  @nowarn("id=E029")
  def test(x: O) =
  (x, x, x, x, x, x, x, x, x, x, x, x, x, x, x) match {
    case (A, A, A, A, A, _, _, _, _, _, _, _, _, _, _) => 1
    case (B, _, _, _, _, A, A, A, A, _, _, _, _, _, _) => 2
    case (_, B, _, _, _, B, _, _, _, A, A, A, _, _, _) => 3
    case (_, _, B, _, _, _, B, _, _, B, _, _, A, A, _) => 4
    case (_, _, _, B, _, _, _, B, _, _, B, _, B, _, A) => 5
    case (_, _, _, _, B, _, _, _, B, _, _, B, _, B, B) => 6

  }
}
