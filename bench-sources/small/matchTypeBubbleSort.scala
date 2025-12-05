package matchTypeBubbleSort

import scala.compiletime.ops.int.*

object TupleOps:
  import Tuple.*

  type Reduce[T <: NonEmptyTuple, F[_, _]] =
    Fold[Tuple.Tail[T], Tuple.Head[T], F]

  infix type Maximum[T <: NonEmptyTuple] = Reduce[
    T,
    [A, B] =>> (A, B) match
      case (Int, Int) => Max[A, B],
  ]

  type IndexOfRec[T <: Tuple, Elem, I <: Int] = Tuple.Elem[T, I] match
    case Elem => I
    case _ => IndexOfRec[T, Elem, I + 1]

  infix type IndexOf[T <: Tuple, Elem] = IndexOfRec[T, Elem, 0]

  type DropLargest[T <: NonEmptyTuple] =
    T IndexOf Maximum[T] match
      case Int =>
        Concat[Take[T, (T IndexOf Maximum[T])], Drop[T, (T IndexOf Maximum[T]) + 1]]

  type BubbleSort[T <: Tuple] = T match
    case EmptyTuple => EmptyTuple
    case NonEmptyTuple =>
      Concat[BubbleSort[DropLargest[T]], (Maximum[T] *: EmptyTuple)]

@main def Main =
  println(compiletime.constValueTuple[(TupleOps.BubbleSort[(
      10,
      8,
      29,
      20,
      24,
      15,
      2,
      17,
      3,
      30,
      22,
      25,
      5,
      16,
      9,
      4,
      23,
      14,
      18,
      1,
      26,
      7,
      6,
      0,
      27,
      19,
      11,
      12,
      13,
      28,
      21,
  )])])
