package pl.tictactoe.core

enum GameStatus:
  case Ongoing(nextPlayer: Player)
  case Won(winner: Player)
  case Drawn
