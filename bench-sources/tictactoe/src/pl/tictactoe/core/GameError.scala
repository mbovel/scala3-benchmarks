package pl.tictactoe.core

import cats.syntax.all._

enum GameError(msg: String) extends Throwable(msg):
  case FieldAlreadyTaken(c: Coordinate, p: Player) extends GameError(show"The field $c is already taken by $p.")
  case WrongPlayer(c: Coordinate, p: Player) extends GameError(show"Wrong player: $p.")
  case CoordinateOutOfBound(c: Coordinate)
      extends GameError(
        show"Coordinates are out of bounds: $c (${c.x}, ${c.y})."
      )
  case GameIsOver(winner: Option[Player])
      extends GameError(
        winner.fold("Game was drawn")(p => show"Game was already won by $p.")
      )
