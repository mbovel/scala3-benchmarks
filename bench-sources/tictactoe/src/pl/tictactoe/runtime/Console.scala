package pl.tictactoe.runtime

import cats.effect.IO

trait Console:
  def readLine: IO[String]
  def printLine(s: String): IO[Unit]

object LiveConsole extends Console:
  override def readLine: IO[String] = IO.readLine
  override def printLine(s: String): IO[Unit] = IO.println(s)
