package pl.tictactoe.core

import cats.syntax.all._
import cats.Show
import scala.collection.immutable.ArraySeq

val BoardSize = 3

opaque type Board = ArraySeq[ArraySeq[FieldStatus]]

object Board:
  given Show[Board] with
    def show(f: Board) =
      val numbers = LazyList.from(1).take(f.size).map(String.format("%1$2d", _))
      val letters = LazyList.from('A').take(f.size).map(_.toChar.toString)

      ("   " + letters.mkString(" ") + "\n") + f
        .zip(numbers)
        .map { case (line, number) =>
          (number + " ") + line
            .map {
              case FieldStatus.Empty           => " "
              case FieldStatus.Taken(Player.X) => "X"
              case FieldStatus.Taken(Player.O) => "O"
            }
            .mkString("|")
        }
        .mkString("\n   " + "- " * f.size + "\n")

  def create: Board =
    ArraySeq.fill(BoardSize)(ArraySeq.fill(BoardSize)(FieldStatus.Empty))

  extension (f: Board)
    def apply(c: Coordinate): Option[FieldStatus] = for
      line <- f.get(c.y)
      cell <- line.get(c.x)
    yield cell

    def update(c: Coordinate, fieldStatus: FieldStatus): Option[Board] =
      import c.{x, y}
      for line <- f.get(y)
      yield f.updated(y, line.updated(x, fieldStatus))
