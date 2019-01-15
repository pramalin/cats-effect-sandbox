package datatypes
import cats.effect.IO

object e_error_handling extends App {

  // raiseError
  val boom = IO.raiseError(new Exception("boom"))
  // boom: cats.effect.IO[Nothing] = IO(throw java.lang.Exception: boom)

  boom.unsafeRunSync()

}
