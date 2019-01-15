package datatypes
import cats.effect.IO
import scala.concurrent.Future

object d_from_future extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  val iof1 = IO.fromFuture(IO {
    Future(println("I come from the Future!"))
  })


  val f2 = Future.successful("I come from the Future!")

  val iof2 = IO.fromFuture(IO.pure(f2))

}
