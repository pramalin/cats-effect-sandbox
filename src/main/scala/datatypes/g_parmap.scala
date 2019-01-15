package datatypes
import cats.effect.IO
import cats.syntax.all._
import cats.implicits._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object g_parmap extends App {

  val cachedThreadPool = Executors.newCachedThreadPool()
  val BlockingFileIO   = ExecutionContext.fromExecutor(cachedThreadPool)
  implicit val Main = ExecutionContext.global

  val ioA = IO(println("Running ioA"))
  val ioB = IO(println("Running ioB"))
  val ioC = IO(println("Running ioC"))

//  val program = (ioA, ioB, ioC).parMapN{(_, _, _) => ()}

//  program.unsafeRunSync()
}
