package pl.tictactoe.core

import cats.syntax.all._

class CoordinateSpec extends munit.FunSuite:

  test("should parse correct coordinates") {

    // when
    val cases = List(
      ("A1", Some(Coordinate(0, 0))),
      ("A3", Some(Coordinate(0, 2))),
      ("B1", Some(Coordinate(1, 0))),
      ("B2", Some(Coordinate(1, 1))),
      ("B3", Some(Coordinate(1, 2))),
      ("C1", Some(Coordinate(2, 0))),
      ("C3", Some(Coordinate(2, 2)))
    )

    // then
    cases.foreach { case (rawString, expected) =>
      assert(clue(Coordinate.parse(rawString)) == clue(expected))
    }
  }

  test("should reject incorrect coordinates") {

    // when
    val cases = List(
      "A0",
      "3A",
      "Z29",
      "!!",
      "B-1",
      "BB4",
      "1B2",
      ""
    )

    // then
    cases.foreach(rawString => assert(clue(Coordinate.parse(rawString)) == None))
  }

  test("should correctly format coordinates") {
    // given
    val cases = List(
      Coordinate(0, 0) -> "A1",
      Coordinate(0, 2) -> "A3",
      Coordinate(2, 0) -> "C1",
      Coordinate(1, 1) -> "B2",
      Coordinate(3, 4) -> "--OUT OF BOUNDS--"
    )

    // then
    cases.foreach { case (coordinate, expected) =>
      assert(clue(coordinate.show) == expected)
    }

  }
