package pl.tictactoe.core

import cats.syntax.all._

class GameSpec extends munit.FunSuite:

  extension (b: Game)
    def moveMultiple(moves: (String, Player)*): Either[GameError, Game] =
      moves.foldLeft(b.asRight) { case (board, (move, player)) =>
        val coordinate =
          Coordinate.parse(move).getOrElse(fail(s"Wrong coordinate: $move !"))
        board.flatMap(_.move(coordinate, player))
      }

  test("should reject moves into same place") {
    // given
    val board = Game.create
    val Right(updated) = (board.move(Coordinate(1, 1), Player.X): @unchecked)

    // when
    val Left(error) = (updated.move(Coordinate(1, 1), Player.O): @unchecked)

    // then
    assert(
      clue(error) == GameError.FieldAlreadyTaken(Coordinate(1, 1), Player.O)
    )
  }

  test("should reject moves from the wrong player") {
    // given
    val board = Game.create

    // when
    val Left(error) = (board.move(Coordinate(1, 1), Player.O): @unchecked)

    // then
    assert(clue(error) == GameError.WrongPlayer(Coordinate(1, 1), Player.O))
  }

  test("should reject moves with coordinates out of bounds") {
    // given
    val board = Game.create

    // when
    val Left(error) = (board.move(Coordinate(5, 4), Player.X): @unchecked)

    // then
    assert(clue(error) == GameError.CoordinateOutOfBound(Coordinate(5, 4)))
  }

  test("should allow playing whole game and winning by one player") {
    // given
    val board = Game.create

    // when
    val cases = List(
      board.moveMultiple(
        ("A1", Player.X),
        ("B1", Player.O),
        ("B2", Player.X),
        ("C2", Player.O),
        ("C3", Player.X)
      ) ->
        Player.X,
      board.moveMultiple(
        ("A1", Player.X),
        ("B2", Player.O),
        ("A2", Player.X),
        ("B1", Player.O),
        ("C3", Player.X),
        ("B3", Player.O)
      ) ->
        Player.O,
      board.moveMultiple(
        ("A3", Player.X),
        ("A1", Player.O),
        ("B2", Player.X),
        ("A2", Player.O),
        ("C1", Player.X)
      ) ->
        Player.X,
      board.moveMultiple(
        ("A1", Player.X),
        ("A3", Player.O),
        ("C1", Player.X),
        ("B3", Player.O),
        ("C2", Player.X),
        ("C3", Player.O)
      ) -> Player.O
    )

    // then
    cases.foreach { case (game, winner) =>
      val Right(Game(_, status, _)) = (game: @unchecked)
      assert(clue(status) == GameStatus.Won(winner))
    }

  }

  test("should allow playing whole game and drawing") {
    // given
    val board = Game.create

    // when
    val Right(result) = (board.moveMultiple(
      ("A1", Player.X),
      ("A2", Player.O),
      ("A3", Player.X),
      ("B1", Player.O),
      ("B3", Player.X),
      ("B2", Player.O),
      ("C2", Player.X),
      ("C3", Player.O),
      ("C1", Player.X)
    ): @unchecked)

    // then
    assert(clue(result.status) == GameStatus.Drawn)

  }

  test("should disallow playing game after it's finished") {
    // given
    val Right(board) = (Game.create.moveMultiple(
      ("A1", Player.X),
      ("A2", Player.O),
      ("B1", Player.X),
      ("C3", Player.O),
      ("C1", Player.X)
    ): @unchecked)

    // when
    val result = board.move(Coordinate(0, 2), Player.O)

    // then
    assert(clue(result) == Left(GameError.GameIsOver(Player.X.some)))
  }
