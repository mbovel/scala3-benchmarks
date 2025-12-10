package pl.tictactoe

import cats.effect.IOApp
import cats.effect.IO

import pl.tictactoe.runtime.LiveConsole
import pl.tictactoe.runtime.GameRuntime

object Main extends IOApp.Simple:

  override val run = GameRuntime(LiveConsole).run.foreverM
