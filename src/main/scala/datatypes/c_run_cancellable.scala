package datatypes

object c_run_cancellable extends App {

  import cats.effect._
  import cats.syntax.all._

  import scala.concurrent.ExecutionContext
  import scala.concurrent.duration._

  // Needed for `sleep`
  implicit val timer = IO.timer(ExecutionContext.global)

  // Delayed println
  val io: IO[Unit] = IO.sleep(10.seconds) *> IO(println("Hello!"))

  val cancel: IO[Unit] =
    io.unsafeRunCancelable(r => println(s"Done: $r"))

  // ... if a race condition happens, we can cancel it,
  // thus canceling the scheduling of `IO.sleep`
  cancel.unsafeRunSync()

  val pureResult: SyncIO[IO[Unit]] = io.runCancelable { r =>
    IO(println(s"Done: $r"))
  }

  // On evaluation, this will first execute the source, then it
  // will cancel it, because it makes perfect sense :-)
  val cancel2 = pureResult.toIO.flatten
  cancel2.unsafeRunSync()

}
