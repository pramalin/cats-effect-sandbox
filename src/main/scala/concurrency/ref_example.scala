package concurrency

import java.util.concurrent.Executors

import cats.effect.{ExitCode, IO, IOApp, Sync}

import scala.concurrent.ExecutionContext
import cats.effect.concurrent.Ref
import cats.implicits._

import scala.concurrent.ExecutionContext
object ref_example extends IOApp {
  val cachedThreadPool = Executors.newCachedThreadPool()
  val BlockingFileIO = ExecutionContext.fromExecutor(cachedThreadPool)
  implicit val ctx = IO.contextShift(ExecutionContext.global)

  class Worker[F[_]](number: Int, ref: Ref[F, Int])(implicit F: Sync[F]) {

    private def putStrLn(value: String): F[Unit] = F.delay(println(value))

    def start: F[Unit] =
      for {
        c1 <- ref.get
        _ <- putStrLn(show"#$number >> $c1")
        c2 <- ref.modify(x => (x + 1, x))
        _ <- putStrLn(show"#$number >> $c2")
      } yield ()

  }

  val program: IO[Unit] =
    for {
      ref <- Ref.of[IO, Int](0)
      w1 = new Worker[IO](1, ref)
      w2 = new Worker[IO](2, ref)
      w3 = new Worker[IO](3, ref)
      _ <- List(
        w1.start,
        w2.start,
        w3.start
      ).parSequence.void
    } yield ()

  def run(args: List[String]): IO[ExitCode] = {
      program >> IO.pure(ExitCode.Success)
  }
}