package pl.tictactoe.core

type Direction = (-1 | 0 | 1, -1 | 0 | 1)

object Direction:
  // left
  val L: Direction = (-1, 0)
  // right
  val R: Direction = (1, 0)

  // top
  val T: Direction = (0, -1)
  // bottom
  val B: Direction = (0, 1)

  // left top
  val LT: Direction = (-1, -1)
  // right top
  val RT: Direction = (1, -1)

  // left bottom
  val LB: Direction = (-1, 1)
  // right bottom
  val RB: Direction = (1, 1)
