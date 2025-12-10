package pl.tictactoe.runtime

import cats.effect.kernel.Ref
import cats.effect.IO
import munit.Assertions._

class TestConsole private (
    inputs: Ref[IO, List[String]],
    outputs: Ref[IO, List[String]]
) extends Console:

  def printedLines: IO[List[String]] = outputs.get.map(_.reverse)

  override def readLine: IO[String] =
    inputs.modify {
      case x :: xs => (xs, x)
      case Nil     => fail("No more input strings for test console!")
    }

  override def printLine(s: String): IO[Unit] =
    outputs.update(xs => s :: xs)

object TestConsole:

  def create(inputs: String*): IO[TestConsole] = for
    inputs <- Ref.of[IO, List[String]](inputs.toList)
    outputs <- Ref.of[IO, List[String]](Nil)
  yield TestConsole(inputs, outputs)
