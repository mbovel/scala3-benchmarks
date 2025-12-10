package pl.tictactoe.runtime

import cats.effect.IO
import cats.syntax.all._
import pl.tictactoe.core._

final class GameRuntime(console: Console):

  private def loop(game: Game): IO[Unit] = for
    _ <- console.printLine(show"\n${game.fields}\n")
    _ <- game.status match
      case GameStatus.Drawn => console.printLine("\nDraw!\n")
      case GameStatus.Won(player) =>
        console.printLine(show"\nPlayer ${player} won the game!\n")
      case GameStatus.Ongoing(nextPlayer) =>
        for
          _ <- console.printLine("")
          move <- readLoop(nextPlayer)
          _ <- game.move(move, nextPlayer) match
            case Right(updated) => loop(updated)
            case Left(error) =>
              console.printLine(error.getMessage) >> loop(game)
        yield ()
  yield ()

  private def readLoop(nextPlayer: Player): IO[Coordinate] = for
    _ <- console.printLine(show"Player $nextPlayer please make a move: ")
    line <- console.readLine
    result <- Coordinate.parse(line) match
      case Some(result) => result.pure[IO]
      case None =>
        console.printLine("Please enter correct coordinate!") >> readLoop(nextPlayer)
  yield result

  val run: IO[Unit] =
    console.printLine("\n-- Starting a new game --\n") >> loop(Game.create)
