package datatypes

import cats.effect._
import cats.syntax.all._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

class b_concurrent_cancel_start {
  val io = IO.shift *> IO(println("Hello!"))

  val fiber: IO[Fiber[IO, Unit]] = io.start

  val launchMissiles = IO.raiseError(new Exception("boom!"))
  val runToBunker = IO(println("To the bunker!!!"))

  for {
    fiber <- IO.shift *> launchMissiles.start
    _ <- runToBunker.handleErrorWith { error =>
      // Retreat failed, cancel launch (maybe we should
      // have retreated to our bunker before the launch?)
      fiber.cancel *> IO.raiseError(error)
    }
    aftermath <- fiber.join
  } yield {
    aftermath
  }
}
